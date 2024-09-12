package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysLog;

import java.util.Set;

/**
 * 系统日志服务接口，定义了与系统日志相关的操作
 * 继承了通用的服务接口IService，用于对SysLog实体进行增删查改等操作
 *
 * @author CH
 * @since 2024/8/7
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 分页查询系统日志信息
     *
     * @param query 分页查询对象，用于指定分页参数和查询条件
     * @param sysLog 日志实体对象，用于设置查询条件
     * @param roles 用户角色集合，用于权限控制或查询特定角色的日志
     * @return 返回分页查询结果，包含日志列表和分页信息
     */
    IPage<SysLog> pageForLog(Query<SysLog> query, SysLog sysLog, Set<String> roles);

    /**
     * 删除指定的日志记录
     *
     * @param logId 日志记录的唯一标识
     * @return 返回删除操作的结果，包含成功与否的标志和可能的错误信息
     */
    ReturnResult<Boolean> delete(Long logId);
}
