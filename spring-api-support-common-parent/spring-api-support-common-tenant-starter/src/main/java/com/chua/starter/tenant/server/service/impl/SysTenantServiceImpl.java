package com.chua.starter.tenant.server.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;
import com.chua.starter.tenant.entity.SysTenantStats;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.mapper.SysTenantServiceMapper;
import com.chua.starter.tenant.server.pojo.SysTenantBindServiceV1Request;
import com.chua.starter.tenant.server.sync.TenantServerSyncPublisher;
import com.chua.starter.tenant.server.support.TenantDatabaseInitializer;
import com.chua.starter.tenant.server.support.TenantDatabaseAdminSynchronizer;
import com.chua.starter.tenant.server.support.TenantDatabaseProvisioner;
import com.chua.starter.tenant.server.support.TenantDataSourceRegistry;
import com.chua.starter.tenant.server.support.TenantPasswordCipher;
import com.chua.starter.tenant.server.support.TenantServiceCatalogSynchronizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 租户服务实现类
 *
 * @since 2024/12/06
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant>
        implements com.chua.starter.tenant.server.service.SysTenantService {

    /**
     * 租户编码合法形态：字母数字开头，允许下划线/中划线，长度 2-32。
     *
     * <p>限制成这个形状是为了它同时能当 URL 片段、Redis key 片段、日志里的检索词，
     * 不出现需要转义的字符。</p>
     */
    private static final Pattern TENANT_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]{1,31}$");

    /**
     * 平台保留的租户编码：登录时这些词要表达「平台自身」，不能让某个租户占掉
     */
    private static final Set<String> TENANT_CODE_RESERVED =
            Set.of("master", "platform", "system", "admin", "root", "default", "public", "tenant");

    /**
     * Transaction模板
    */
    private final TransactionTemplate transactionTemplate;
    /**
     * systenant服务映射器
    */
    private final SysTenantServiceMapper sysTenantServiceMapper;

    /**
     * 租户口令落库加解密
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 租户独立库自动开通器
     */
    private final TenantDatabaseProvisioner tenantDatabaseProvisioner;

    /**
     * 租户独立库业务表初始化器
     */
    private final TenantDatabaseInitializer tenantDatabaseInitializer;

    /**
     * 租户库管理员同步器：把平台租户账号写进租户库的 {@code sys_user}
     */
    private final TenantDatabaseAdminSynchronizer tenantDatabaseAdminSynchronizer;

    /**
     * 租户数据源注册器
     */
    private final TenantDataSourceRegistry tenantDataSourceRegistry;

    /**
     * 租户库服务目录同步器：绑定变化后把有效服务写进租户自己的库
     */
    private final TenantServiceCatalogSynchronizer tenantServiceCatalogSynchronizer;

    @Autowired(required = false)
    /**
     * 同步publisher
    */
    private TenantServerSyncPublisher syncPublisher;

    @Override
    /**
     * pageforsystenant
    */
    public IPage<SysTenant> pageForSysTenant(Query<SysTenant> query, SysTenant sysTenant) {
        log.debug("分页查询租户, 条件: {}", sysTenant);
        IPage<SysTenant> page = baseMapper.selectPage(query.createPage(),
                Wrappers.<SysTenant>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantName()),
                                SysTenant::getSysTenantName, sysTenant.getSysTenantName())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantCode()),
                                SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantPhone()),
                                SysTenant::getSysTenantPhone, sysTenant.getSysTenantPhone())
                        .eq(sysTenant.getSysTenantStatus() != null,
                                SysTenant::getSysTenantStatus, sysTenant.getSysTenantStatus())
                        .orderByDesc(SysTenant::getCreateTime)
        );

        List<SysTenant> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            fillTenantServices(records);
        }
        return page;
    }

    @Override
    /**
     * 列表forsystenant
    */
    public ReturnResult<List<SysTenant>> listForSysTenant(SysTenant sysTenant) {
        log.debug("查询租户列表, 条件: {}", sysTenant);
        List<SysTenant> records = baseMapper.selectList(
                Wrappers.<SysTenant>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantName()),
                                SysTenant::getSysTenantName, sysTenant.getSysTenantName())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantCode()),
                                SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                        .eq(sysTenant.getSysTenantStatus() != null,
                                SysTenant::getSysTenantStatus, sysTenant.getSysTenantStatus())
                        .orderByDesc(SysTenant::getCreateTime)
        );

        if (CollectionUtils.isNotEmpty(records)) {
            fillTenantServices(records);
        }
        return ReturnResult.ok(records);
    }

    @Override
    /**
     * 保存forsystenant
     *
     * @param sysTenant sysTenant
     * @return 保存forsystenant的结果
     */
    public ReturnResult<SysTenant> saveForSysTenant(SysTenant sysTenant) {
        log.debug("保存租户: {}", sysTenant);

        ReturnResult<SysTenant> saved = saveRecordForSysTenant(sysTenant);
        if (!saved.isOk()) {
            return saved;
        }
        SysTenant record = saved.getData();

        // 明文口令在这里从落库密文还原：建库建账号、建租户库管理员都要明文，
        // 又不能让明文在库里存在，所以「加密落库 → 需要时解密」是唯一安全的取法
        String plainSqlPassword = tenantPasswordCipher.decrypt(record.getSysTenantSqlPassword());
        String plainTenantPassword = tenantPasswordCipher.decrypt(record.getSysTenantPassword());

        // 独立库开通：配置了管理连接才执行，失败只记日志，不回滚租户
        provisionTenantDatabase(record, plainSqlPassword);

        // 租户库管理员：把平台租户账号同步进租户库的 sys_user。
        // 与建库分开调用 —— 手工登记库地址（不自动建库）的租户同样需要这条账号
        provisionTenantDatabaseAdmin(record, plainTenantPassword);

        // 发布同步消息：推送给租户端租户信息，并按当前订阅重置租户端菜单
        if (syncPublisher != null) {
            syncPublisher.publishTenantUpdate(record);
        }
        resetMenuForSysTenant(record.getSysTenantId());

        return ReturnResult.ok(record);
    }

    @Override
    /**
     * 只保存租户记录（校验 + 口令加密落库），不做建库/建表/管理员/菜单。
     *
     * <p>拆出来是给「创建进度」编排用的：进度弹窗要把「保存记录」「建库」「建表」
     * 「同步管理员」「推送菜单」分别显示成一个步骤，合并成一次调用就只能显示「进行中」
     * 而说不出进行到哪一步。</p>
     *
     * @param sysTenant 租户对象
     * @return 保存结果（含生成的租户标识）
     */
    public ReturnResult<SysTenant> saveRecordForSysTenant(SysTenant sysTenant) {
        log.debug("保存租户记录: {}", sysTenant);

        // 租户编码由前端填写：它是登录时要人肉输入的短码，不能再拿雪花主键顶替
        String tenantCodeError = validateTenantCode(sysTenant.getSysTenantCode(), null);
        if (tenantCodeError != null) {
            log.warn("租户编码不可用: code={}, reason={}", sysTenant.getSysTenantCode(), tenantCodeError);
            return ReturnResult.error(tenantCodeError);
        }
        sysTenant.setSysTenantCode(normalizeTenantCode(sysTenant.getSysTenantCode()));

        // 检查账号是否已存在
        if (StringUtils.isNotBlank(sysTenant.getSysTenantUsername())) {
            if (exists(Wrappers.<SysTenant>lambdaQuery()
                    .eq(SysTenant::getSysTenantUsername, sysTenant.getSysTenantUsername()))) {
                log.warn("租户账号已存在: {}", sysTenant.getSysTenantUsername());
                return ReturnResult.error("租户账号已存在");
            }
        }

        // 设置默认值
        sysTenant.setSysTenantDelete(0);
        if (sysTenant.getSysTenantStatus() == null) {
            sysTenant.setSysTenantStatus(0);
        }

        if (StringUtils.isBlank(sysTenant.getSysTenantGid())) {
            sysTenant.setSysTenantGid(IdUtils.createSimpleUuid());
        }

        // 落库前加密，避免新建租户时明文直接进库；
        // 下发同步时由 TenantServerSyncPublisher 解密还原，线格式不变
        sysTenant.setSysTenantPassword(tenantPasswordCipher.encrypt(sysTenant.getSysTenantPassword()));
        // 租户独立库口令同样密文落库，供 TenantDataSourceRegistry 建池时还原
        sysTenant.setSysTenantSqlPassword(tenantPasswordCipher.encrypt(sysTenant.getSysTenantSqlPassword()));

        save(sysTenant);

        log.info("租户记录已保存, ID: {}, 编码: {}", sysTenant.getSysTenantId(), sysTenant.getSysTenantCode());
        return ReturnResult.ok(sysTenant);
    }

    /**
     * 归一化租户编码：去掉首尾空白并转小写。
     *
     * <p>登录页是人工输入，大小写与空格不该成为「密码对了但登不进去」的原因。</p>
     *
     * @param tenantCode 原始编码
     * @return 归一化后的编码，入参为空时返回 {@code null}
     */
    private String normalizeTenantCode(String tenantCode) {
        return (tenantCode == null) ? null : tenantCode.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 校验租户编码是否可用。
     *
     * @param tenantCode     原始编码
     * @param excludeTenantId 更新场景下要排除的租户标识，新增传 {@code null}
     * @return 不可用时返回给用户看的原因，可用时返回 {@code null}
     */
    private String validateTenantCode(String tenantCode, String excludeTenantId) {
        String code = normalizeTenantCode(tenantCode);
        if (StringUtils.isBlank(code)) {
            return "租户编码不能为空";
        }
        if (!TENANT_CODE_PATTERN.matcher(code).matches()) {
            return "租户编码只能用字母、数字、下划线和中划线，长度 2-32";
        }
        if (TENANT_CODE_RESERVED.contains(code)) {
            return "租户编码「" + code + "」是平台保留字，请换一个";
        }
        boolean exists = exists(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getSysTenantCode, code)
                .ne(StringUtils.isNotBlank(excludeTenantId), SysTenant::getSysTenantId, excludeTenantId));
        return exists ? "租户编码「" + code + "」已被占用" : null;
    }

    @Override
    /**
     * 更新forsystenant
     *
     * @param sysTenant sysTenant
     * @return 更新forsystenant的结果
     */
    public ReturnResult<Boolean> updateForSysTenant(SysTenant sysTenant) {
        log.debug("更新租户: {}", sysTenant);

        // 编码允许修改（写错字的租户要能纠正），但必须重新校验唯一性
        sysTenant.setSysTenantCode(normalizeTenantCode(sysTenant.getSysTenantCode()));
        if (sysTenant.getSysTenantCode() != null) {
            String tenantCodeError = validateTenantCode(sysTenant.getSysTenantCode(), sysTenant.getSysTenantId());
            if (tenantCodeError != null) {
                log.warn("租户编码不可用: code={}, reason={}", sysTenant.getSysTenantCode(), tenantCodeError);
                return ReturnResult.error(tenantCodeError);
            }
        }

        if (StringUtils.isNotBlank(sysTenant.getSysTenantUsername())) {
            if (exists(Wrappers.<SysTenant>lambdaQuery()
                    .eq(SysTenant::getSysTenantUsername, sysTenant.getSysTenantUsername())
                    .ne(SysTenant::getSysTenantId, sysTenant.getSysTenantId()))) {
                log.warn("租户账号已存在: {}", sysTenant.getSysTenantUsername());
                return ReturnResult.error("租户账号已存在");
            }
        }

        sysTenant.setSysTenantDelete(null);

        // 口令非空才加密；空表示「不改口令」，置空让 MyBatis-Plus 跳过该列，
        // 防止把已有密文覆盖成 null
        String plainTenantPassword = null;
        if (StringUtils.isNotBlank(sysTenant.getSysTenantPassword())) {
            plainTenantPassword = sysTenant.getSysTenantPassword();
            sysTenant.setSysTenantPassword(tenantPasswordCipher.encrypt(plainTenantPassword));
        }
        if (StringUtils.isNotBlank(sysTenant.getSysTenantSqlPassword())) {
            sysTenant.setSysTenantSqlPassword(tenantPasswordCipher.encrypt(sysTenant.getSysTenantSqlPassword()));
        }

        boolean result = updateById(sysTenant);
        log.info("租户更新结果: {}", result);

        // 库地址可能被改：先释放旧连接池，下次请求按新地址重建
        if (result) {
            tenantDataSourceRegistry.evict(sysTenant.getSysTenantId());
        }

        // 发布同步消息：推送给租户端租户信息，并按当前订阅重置租户端菜单
        if (result && syncPublisher != null) {
            SysTenant updated = getById(sysTenant.getSysTenantId());
            syncPublisher.publishTenantUpdate(updated);
        }
        // 平台侧改了口令就必须同步租户库管理员，否则「平台改完、租户库还是旧密码」
        if (result && plainTenantPassword != null) {
            SysTenant updated = getById(sysTenant.getSysTenantId());
            provisionTenantDatabaseAdmin(updated, plainTenantPassword);
        }
        if (result) {
            resetMenuForSysTenant(sysTenant.getSysTenantId());
        }

        return (result) ? ReturnResult.ok(true) : ReturnResult.error("操作失败");
    }

    @Override
    /**
     * 删除forsystenant
    */
    public ReturnResult<Boolean> deleteForSysTenant(String sysTenantId) {
        log.debug("删除租户, ID: {}", sysTenantId);
        // 独立库信息只在删除前拿得到：记录被逻辑删除后仍能查，但读一下更直观
        SysTenant deleting = getById(sysTenantId);

        ReturnResult<Boolean> result = transactionTemplate.execute(status -> {
            sysTenantServiceMapper.delete(Wrappers.<SysTenantService>lambdaQuery()
                    .eq(SysTenantService::getSysTenantId, sysTenantId));

            boolean removed = removeById(sysTenantId);
            log.info("租户删除结果: {}", removed);

            // 连接池随租户一起释放，避免删除后仍持有已废弃库的连接
            if (removed) {
                tenantDataSourceRegistry.evict(sysTenantId);
            }

            // 发布同步消息
            if (removed && syncPublisher != null) {
                syncPublisher.publishTenantDelete(sysTenantId);
            }

            return (removed) ? ReturnResult.ok(true) : ReturnResult.error("操作失败");
        });

        // 物理删库必须在事务提交之后：DROP DATABASE 是隐式提交的 DDL，
        // 放进事务里会出现「库删了、平台记录因回滚还在」的错位
        if (result != null && Boolean.TRUE.equals(result.getData()) && deleting != null) {
            tenantDatabaseProvisioner.destroy(deleting);
        }
        return result;
    }

    @Override
    /**
     * 获取forsystenant
    */
    public ReturnResult<SysTenant> getForSysTenant(String sysTenantId) {
        log.debug("获取租户详情, ID: {}", sysTenantId);
        SysTenant sysTenant = getById(sysTenantId);
        if (sysTenant == null) {
            return ReturnResult.error("租户不存在");
        }

        fillTenantServices(List.of(sysTenant));
        return ReturnResult.ok(sysTenant);
    }

    @Override
    /**
     * 绑定服务forsystenant
    */
    public ReturnResult<Boolean> bindServiceForSysTenant(SysTenantBindServiceV1Request request) {
        log.debug("绑定租户服务, 租户ID: {}, 服务数量: {}",
                request.sysTenantId(),
                request.serviceBindItems() != null ? request.serviceBindItems().size() : 0);

        ReturnResult<Boolean> result = transactionTemplate.execute(status -> {
            sysTenantServiceMapper.delete(Wrappers.<SysTenantService>lambdaQuery()
                    .eq(SysTenantService::getSysTenantId, request.sysTenantId()));

            if (CollectionUtils.isNotEmpty(request.serviceBindItems())) {
                List<SysTenantService> tenantServices = request.serviceBindItems().stream()
                        .map(item -> {
                            SysTenantService tenantService = new SysTenantService();
                            tenantService.setSysTenantId(request.sysTenantId());
                            tenantService.setSysServiceId(item.sysServiceId());
                            tenantService.setSysTenantServiceValidTime(item.validTime());
                            return tenantService;
                        })
                        .toList();

                for (SysTenantService tenantService : tenantServices) {
                    sysTenantServiceMapper.insert(tenantService);
                }
                log.info("租户服务绑定成功, 绑定数量: {}", tenantServices.size());

            }

            if (syncPublisher != null) {
                List<Integer> menuIds = sysTenantServiceMapper.getMenuByTenantId(request.sysTenantId());
                syncPublisher.publishServiceUpdate(request.sysTenantId(), menuIds);
            }
            return ReturnResult.ok();
        });

        // 绑定关系提交后再动租户库：独立库写失败不能回滚已经确认的绑定，
        // 同步失败只记日志，下一次绑定/到期清理会再对齐一次
        SysTenant boundTenant = getById(request.sysTenantId());
        if (boundTenant != null) {
            tenantServiceCatalogSynchronizer.sync(boundTenant);
        }
        return result;
    }

    @Override
    /**
     * 更新状态forsystenant
    */
    public ReturnResult<Boolean> updateStatusForSysTenant(String sysTenantId, Integer status) {
        log.debug("更新租户状态, ID: {}, 状态: {}", sysTenantId, status);

        boolean result = update(Wrappers.<SysTenant>lambdaUpdate()
                .set(SysTenant::getSysTenantStatus, status)
                .eq(SysTenant::getSysTenantId, sysTenantId));

        log.info("租户状态更新结果: {}", result);

        // 发布同步消息
        if (result && syncPublisher != null) {
            SysTenant tenant = getById(sysTenantId);
            syncPublisher.publishTenantUpdate(tenant);
        }

        return (result) ? ReturnResult.ok(true) : ReturnResult.error("操作失败");
    }

    @Override
    /**
     * statsforsystenant
    */
    public ReturnResult<SysTenantStats> statsForSysTenant() {
        log.debug("获取租户统计数据");
        // 总数
        Long total = count();
        // 启用数 (status = 0 表示启用)
        Long enabled = count(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantStatus, 0));
        // 禁用数 (status = 1 表示禁用)
        Long disabled = count(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantStatus, 1));
        // 订阅服务数（有订阅服务的租户数）
        List<SysTenantService> allTenantServices = sysTenantServiceMapper.selectList(
                Wrappers.<SysTenantService>lambdaQuery().select(SysTenantService::getSysTenantId)
        );
        long subscribedCount = allTenantServices.stream()
                .map(SysTenantService::getSysTenantId)
                .distinct()
                .count();
        SysTenantStats stats = new SysTenantStats(total, enabled, disabled, subscribedCount);
        return ReturnResult.ok(stats);
    }

    @Override
    /**
     * 重置密码forsystenant
    */
    public ReturnResult<Boolean> resetPasswordForSysTenant(String sysTenantId, String sysTenantPassword) {
        if (StringUtils.isBlank(sysTenantId)) {
            return ReturnResult.illegal("租户ID不能为空");
        }
        if (StringUtils.isBlank(sysTenantPassword)) {
            return ReturnResult.illegal("新密码不能为空");
        }

        SysTenant tenant = getById(sysTenantId);
        if (tenant == null) {
            return ReturnResult.error("租户不存在");
        }

        boolean result = update(Wrappers.<SysTenant>lambdaUpdate()
                // 落库存密文，不存明文；下发时由 TenantPasswordCipher 解密还原，
                // 同步线格式仍是明文，租户端无需改动
                .set(SysTenant::getSysTenantPassword, tenantPasswordCipher.encrypt(sysTenantPassword))
                .eq(SysTenant::getSysTenantId, sysTenantId));
        log.info("租户密码重置结果: tenantId={}, result={}", sysTenantId, result);

        // 将重置后的密码推送给租户端，租户端同步更新登录账号密码
        if (result && syncPublisher != null) {
            SysTenant updated = getById(sysTenantId);
            syncPublisher.publishTenantUpdate(updated);
        }

        // 同一个口令还要落进租户库的管理员账号：租户端登录比对的是租户库 sys_user，
        // 只改平台这张表会表现为「平台显示已重置、租户还是登不上」
        if (result) {
            SysTenant updated = getById(sysTenantId);
            provisionTenantDatabaseAdmin(updated, sysTenantPassword);
        }

        return (result) ? ReturnResult.ok(true) : ReturnResult.error("操作失败");
    }

    @Override
    /**
     * 重置menuforsystenant
    */
    public void resetMenuForSysTenant(String sysTenantId) {
        if (syncPublisher == null || StringUtils.isBlank(sysTenantId)) {
            return;
        }

        // 按租户当前订阅的服务重新计算菜单并全量推送给租户端
        List<Integer> menuIds = sysTenantServiceMapper.getMenuByTenantId(sysTenantId);
        syncPublisher.publishServiceUpdate(sysTenantId, menuIds);
        log.info("租户菜单重置完成并推送, tenantId={}, menuCount={}", sysTenantId,
                menuIds == null ? 0 : menuIds.size());
    }

    /**
     * 开通租户独立库并初始化业务表。
     *
     * <p>只在 {@code datasource-routing-auto-provision=true} 且配好管理连接时才会真正建库；
     * 其它情况直接跳过。任何失败都只记日志——租户记录已经落库，不能因为建库失败让接口报错。</p>
     *
     * <p>建库成功后紧接着初始化业务表：只建空库等于把「表不存在」推迟到租户第一次请求时才暴露，
     * 而且从平台这边看不出是建库漏了一步。初始化同样只记录结果，不回滚租户记录；
     * 需要补做时调 {@code PUT /v2/tenant/init-database}。</p>
     *
     * @param tenant            租户记录（{@code sqlPassword} 已是密文）
     * @param plainSqlPassword  落库前捕获的明文库口令，用于建账号
     */
    private void provisionTenantDatabase(SysTenant tenant, String plainSqlPassword) {
        if (!tenantDatabaseProvisioner.isAvailable()) {
            return;
        }
        boolean created;
        try {
            created = tenantDatabaseProvisioner.provision(tenant, plainSqlPassword);
        } catch (Exception e) {
            log.error("[租户建库] 租户 {} 开通独立库异常，不影响租户记录: {}",
                    tenant.getSysTenantId(), e.getMessage(), e);
            return;
        }
        if (!created) {
            // 建库失败/跳过时不去连库：既没有库也可能是地址写错，硬连只会多留一条误导性错误
            return;
        }
        initializeTenantDatabase(tenant, plainSqlPassword);
    }

    /**
     * 初始化租户库业务表。
     *
     * <p>失败只记日志：库与租户记录都已就绪，初始化是可重做的补充动作。</p>
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 明文库口令，可为空（空则从记录解密）
     */
    private void initializeTenantDatabase(SysTenant tenant, String plainSqlPassword) {
        try {
            TenantDatabaseInitializer.InitResult result =
                    tenantDatabaseInitializer.initialize(tenant, plainSqlPassword);
            if (result.isSuccess()) {
                log.info("[租户建表] 租户 {} 业务表就绪: {}", tenant.getSysTenantId(), result.message());
            } else {
                log.warn("[租户建表] 租户 {} 业务表未初始化[{}]: {}",
                        tenant.getSysTenantId(), result.status(), result.message());
            }
        } catch (Exception e) {
            log.error("[租户建表] 租户 {} 初始化业务表异常，不影响租户记录: {}",
                    tenant.getSysTenantId(), e.getMessage(), e);
        }
    }

    /**
     * 同步租户库管理员账号。
     *
     * <p>失败只记日志：租户记录与库都已就绪，管理员账号是可重做的补充动作，
     * 不能因为「租户库还没就绪」把建租户接口打成 500。</p>
     *
     * @param tenant          租户记录
     * @param plainPassword   租户明文口令
     */
    private void provisionTenantDatabaseAdmin(SysTenant tenant, String plainPassword) {
        if (tenant == null) {
            return;
        }
        try {
            TenantDatabaseAdminSynchronizer.Result result =
                    tenantDatabaseAdminSynchronizer.sync(tenant, plainPassword);
            if (result.isSynced()) {
                log.info("[租户库管理员] 租户 {} {}", tenant.getSysTenantId(), result.message());
            } else {
                log.warn("[租户库管理员] 租户 {} 未同步[{}]: {}",
                        tenant.getSysTenantId(), result.status(), result.message());
            }
        } catch (Exception e) {
            log.error("[租户库管理员] 租户 {} 同步异常，不影响租户记录: {}",
                    tenant.getSysTenantId(), e.getMessage(), e);
        }
    }

    /**
     * 初始化（或补齐）租户独立库的业务表。
     *
     * <p>两种情况都要走这里：建租户时脚本失败（库建好了但表没建全），以及存量租户库升级后
     * 需要补跑新增的 {@code V1.x__alter_xxx.sql}。租户库自己的 {@code sys_database_version}
     * 记录了已执行版本，所以重复调用只会执行没跑过的脚本。</p>
     *
     * @param sysTenantId 租户标识
     * @return 初始化结果说明
     */
    @Override
    public ReturnResult<String> initTenantDatabase(String sysTenantId) {
        if (StringUtils.isBlank(sysTenantId)) {
            return ReturnResult.illegal("租户ID不能为空");
        }
        SysTenant tenant = getById(sysTenantId);
        if (tenant == null) {
            return ReturnResult.error("租户不存在: " + sysTenantId);
        }
        log.info("[租户建表] 手工触发业务表初始化: tenantId={}", sysTenantId);
        TenantDatabaseInitializer.InitResult result = tenantDatabaseInitializer.initialize(tenant);
        if (result.isSuccess()) {
            // 表就绪后顺手补一次管理员账号：存量租户库绝大多数就是「表有了、管理员没有」
            TenantDatabaseAdminSynchronizer.Result admin = tenantDatabaseAdminSynchronizer.sync(tenant);
            return ReturnResult.ok(result.message() + "；租户库管理员：" + admin.message());
        }
        if (result.status() == TenantDatabaseInitializer.InitResult.Status.SKIPPED) {
            return ReturnResult.error(result.message());
        }
        return ReturnResult.error(result.message());
    }

    /**
     * 同步租户库管理员（给存量租户补做 / 运维手工修复用）。
     *
     * @param sysTenantId 租户标识
     * @return 同步结果说明
     */
    @Override
    public ReturnResult<String> syncTenantDatabaseAdmin(String sysTenantId) {
        if (StringUtils.isBlank(sysTenantId)) {
            return ReturnResult.illegal("租户ID不能为空");
        }
        SysTenant tenant = getById(sysTenantId);
        if (tenant == null) {
            return ReturnResult.error("租户不存在: " + sysTenantId);
        }
        TenantDatabaseAdminSynchronizer.Result result = tenantDatabaseAdminSynchronizer.sync(tenant);
        if (result.isSynced()) {
            return ReturnResult.ok(result.message());
        }
        return ReturnResult.error(result.message());
    }

    /**
     * 填充租户的关联服务信息
     *
     * @param records 租户列表
     */
    private void fillTenantServices(List<SysTenant> records) {
        List<String> tenantIds = records.stream()
                .map(SysTenant::getSysTenantId)
                .toList();

        List<SysTenantService> tenantServices = sysTenantServiceMapper.selectList(
                Wrappers.<SysTenantService>lambdaQuery()
                        .in(SysTenantService::getSysTenantId, tenantIds));

        Map<String, List<SysTenantService>> serviceMap = tenantServices.stream()
                .collect(Collectors.groupingBy(SysTenantService::getSysTenantId));

        for (SysTenant record : records) {
            List<SysTenantService> services = serviceMap.getOrDefault(record.getSysTenantId(), List.of());
            record.setSysTenantService(services);
            record.setServiceIds(services.stream()
                    .map(SysTenantService::getSysServiceId)
                    .toList());
        }
    }
}
