package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysIpBlackwhite;

/**
 * IP黑白名单服务接口
 *
 * <p>数据已并入统一策略配置表 {@code sys_policy_config}（{@code type='IP'}），
 * 本接口只负责 IP 名单的领域逻辑（CIDR 归一、缓存投影、按 IP 判定放行），
 * 不再对应独立的物理表。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
public interface SysIpBlackwhiteService {

    /**
     * 分页查询IP黑白名单
     *
     * @param query 分页参数
     * @param entity 查询条件(type/ip/status)
     * @return 分页结果
     */
    IPage<SysIpBlackwhite> page(Query<SysIpBlackwhite> query, SysIpBlackwhite entity);

    /**
     * 新增IP黑白名单
     *
     * @param entity 实体
     * @return 新增后的实体
     */
    ReturnResult<SysIpBlackwhite> saveIp(SysIpBlackwhite entity);

    /**
     * 更新IP黑白名单
     *
     * @param entity 实体
     * @return 是否成功
     */
    ReturnResult<Boolean> updateIp(SysIpBlackwhite entity);

    /**
     * 删除IP黑白名单
     *
     * @param ipId 主键
     * @return 是否成功
     */
    ReturnResult<Boolean> deleteIp(Integer ipId);

    /**
     * 校验IP是否允许访问
     *
     * <p>规则：命中启用的黑名单则拒绝；其余一律放行。</p>
     *
     * <p><b>白名单不是准入名单</b>：它的作用是让该 IP 跳过限流与访问拦截
     * （见 {@code PolicyRateLimitFilter}），不改变「未列入名单的 IP 默认放行」这一基线。
     * 早期实现曾写成「存在白名单则拒绝所有未列入的 IP」，与限流路径语义冲突，
     * 且配一条白名单就会导致其余 IP 全被拒，已移除。</p>
     *
     * @param ip 客户端IP
     * @return true=允许, false=拒绝
     */
    boolean checkIp(String ip);
}
