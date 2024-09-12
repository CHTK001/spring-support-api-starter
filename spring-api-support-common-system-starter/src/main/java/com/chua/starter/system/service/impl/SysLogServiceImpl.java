package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysLog;
import com.chua.starter.system.mapper.SysLogMapper;
import com.chua.starter.system.service.SysLogService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService{

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
}
