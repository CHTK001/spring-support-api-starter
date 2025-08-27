package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.logger.SysLoggerInfo;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysLogService;
import com.chua.starter.service.mapper.SysLogMapper;
import com.chua.starter.system.entity.SysLog;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 系统日志服务实现类，提供系统日志的分页查询、删除和监听日志事件功能
 *
 * @author CH
 * @since 2024/8/7
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService, ApplicationListener<SysLoggerInfo> {

    /**
     * 分页查询系统日志信息
     *
     * @param query  分页查询对象，用于指定分页参数和查询条件
     *               例如：new Query<SysLog>().setPage(1).setPageSize(10)
     * @param sysLog 日志实体对象，用于设置查询条件
     *               例如：new SysLog().setSysLogUsername("admin").setSysLogStatus(1)
     * @param roles  用户角色集合，用于权限控制或查询特定角色的日志
     *               例如：Set.of("ROLE_ADMIN", "ROLE_USER")
     * @return 返回分页查询结果，包含日志列表和分页信息
     */
    @Override
    public IPage<SysLog> pageForLog(Query<SysLog> query, SysLog sysLog, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysLog>lambdaQuery()
                .like(StringUtils.isNotBlank(sysLog.getSysLogUsername()), SysLog::getSysLogUsername, sysLog.getSysLogUsername())
                .like(StringUtils.isNotBlank(sysLog.getSysLogFrom()), SysLog::getSysLogFrom, sysLog.getSysLogFrom())
                .like(StringUtils.isNotBlank(sysLog.getSysLogAddress()), SysLog::getSysLogAddress, sysLog.getSysLogAddress())
                .eq(ObjectUtils.isNotEmpty(sysLog.getSysLogStatus()), SysLog::getSysLogStatus, sysLog.getSysLogStatus())
                .orderByDesc(SysLog::getCreateTime)
        );
    }

    /**
     * 删除指定的日志记录
     *
     * @param logId 日志记录的唯一标识
     *              例如：1L
     * @return 返回删除操作的结果，包含成功与否的标志和可能的错误信息
     */
    @Override
    public ReturnResult<Boolean> delete(Long logId) {
        return baseMapper.deleteById(logId) > 0 ? ReturnResult.ok() : ReturnResult.error();
    }

    /**
     * 监听系统日志事件，并将日志信息保存到数据库中
     *
     * @param event 系统日志事件对象，包含日志的相关信息
     *              例如：SysLoggerInfo.builder().createName("admin").logModule("用户管理").logCost(100L).build()
     */
    @Override
    public void onApplicationEvent(SysLoggerInfo event) {
        SysLog sysLog = new SysLog();
        sysLog.setSysLogUsername(event.getCreateName());
        sysLog.setSysLogFrom(event.getLogModule());
        sysLog.setSysLogCost(event.getLogCost());
        sysLog.setSysLogAddress(event.getClientIp());
        sysLog.setSysLogParam(event.getLogParam());
        sysLog.setSysLogStatus(event.getLogStatus());
        sysLog.setSysLogUrl(event.getLogMapping());
        sysLog.setSysLogBrowser(event.getBrowser());
        sysLog.setSysLogContent(event.getLogContent());

        baseMapper.insert(sysLog);
    }
}
