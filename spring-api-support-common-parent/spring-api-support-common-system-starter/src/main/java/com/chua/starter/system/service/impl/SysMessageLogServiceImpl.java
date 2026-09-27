package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMessageLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysMessageLogMapper;
import com.chua.starter.system.service.SysMessageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 消息推送日志服务实现
 *
 * @since 4.0.0.50
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysMessageLogServiceImpl implements SysMessageLogService {

    /**
     * SYS消息日志映射器
    */
    private final SysMessageLogMapper sysMessageLogMapper;

    @Override
    /**
     * 分页日志
    */
    public IPage<SysMessageLog> pageForLog(Query<SysMessageLog> query, SysMessageLog log) {
        SysMessageLog c = log == null ? new SysMessageLog() : log;
        return sysMessageLogMapper.selectPage(query.createPage(),
                Wrappers.<SysMessageLog>lambdaQuery()
                        .like(StringUtils.isNotBlank(c.getSysMessageLogTarget()), SysMessageLog::getSysMessageLogTarget, c.getSysMessageLogTarget())
                        .like(StringUtils.isNotBlank(c.getSysMessageLogChannel()), SysMessageLog::getSysMessageLogChannel, c.getSysMessageLogChannel())
                        .eq(StringUtils.isNotBlank(c.getSysMessageCode()), SysMessageLog::getSysMessageCode, c.getSysMessageCode())
                        .eq(StringUtils.isNotBlank(c.getSysMessageBatchId()), SysMessageLog::getSysMessageBatchId, c.getSysMessageBatchId())
                        .eq(null != c.getSysMessageProviderId(), SysMessageLog::getSysMessageProviderId, c.getSysMessageProviderId())
                        .eq(null != c.getSysMessageTemplateId(), SysMessageLog::getSysMessageTemplateId, c.getSysMessageTemplateId())
                        .eq(null != c.getSysMessageReadStatus(), SysMessageLog::getSysMessageReadStatus, c.getSysMessageReadStatus())
                        .orderByDesc(SysMessageLog::getSysMessageLogId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 清理日志
    */
    public ReturnResult<Integer> cleanLog(LocalDateTime beforeTime) {
        if (null == beforeTime) {
            return ReturnResult.illegal("截止时间不能为空，格式为 yyyy-MM-dd 或 yyyy-MM-ddTHH:mm:ss");
        }
        int deleted = sysMessageLogMapper.delete(Wrappers.<SysMessageLog>lambdaQuery()
                .le(SysMessageLog::getCreateTime, beforeTime));
        return ReturnResult.ok(deleted);
    }
}
