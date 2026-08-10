package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.system.mapper.SysErrorLogMapper;
import com.chua.starter.system.pojo.error.ErrorReportV1Item;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;
import com.chua.starter.system.service.SysErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端异常上报日志服务实现
 *
 * @author CH
 * @since 2026/8/8
 */
@Service
@Slf4j
public class SysErrorLogServiceImpl extends ServiceImpl<SysErrorLogMapper, SysErrorLog>
        implements SysErrorLogService {

    /**
     * 单次批量上报允许的最大异常条数, 防止超长请求
     */
    private static final int MAX_BATCH_SIZE = 200;

    /**
     * 单条异常消息允许的最大长度
     */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> saveReport(ErrorReportV1Request request) {
        if (request == null || request.getRecords() == null || request.getRecords().isEmpty()) {
            return ReturnResult.illegal("异常记录列表不能为空");
        }

        List<ErrorReportV1Item> records = request.getRecords();
        if (records.size() > MAX_BATCH_SIZE) {
            records = records.subList(0, MAX_BATCH_SIZE);
        }

        List<SysErrorLog> entityList = new ArrayList<>(records.size());
        for (ErrorReportV1Item item : records) {
            if (item == null) {
                continue;
            }
            entityList.add(buildEntity(item, request.getTime()));
        }

        if (entityList.isEmpty()) {
            return ReturnResult.illegal("异常记录列表不能为空");
        }

        boolean saved = saveBatch(entityList);
        if (!saved) {
            return ReturnResult.of(false);
        }

        log.info("前端异常上报入库成功: {} 条", entityList.size());
        return ReturnResult.ok(true);
    }

    @Override
    public List<SysErrorLog> listRecent(String source, int limit) {
        return list(
                Wrappers.<SysErrorLog>lambdaQuery()
                        .eq(
                                source != null && !source.isEmpty(),
                                SysErrorLog::getSysErrorSource,
                                source
                        )
                        .orderByDesc(SysErrorLog::getSysErrorLogId)
                        .last("LIMIT " + Math.max(limit, 0))
        );
    }

    /**
     * 将单条上报记录转换为实体对象
     *
     * @param item      上报记录
     * @param reportTime 客户端上报时间
     * @return 实体对象
     */
    private SysErrorLog buildEntity(ErrorReportV1Item item, Long reportTime) {
        SysErrorLog entity = new SysErrorLog();
        entity.setSysErrorKey(truncate(item.getKey()));
        entity.setSysErrorSource(truncate(item.getSource()));
        entity.setSysErrorMessage(truncate(item.getMessage()));
        entity.setSysErrorStack(truncate(item.getStack()));
        entity.setSysErrorFilename(truncate(item.getFilename()));
        entity.setSysErrorLineno(item.getLineno());
        entity.setSysErrorColno(item.getColno());
        entity.setSysErrorResourceUrl(truncate(item.getResourceUrl()));
        entity.setSysErrorTagName(truncate(item.getTagName()));
        entity.setSysErrorFirstAt(item.getFirstAt());
        entity.setSysErrorCount(item.getCount() == null ? 1 : item.getCount());
        entity.setSysErrorReportTime(reportTime);
        return entity;
    }

    /**
     * 截断超长字符串, 避免超过数据库字段长度
     *
     * @param value 原始字符串
     * @return 截断后的字符串
     */
    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > MAX_MESSAGE_LENGTH ? value.substring(0, MAX_MESSAGE_LENGTH) : value;
    }
}
