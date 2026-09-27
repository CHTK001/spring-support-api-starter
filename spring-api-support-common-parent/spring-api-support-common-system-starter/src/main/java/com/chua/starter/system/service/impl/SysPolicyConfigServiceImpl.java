package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import com.chua.starter.system.service.SysPolicyConfigService;
import com.chua.starter.system.support.ApiRouteExecutor;
import com.chua.starter.system.support.IpPolicyCacheProjector;
import com.chua.starter.system.support.PolicyCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一策略配置服务实现
 *
 * <p>面向灰度(GRAY)、功能开关(FEATURE) 等无复杂领域逻辑的类型提供通用 CRUD。
 * 所有类型共享同一张表，类型之间靠 {@code type} 列区分。</p>
 *
 * <p><b>运行时缓存同步</b>：IP、账号名单、限流规则三类在运行时是 Spring Cache 投影，
 * 数据库只是权威数据源。通用 CRUD 必须与领域服务（{@code /v2/ip/save} 等）一样
 * 在事务提交后同步投影，否则会出现两类难查故障：</p>
 * <ul>
 *   <li>经本服务新增黑名单，库里写对了但运行时<b>不拦截</b>；</li>
 *   <li>经本服务删除黑名单，库里删干净了但运行时<b>继续拦截</b>，直到重启应用。</li>
 * </ul>
 * <p>灰度与功能开关不走运行时缓存，无需同步。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPolicyConfigServiceImpl extends ServiceImpl<SysPolicyConfigMapper, SysPolicyConfig>
        implements SysPolicyConfigService {

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    /**
     * IP 策略缓存投影器（自带 afterCommit 与启用态判定）
     */
    private final IpPolicyCacheProjector ipPolicyCacheProjector;

    /**
     * 接口停用 / 转发执行器
     */
    private final ApiRouteExecutor apiRouteExecutor;

    @Override
    public IPage<SysPolicyConfig> pageByType(String type, Query<SysPolicyConfig> query, SysPolicyConfig filter) {
        Page<SysPolicyConfig> page = new Page<>(query.getPage(), query.getPageSize());
        getBaseMapper().selectPage(page, Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(StringUtils.isNotBlank(type), SysPolicyConfig::getType, type)
                .like(null != filter && StringUtils.isNotBlank(filter.getBizKey()),
                        SysPolicyConfig::getBizKey, null == filter ? null : filter.getBizKey())
                .eq(null != filter && null != filter.getStatus(), SysPolicyConfig::getStatus,
                        null == filter ? null : filter.getStatus())
                .orderByAsc(SysPolicyConfig::getSort)
                .orderByDesc(SysPolicyConfig::getCreateTime));
        return page;
    }

    @Override
    public List<SysPolicyConfig> listByType(String type, Integer status) {
        return list(Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(StringUtils.isNotBlank(type), SysPolicyConfig::getType, type)
                .eq(null != status, SysPolicyConfig::getStatus, status)
                .orderByAsc(SysPolicyConfig::getSort)
                .orderByDesc(SysPolicyConfig::getCreateTime));
    }

    @Override
    public SysPolicyConfig getByBizKey(String type, String bizKey) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(bizKey)) {
            return null;
        }
        return getOne(Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(SysPolicyConfig::getType, type)
                .eq(SysPolicyConfig::getBizKey, bizKey)
                .last("limit 1"));
    }

    @Override
    public boolean existsUniqueKey(String type, String dimension, Integer listType, String bizKey) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(bizKey)) {
            return false;
        }
        return count(Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(SysPolicyConfig::getType, type)
                .eq(SysPolicyConfig::getDimension, null == dimension ? "" : dimension)
                .eq(SysPolicyConfig::getListType, listType)
                .eq(SysPolicyConfig::getBizKey, bizKey)) > 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysPolicyConfig> saveConfig(SysPolicyConfig config) {
        if (null == config) {
            return ReturnResult.illegal("记录不能为空");
        }
        if (StringUtils.isBlank(config.getType())) {
            return ReturnResult.illegal("策略类型不能为空");
        }
        if (StringUtils.isBlank(config.getBizKey())) {
            return ReturnResult.illegal("业务键不能为空");
        }
        normalize(config);
        // 判重必须按表唯一键（含 list_type），否则同一 IP/账号无法同时配黑名单与白名单
        if (existsUniqueKey(config.getType(), config.getDimension(), config.getListType(), config.getBizKey())) {
            return ReturnResult.illegal("该类型的业务键已存在");
        }
        if (1 != getBaseMapper().insert(config)) {
            return ReturnResult.error("保存失败");
        }
        syncRuntimeCache(null, config, false);
        return ReturnResult.ok(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateConfig(SysPolicyConfig config) {
        if (null == config || null == config.getSysPolicyConfigId()) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getById(config.getSysPolicyConfigId());
        if (null == previous) {
            return ReturnResult.illegal("记录不存在");
        }
        normalize(config);
        // 业务键/类型不允许被改写，否则唯一键与运行时投影都会指向别的配置
        config.setType(previous.getType());
        config.setBizKey(previous.getBizKey());
        if (1 != getBaseMapper().updateById(config)) {
            return ReturnResult.of(false);
        }
        syncRuntimeCache(previous, config, false);
        return ReturnResult.of(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteConfig(Long id) {
        if (null == id) {
            return ReturnResult.illegal("记录不存在");
        }
        if (null == getById(id)) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig existing = getById(id);
        if (1 != getBaseMapper().deleteById(id)) {
            return ReturnResult.of(false);
        }
        syncRuntimeCache(existing, null, true);
        return ReturnResult.of(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> toggleStatus(Long id, Integer status) {
        if (null == id) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig existing = getById(id);
        if (null == existing) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig update = new SysPolicyConfig();
        update.setSysPolicyConfigId(id);
        update.setStatus(null == status ? SysPolicyConfig.STATUS_DISABLED : status);
        if (1 != getBaseMapper().updateById(update)) {
            return ReturnResult.of(false);
        }
        // toggle 只带 id+status，而缓存投影需要 type/listType/bizKey/qps/config 等整行信息。
        // 这里直接复用 getById 查出的完整行、只覆盖 status：逐字段手抄极易漏掉类型特有列
        // （API_ROUTE 的 config 就漏过一次，漏了会把规则投影成空转的 PASS ——
        //  UI 显示已启用，实际什么都不拦）。
        // getById 返回的是新查出的游离对象，改它不会影响任何其它调用方。
        SysPolicyConfig merged = existing;
        merged.setStatus(update.getStatus());
        syncRuntimeCache(existing, merged, false);
        return ReturnResult.of(true);
    }

    @Override
    public Map<String, Long> countByType() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (String type : List.of(SysPolicyConfig.TYPE_IP, SysPolicyConfig.TYPE_RATE_LIMIT,
                SysPolicyConfig.TYPE_ACCOUNT, SysPolicyConfig.TYPE_GRAY, SysPolicyConfig.TYPE_FEATURE,
                SysPolicyConfig.TYPE_API_ROUTE)) {
            result.put(type, count(Wrappers.<SysPolicyConfig>lambdaQuery()
                    .eq(SysPolicyConfig::getType, type)));
        }
        return result;
    }

    /**
     * 补齐默认值：dimension 不可为 NULL（唯一键依赖），status/sort 缺省为启用/0。
     *
     * @param config 统一表行
     */
    private void normalize(SysPolicyConfig config) {
        if (null == config.getDimension()) {
            config.setDimension("");
        }
        if (null == config.getListType()) {
            config.setListType(SysPolicyConfig.LIST_NONE);
        }
        if (null == config.getStatus()) {
            config.setStatus(SysPolicyConfig.STATUS_ENABLED);
        }
        if (null == config.getSort()) {
            config.setSort(0);
        }
    }

    /* ==================== 运行时缓存同步 ==================== */

    /**
     * 事务提交后把改动同步到策略运行时缓存。
     *
     * <p>IP 走 {@link IpPolicyCacheProjector}（它自己处理 afterCommit、白/黑方向转换与启用态）；
     * 账号名单与限流规则在这里处理。灰度与功能开关每次都读库，不进运行时缓存，直接跳过。</p>
     *
     * <p>失败只记警告不抛异常：缓存是投影，权威数据在库里，回滚投影不该让写操作失败。</p>
     *
     * @param previous 旧记录，新增时为 null
     * @param current  新记录，删除时为 null
     * @param deleted  是否删除
     */
    private void syncRuntimeCache(SysPolicyConfig previous, SysPolicyConfig current, boolean deleted) {
        SysPolicyConfig probe = null != current ? current : previous;
        if (null == probe || !isRuntimeCached(probe.getType())) {
            return;
        }
        if (SysPolicyConfig.TYPE_IP.equals(probe.getType())) {
            ipPolicyCacheProjector.syncAfterCommit(current, previous, deleted);
            return;
        }
        if (SysPolicyConfig.TYPE_API_ROUTE.equals(probe.getType())) {
            apiRouteExecutor.syncAfterCommit(previous, deleted ? null : current);
            return;
        }
        String type = probe.getType();
        String bizKey = probe.getBizKey();
        runAfterCommit(() -> {
            try {
                if (null != previous) {
                    evictRuntimeCache(previous);
                }
                if (!deleted && isRuntimeEnabled(current)) {
                    projectRuntimeCache(current);
                }
            } catch (Exception e) {
                log.warn("[PolicyConfig] 同步策略运行时缓存失败: type={}, bizKey={}", type, bizKey, e);
            }
        });
    }

    /**
     * 该类型是否有运行时缓存投影
     *
     * @param type 策略类型
     * @return IP/ACCOUNT/RATE_LIMIT 返回 true
     */
    private boolean isRuntimeCached(String type) {
        return SysPolicyConfig.TYPE_IP.equals(type)
                || SysPolicyConfig.TYPE_ACCOUNT.equals(type)
                || SysPolicyConfig.TYPE_RATE_LIMIT.equals(type)
                || SysPolicyConfig.TYPE_API_ROUTE.equals(type);
    }

    /**
     * 统一表行是否应进入运行时缓存
     *
     * @param row 统一表行
     * @return 启用中返回 true
     */
    private boolean isRuntimeEnabled(SysPolicyConfig row) {
        return null != row && (null == row.getStatus() || row.getStatus() == SysPolicyConfig.STATUS_ENABLED);
    }

    /**
     * 把启用中的行投影进运行时缓存
     *
     * @param row 统一表行
     */
    private void projectRuntimeCache(SysPolicyConfig row) {
        if (SysPolicyConfig.TYPE_ACCOUNT.equals(row.getType())) {
            policyCacheService.putAccount(row.getListType(), row.getBizKey());
            return;
        }
        policyCacheService.putRateLimit(row.getDimension(), row.getBizKey(), row.getQps());
    }

    /**
     * 把行从运行时缓存里清掉
     *
     * <p>账号名单两个方向都要清：同一账号可能先被加黑名单再改成白名单，
     * 只清当前方向会留下另一方向的脏投影。</p>
     *
     * @param row 统一表行
     */
    private void evictRuntimeCache(SysPolicyConfig row) {
        if (SysPolicyConfig.TYPE_ACCOUNT.equals(row.getType())) {
            policyCacheService.evictAccount(SysPolicyConfig.LIST_BLACK, row.getBizKey());
            policyCacheService.evictAccount(SysPolicyConfig.LIST_WHITE, row.getBizKey());
            return;
        }
        if (SysPolicyConfig.TYPE_API_ROUTE.equals(row.getType())) {
            policyCacheService.evictApiRoute(row.getBizKey());
            return;
        }
        policyCacheService.evictRateLimit(row.getDimension(), row.getBizKey());
    }

    /**
     * 有事务就在提交后执行，没有事务就立即执行
     *
     * @param action 缓存动作
     */
    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
