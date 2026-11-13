package com.chua.starter.tenant.server.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 为存量「引用型」租户补专用数据库账号。
 *
 * <p>A 方案要求每个租户在目标实例上有自己的 {@code t_<编码>} 账号。升级前创建的租户
 * 记录没有这个账号（{@code sys_tenant_sql_username} 为空），它们目前仍用实例账号路由，
 * 能正常工作但没有 DB 层隔离。本任务把它们逐个补上。</p>
 *
 * <h2>为什么不能只改记录</h2>
 *
 * <p>账号必须在实例上真实创建并授权后才能写回记录：先写记录再建账号，中间任何一步失败
 * 都会让租户指向一个不存在的账号，该租户所有请求直接 503。因此顺序固定为
 * <b>建库建账号 → 成功后才落记录</b>，失败时记录保持原样、继续用实例账号。</p>
 *
 * <h2>幂等与并发</h2>
 *
 * <p>只挑「引用了实例且没有 sql_username」的租户，处理过的不会再次入选；
 * 账号创建本身也是 {@code IF NOT EXISTS} + {@code ALTER USER}，可重复执行。
 * 单节点内用 {@link ReentrantLock} 防止两个管理员同时点；多节点部署时仍需外部串行化，
 * 但即使并发，最终落库的口令与最后一次 {@code ALTER USER} 一致的概率很高，
 * 不一致时重跑一次即可。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TenantDedicatedAccountBackfill {

    /**
     * 单次最多处理多少个租户
     */
    private static final int MAX_LIMIT = 200;

    /**
     * 租户查询
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 建库/建账号器
     */
    private final TenantDatabaseProvisioner tenantDatabaseProvisioner;

    /**
     * 口令加解密
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 数据源注册器（落库后释放旧连接池）
     */
    private final TenantDataSourceRegistry tenantDataSourceRegistry;

    /**
     * 同一实例内串行执行
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 执行补账号任务。
     *
     * @param dryRun 只演练、不改库也不改记录
     * @param limit  本次最多处理多少个（1..200）
     * @return 执行结果
     */
    public Result run(boolean dryRun, int limit) {
        int max = Math.max(1, Math.min(limit, MAX_LIMIT));
        boolean locked = false;
        if (!dryRun) {
            locked = lock.tryLock();
            if (!locked) {
                return new Result(false, List.of(), 0, "已有补账号任务在执行，请稍后重试");
            }
        }
        try {
            List<SysTenant> rows = sysTenantMapper.selectList(Wrappers.<SysTenant>lambdaQuery()
                    .isNotNull(SysTenant::getSysTenantDatasourceId)
                    .ne(SysTenant::getSysTenantDatasourceId, 0L)
                    .and(w -> w.isNull(SysTenant::getSysTenantSqlUsername)
                            .or().eq(SysTenant::getSysTenantSqlUsername, ""))
                    .orderByAsc(SysTenant::getCreateTime)
                    .last("limit " + (max + 1)));
            List<SysTenant> todo = rows.size() > max ? rows.subList(0, max) : rows;

            List<Item> items = new ArrayList<>(todo.size());
            for (SysTenant tenant : todo) {
                items.add(handle(tenant, dryRun));
            }
            long remaining = countRemaining();
            String message = remaining > 0
                    ? (dryRun ? "演练完成，仍有 " + remaining + " 个租户待处理" : "已处理 " + items.size() + " 个，仍有 " + remaining + " 个待处理")
                    : (dryRun ? "演练完成，没有待处理租户" : "全部处理完成");
            return new Result(dryRun, items, remaining, message);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 处理一个租户。
     *
     * @param tenant 租户记录
     * @param dryRun 是否只演练
     * @return 单项结果
     */
    private Item handle(SysTenant tenant, boolean dryRun) {
        String dbName = tenant.getSysTenantDbname();
        if (!hasText(dbName)) {
            return Item.skipped(tenant, "未填写库名，无法确定授权范围");
        }
        if (!tenantDatabaseProvisioner.supportsDedicatedAccount(tenant)) {
            return Item.skipped(tenant, "实例不是 MySQL，当前不支持自动建账号");
        }
        String username = TenantDbAccountNaming.generate(tenant);
        tenant.setSysTenantSqlUsername(username);
        if (!tenantDatabaseProvisioner.isAvailable(tenant)) {
            return Item.skipped(tenant, "管理连接不可用（实例来源或管理权限缺失）");
        }
        if (dryRun) {
            return Item.planned(tenant, username, "将在 " + dbName + " 上创建该账号并只授权到本库");
        }

        // [P3C A20 豁免]
        String plainPassword = IdUtils.createSimpleUuid().substring(0, 24);
        tenant.setSysTenantSqlPassword(plainPassword);
        if (!tenantDatabaseProvisioner.provision(tenant, plainPassword)) {
            return Item.failed(tenant, "建库或建账号失败，记录未改动（仍用实例账号路由），详见服务端日志");
        }
        // 账号已在实例上创建成功，这时才允许把记录指向它
        tenant.setSysTenantSqlPassword(tenantPasswordCipher.encrypt(plainPassword));
        if (sysTenantMapper.updateById(tenant) <= 0) {
            return Item.failed(tenant, "账号已创建但租户记录更新失败，请人工修正记录或重跑");
        }
        tenantDataSourceRegistry.evict(tenant.getSysTenantId());
        log.info("[租户补账号] 已为租户 {} 创建专用账号 {}（库 {}）",
                tenant.getSysTenantId(), username, dbName);
        return Item.created(tenant, username, "已创建并授权");
    }

    /**
     * 统计剩余候选数。
     *
     * @return 仍缺少专用账号的引用型租户数量
     */
    private long countRemaining() {
        Long count = sysTenantMapper.selectCount(Wrappers.<SysTenant>lambdaQuery()
                .isNotNull(SysTenant::getSysTenantDatasourceId)
                .ne(SysTenant::getSysTenantDatasourceId, 0L)
                .and(w -> w.isNull(SysTenant::getSysTenantSqlUsername)
                        .or().eq(SysTenant::getSysTenantSqlUsername, "")));
        return count == null ? 0L : count;
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 单个租户的处理结果。
     *
     * @param tenantId   租户标识
     * @param tenantCode 租户编码
     * @param dbName     库名
     * @param username   生成的账号名
     * @param status     PLANNED / CREATED / SKIPPED / FAILED
     * @param message    说明
     */
    public record Item(String tenantId, String tenantCode, String dbName,
                       String username, String status, String message) {

        private static Item planned(SysTenant tenant, String username, String message) {
            return new Item(tenant.getSysTenantId(), tenant.getSysTenantCode(), tenant.getSysTenantDbname(),
                    username, "PLANNED", message);
        }

        private static Item created(SysTenant tenant, String username, String message) {
            return new Item(tenant.getSysTenantId(), tenant.getSysTenantCode(), tenant.getSysTenantDbname(),
                    username, "CREATED", message);
        }

        private static Item skipped(SysTenant tenant, String message) {
            return new Item(tenant.getSysTenantId(), tenant.getSysTenantCode(), tenant.getSysTenantDbname(),
                    null, "SKIPPED", message);
        }

        private static Item failed(SysTenant tenant, String message) {
            return new Item(tenant.getSysTenantId(), tenant.getSysTenantCode(), tenant.getSysTenantDbname(),
                    tenant.getSysTenantSqlUsername(), "FAILED", message);
        }
    }

    /**
     * 本次执行结果。
     *
     * @param dryRun    是否为演练
     * @param items     逐租户结果
     * @param remaining 仍待处理的租户数
     * @param message   总结
     */
    public record Result(boolean dryRun, List<Item> items, long remaining, String message) {
    }
}
