package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;

import java.util.Set;

/**
 * 系统用户日志服务接口
 * 该接口定义了与系统用户日志相关的操作，包括查询用户日志分页数据和删除指定的日志
 *
 * @author CH
 * @since 2024/8/8
 */
public interface SysUserLogService extends IService<SysUserLog> {

    /**
     * 根据查询条件分页获取用户日志
     * 此方法允许根据不同的查询条件和角色筛选，返回用户日志的分页数据
     *
     * @param query 查询对象，用于分页和排序设置
     * @param sysUserLogQuery 查询条件对象，用于筛选日志
     * @param roles 用户角色集合，用于根据角色筛选日志
     * @return 返回包含用户日志的分页数据对象
     */
    IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Set<String> roles);

    /**
     * 删除指定的用户日志
     * 此方法通过日志的ID进行删除操作，返回一个表示操作结果的对象
     *
     * @param userLogId 用户日志的ID
     * @return 返回一个包含操作结果的对象，对象中的数据字段表示操作是否成功
     */
    ReturnResult<Boolean> delete(Long userLogId);

    /**
     * 获取当前用户的日志
     * 此方法根据用户的角色和查询条件获取当前用户的日志，返回一个包含日志的分页数据对象
     *
     * @param query      查询对象，用于分页和排序设置
     * @param sysUserLogQuery 查询条件对象，用于筛选日志
     * @param sysUserId  账号ID
     * @param roles      用户角色集合，用于根据角色筛选日志
     * @return 返回包含当前用户日志的分页数据对象
     */
    IPage<SysUserLog> mineLogs(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Integer sysUserId, Set<String> roles);
}
