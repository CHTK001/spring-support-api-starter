package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysAccountBlackwhite;

/**
 * 账号黑白名单服务接口
 *
 * <p>数据已并入统一策略配置表 {@code sys_firewall_config}（{@code type='ACCOUNT'}），
 * 本接口只负责账号名单的领域逻辑与策略缓存投影，不再对应独立的物理表。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysAccountBlackwhiteService {

    /**
     * 分页查询账号黑白名单
     *
     * @param query  分页参数
     * @param entity 查询条件(account/type/status)
     * @return 分页结果
     */
    IPage<SysAccountBlackwhite> page(Query<SysAccountBlackwhite> query, SysAccountBlackwhite entity);

    /**
     * 新增账号黑白名单（并投影到策略缓存）
     *
     * @param entity 实体
     * @return 新增后的实体
     */
    ReturnResult<SysAccountBlackwhite> saveAccount(SysAccountBlackwhite entity);

    /**
     * 更新账号黑白名单（并投影到策略缓存）
     *
     * @param entity 实体
     * @return 是否成功
     */
    ReturnResult<Boolean> updateAccount(SysAccountBlackwhite entity);

    /**
     * 删除账号黑白名单（并清理策略缓存）
     *
     * @param id 主键
     * @return 是否成功
     */
    ReturnResult<Boolean> deleteAccount(Long id);
}

