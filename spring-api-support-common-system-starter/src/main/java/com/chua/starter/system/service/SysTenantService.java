package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.pojo.tenant.SysTenantServiceBindV1Request;
import com.chua.starter.system.pojo.tenant.SysTenantSyncV1Request;

/**
 * SysTenantService接口定义了系统租户相关的服务操作
 * 它继承自IService，提供了对SysTenant实体的通用操作
 */
public interface SysTenantService extends IService<SysTenant> {

    /**
     * 根据查询条件分页获取SysTenant对象
     *
     * @param query     查询对象，包含分页信息和查询条件
     * @param sysTenant 查询条件封装对象
     * @return 返回封装的分页结果
     */
    IPage<SysTenant> pageFor(Query<SysTenant> query, SysTenant sysTenant);

    /**
     * 保存SysTenant对象
     *
     * @param sysTenant 要保存的SysTenant对象
     * @return 返回保存结果，包括保存后的对象
     */
    ReturnResult<SysTenant> saveFor(SysTenant sysTenant);

    /**
     * 更新SysTenant对象
     *
     * @param sysTenant 要更新的SysTenant对象，包含需要更新的字段
     * @return 返回更新结果
     */
    ReturnResult<Boolean> updateFor(SysTenant sysTenant);

    /**
     * 删除指定ID的SysTenant对象
     *
     * @param sysServiceId 要删除的SysTenant对象的ID
     * @return 返回删除结果
     */
    ReturnResult<Boolean> deleteFor(Long sysServiceId);

    /**
     * 绑定SysTenant和SysTenantService之间的关系
     *
     * @param request 绑定请求对象，包含需要绑定的信息
     * @return 返回绑定结果
     */
    ReturnResult<Boolean> bind(SysTenantServiceBindV1Request request);

    /**
     * 同步SysTenant和SysTenantService之间的关系
     *
     * @param request 同步请求对象，包含需要同步的信息
     * @return 返回同步结果
     */
    ReturnResult<Boolean> sync(SysTenantSyncV1Request request);
}
