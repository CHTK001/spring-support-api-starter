package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.entity.system.SysRateLimitRule;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import com.chua.starter.system.service.SysRateLimitRuleService;
import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.PolicyConfigConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 限流规则服务实现
 *
 * <p>读写统一策略配置表 {@code sys_firewall_config}（{@code type='RATE_LIMIT'}），
 * 不再使用独立的限流规则表。规则变更后仍按原语义投影/清理 Spring Cache。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysRateLimitRuleServiceImpl implements SysRateLimitRuleService {

    /**
     * 默认 QPS
     */
    private static final int DEFAULT_QPS = 100;
    /**
     * 默认突发
     */
    private static final int DEFAULT_BURST = 200;
    /**
     * 启用
     */
    private static final int ENABLED = SysPolicyConfig.STATUS_ENABLED;

    /**
     * 统一策略配置映射器
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    @Override
    public List<SysRateLimitRule> listByDimension(String dimension) {
        return PolicyConfigConverter.toRateLimitList(sysPolicyConfigMapper.selectList(
                Wrappers.<SysPolicyConfig>lambdaQuery()
                        .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_RATE_LIMIT)
                        .eq(StringUtils.isNotBlank(dimension), SysPolicyConfig::getDimension, dimension)
                        .orderByDesc(SysPolicyConfig::getCreateTime)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysRateLimitRule> saveRule(SysRateLimitRule entity) {
        if (null == entity) {
            return ReturnResult.illegal("记录不能为空");
        }
        if (null == entity.getQps()) {
            entity.setQps(DEFAULT_QPS);
        }
        if (null == entity.getBurstSize()) {
            entity.setBurstSize(DEFAULT_BURST);
        }
        if (null == entity.getEnabled()) {
            entity.setEnabled(ENABLED);
        }
        SysPolicyConfig row = PolicyConfigConverter.fromRateLimit(entity);
        if (1 != sysPolicyConfigMapper.insert(row)) {
            return ReturnResult.error("保存失败");
        }
        entity.setSysRateLimitRuleId(row.getSysPolicyConfigId());
        syncToCache(row, null);
        return ReturnResult.ok(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateRule(SysRateLimitRule entity) {
        if (null == entity || null == entity.getSysRateLimitRuleId()) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getRow(entity.getSysRateLimitRuleId());
        if (null == previous) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig update = PolicyConfigConverter.fromRateLimitForUpdate(entity);
        update.setType(SysPolicyConfig.TYPE_RATE_LIMIT);
        if (null == update.getDimension() || update.getDimension().isBlank()) {
            update.setDimension(previous.getDimension());
        }
        if (null == update.getBizKey()) {
            update.setBizKey(previous.getBizKey());
        }
        if (null == update.getStatus()) {
            update.setStatus(previous.getStatus());
        }
        if (1 != sysPolicyConfigMapper.updateById(update)) {
            return ReturnResult.of(false);
        }
        syncToCache(getRow(entity.getSysRateLimitRuleId()), previous);
        return ReturnResult.of(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteRule(Long id) {
        if (null == id) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getRow(id);
        if (null == previous) {
            return ReturnResult.illegal("记录不存在");
        }
        if (1 != sysPolicyConfigMapper.deleteById(id)) {
            return ReturnResult.of(false);
        }
        evictCache(previous);
        return ReturnResult.of(true);
    }

    /**
     * 按主键读取统一表行。
     *
     * @param id 主键
     * @return 统一表行
     */
    private SysPolicyConfig getRow(Long id) {
        return sysPolicyConfigMapper.selectOne(Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(SysPolicyConfig::getSysPolicyConfigId, id)
                .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_RATE_LIMIT));
    }

    /**
     * 同步规则到 Spring Cache：启用则写入，禁用/变更则清理旧缓存。
     *
     * @param cur 新规则
     * @param old 旧规则(可能为 null)
     */
    private void syncToCache(SysPolicyConfig cur, SysPolicyConfig old) {
        try {
            if (null != old && (!old.getDimension().equals(cur.getDimension())
                    || !old.getBizKey().equals(cur.getBizKey()))) {
                evictCache(old);
            }
            if (null != cur.getStatus() && cur.getStatus() == ENABLED) {
                policyCacheService.putRateLimit(cur.getDimension(), cur.getBizKey(), cur.getQps());
            } else {
                evictCache(cur);
            }
        } catch (Exception e) {
            log.warn("同步限流规则到策略缓存失败: {}", cur, e);
        }
    }

    /**
     * 清理策略缓存中的规则。
     *
     * @param row 规则
     */
    private void evictCache(SysPolicyConfig row) {
        try {
            policyCacheService.evictRateLimit(row.getDimension(), row.getBizKey());
        } catch (Exception e) {
            log.warn("清理限流规则策略缓存失败: {}", row, e);
        }
    }
}
