package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.logger.SysLoggerInfo;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysLog;
import com.chua.starter.system.mapper.SysLogMapper;
import com.chua.starter.system.service.SysLogService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService, ApplicationListener<SysLoggerInfo> {

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

    @Override
    public ReturnResult<Boolean> delete(Long logId) {
        return baseMapper.deleteById(logId) > 0 ? ReturnResult.ok() : ReturnResult.error();
    }

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
