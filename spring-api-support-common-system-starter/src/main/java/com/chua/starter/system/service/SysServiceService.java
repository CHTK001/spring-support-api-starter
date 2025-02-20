package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysService;
import com.chua.starter.system.pojo.service.SysServiceBindV1Request;

import java.util.List;

/**
 * 服务接口
 *
 * @author CH
 */
public interface SysServiceService extends IService<SysService> {

    /**
     * 分页查询服务信息
     *
     * @param query      查询对象，包含查询条件和分页参数
     * @param sysService 服务对象，可能包含查询所需的额外参数
     * @return 返回分页结果，包含服务信息列表和分页数据
     */
    IPage<SysService> pageFor(Query<SysService> query, SysService sysService);

    /**
     * 保存服务信息
     *
     * @param sysService 待保存的服务对象，包含所有需要保存的属性
     * @return 返回保存结果，包括操作状态和可能的服务对象
     */
    ReturnResult<SysService> saveFor(SysService sysService);

    /**
     * 更新服务信息
     *
     * @param sysService 待更新的服务对象，包含所有需要更新的属性
     * @return 返回更新结果，包括操作状态和可能的布尔值表示成功与否
     */
    ReturnResult<Boolean> updateFor(SysService sysService);

    /**
     * 删除服务信息
     *
     * @param sysServiceId 服务的ID，用于标识要删除的服务
     * @return 返回删除结果，包括操作状态和可能的布尔值表示成功与否
     */
    ReturnResult<Boolean> deleteFor(Long sysServiceId);

    /**
     * 绑定服务
     *
     * @param request 绑定服务请求对象，包含服务ID和要绑定的模块ID
     * @return 返回绑定结果，包括操作状态和可能的布尔值表示成功与否
     */
    ReturnResult<Boolean> bind(SysServiceBindV1Request request);

    /**
     * 查询服务列表
     *
     * @param sysService 服务对象，可能包含查询所需的额外参数
     * @return 返回服务列表
     */
    ReturnResult<List<SysService>> listFor(SysService sysService);
}
