package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;
import java.util.Map;

/**
 * 统一策略配置服务接口
 *
 * <p>{@code sys_policy_config} 是所有策略类型的唯一存储，本接口提供与类型无关的通用读写，
 * 供灰度(GRAY)、功能开关(FEATURE) 等新增类型直接使用；
 * IP/限流/账号名单因各自带有领域逻辑（CIDR 归一、缓存投影），
 * 仍由 {@link SysIpBlackwhiteService} / {@link SysRateLimitRuleService} /
 * {@link SysAccountBlackwhiteService} 承担，但底层同样落到本表。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysPolicyConfigService {

    /**
     * 按类型分页查询。
     *
     * @param type   策略类型
     * @param query  分页参数
     * @param filter 过滤条件（bizKey 模糊、status 精确），可为 null
     * @return 分页结果
     */
    IPage<SysPolicyConfig> pageByType(String type, Query<SysPolicyConfig> query, SysPolicyConfig filter);

    /**
     * 按类型查询全部。
     *
     * @param type   策略类型
     * @param status 状态，null 表示不限
     * @return 统一表行列表
     */
    List<SysPolicyConfig> listByType(String type, Integer status);

    /**
     * 按类型与业务键查询单条。
     *
     * @param type   策略类型
     * @param bizKey 业务键
     * @return 统一表行
     */
    SysPolicyConfig getByBizKey(String type, String bizKey);

    /**
     * 按表唯一键判断是否已存在。
     *
     * <p>唯一键是 {@code (type, dimension, list_type, biz_key)}，
     * <b>含名单方向 list_type</b>。新增时必须用本方法判重，不能用
     * {@link #getByBizKey(String, String)}：后者只看 type+bizKey，
     * 会把「同一 IP / 同一账号同时配黑名单和白名单」这种合法组合误判为重复。
     * 而黑白并存是常见运维动作（先拉黑、后加白豁免）。</p>
     *
     * @param type     策略类型
     * @param dimension 维度，非限流类填空串
     * @param listType 名单方向，1黑 2白，非名单类填 0
     * @param bizKey   业务键
     * @return 已存在返回 true
     */
    boolean existsUniqueKey(String type, String dimension, Integer listType, String bizKey);

    /**
     * 新增一条策略配置。
     *
     * @param config 统一表行
     * @return 保存后的行（含自增主键）
     */
    ReturnResult<SysPolicyConfig> saveConfig(SysPolicyConfig config);

    /**
     * 更新一条策略配置。
     *
     * @param config 统一表行
     * @return 是否成功
     */
    ReturnResult<Boolean> updateConfig(SysPolicyConfig config);

    /**
     * 删除一条策略配置。
     *
     * @param id 主键
     * @return 是否成功
     */
    ReturnResult<Boolean> deleteConfig(Long id);

    /**
     * 切换启用状态。
     *
     * @param id     主键
     * @param status 目标状态 1启用 0禁用
     * @return 是否成功
     */
    ReturnResult<Boolean> toggleStatus(Long id, Integer status);

    /**
     * 统计各类型的配置条数。
     *
     * @return 类型 → 条数
     */
    Map<String, Long> countByType();
}
