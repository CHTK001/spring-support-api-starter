package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysRateLimitRule;

import java.util.List;

/**
 * 限流规则服务接口
 *
 * <p>数据已并入统一策略配置表 {@code sys_firewall_config}（{@code type='RATE_LIMIT'}），
 * 本接口只负责限流规则的领域逻辑与策略缓存投影，不再对应独立的物理表。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysRateLimitRuleService {

    /**
     * 按维度查询规则列表
     *
     * @param dimension 维度(可空)
     * @return 规则列表
     */
    List<SysRateLimitRule> listByDimension(String dimension);

    /**
     * 新增规则（并投影到策略缓存）
     *
     * @param entity 规则
     * @return 保存后的实体
     */
    ReturnResult<SysRateLimitRule> saveRule(SysRateLimitRule entity);

    /**
     * 更新规则（并投影到策略缓存）
     *
     * @param entity 规则
     * @return 是否成功
     */
    ReturnResult<Boolean> updateRule(SysRateLimitRule entity);

    /**
     * 删除规则（并清理策略缓存）
     *
     * @param id 主键
     * @return 是否成功
     */
    ReturnResult<Boolean> deleteRule(Long id);
}

