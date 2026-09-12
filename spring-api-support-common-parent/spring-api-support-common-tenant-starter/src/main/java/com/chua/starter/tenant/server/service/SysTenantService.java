package com.chua.starter.tenant.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantStats;
import com.chua.starter.tenant.server.pojo.SysTenantBindServiceV1Request;

import java.util.List;

/**
 * 租户服务接口
 *
   * @版本 1.0.0
 * @since 2024/12/06
 * @author CH
 */
public interface SysTenantService extends IService<SysTenant> {

    /**
     * 分页查询租户
     *
     * @param query     查询对象
     * @param sysTenant 租户条件
     * @return 分页结果
     */
    IPage<SysTenant> pageForSysTenant(Query<SysTenant> query, SysTenant sysTenant);

    /**
     * 查询租户列表
     *
     * @param sysTenant 租户条件
     * @return 租户列表
     */
    ReturnResult<List<SysTenant>> listForSysTenant(SysTenant sysTenant);

    /**
     * 保存租户
     *
     * @param sysTenant 租户对象
     * @return 保存结果
     */
    ReturnResult<SysTenant> saveForSysTenant(SysTenant sysTenant);

    /**
     * 更新租户
     *
     * @param sysTenant 租户对象
     * @return 更新结果
     */
    ReturnResult<Boolean> updateForSysTenant(SysTenant sysTenant);

    /**
     * 删除租户
     *
     * @param sysTenantId 租户标识
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteForSysTenant(String sysTenantId);

    /**
      * 根据标识获取租户详情
     *
     * @param sysTenantId 租户标识
     * @return 租户详情
     */
    ReturnResult<SysTenant> getForSysTenant(String sysTenantId);

    /**
     * 绑定租户服务
     *
     * @param request 绑定请求
     * @return 绑定结果
     */
    ReturnResult<Boolean> bindServiceForSysTenant(SysTenantBindServiceV1Request request);

    /**
     * 更新租户状态
     *
     * @param sysTenantId 租户标识
     * @param status      状态值
     * @return 更新结果
     */
    ReturnResult<Boolean> updateStatusForSysTenant(String sysTenantId, Integer status);

    /**
     * 获取租户统计数据
     *
     * @return 统计数据（总数、启用数、禁用数、订阅服务数）
     */
    ReturnResult<SysTenantStats> statsForSysTenant();

    /**
     * 重置租户密码并推送租户端
     *
     * @param sysTenantId     租户标识
     * @param sysTenantPassword 新密码（明文，同步到租户端登录账号）
     * @return 重置结果
     */
    ReturnResult<Boolean> resetPasswordForSysTenant(String sysTenantId, String sysTenantPassword);

    /**
     * 重置租户菜单并推送到租户端
     * <p>
     * 按租户当前订阅的服务重新计算菜单并全量推送给租户端，
     * 保证租户端菜单始终与平台分配保持一致（租户端不可自行维护菜单数据，只能被分配）。
     * </p>
     *
     * @param sysTenantId 租户标识
     */
    void resetMenuForSysTenant(String sysTenantId);
}
