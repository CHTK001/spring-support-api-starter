// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysErrorLogMapper;
import com.chua.starter.system.pojo.error.ErrorReportV1Item;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;
import com.chua.starter.system.service.SysErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端异常上报日志服务实现
 *
 * @since 2026/8/8
 * @author CH
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
    /**
     * 保存Report
     *
     * @param request 请求
     * @return 保存report的结果
     */
    public ReturnResult<Boolean> saveReport(ErrorReportV1Request request) {
        if (request == null || request.records() == null || request.records().isEmpty()) {
            return ReturnResult.illegal("异常记录列表不能为空");
        }

        List<ErrorReportV1Item> records = request.records();
        if (records.size() > MAX_BATCH_SIZE) {
            records = records.subList(0, MAX_BATCH_SIZE);
        }

        List<SysErrorLog> entityList = new ArrayList<>(records.size());
        for (ErrorReportV1Item item : records) {
            if (item == null) {
                continue;
            }
            entityList.add(buildEntity(item, request.time()));
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
    /**
     * 列表recent
    */
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

    @Override
    /**
     * pagefor记录日志
    */
    public IPage<SysErrorLog> pageForLog(Query<SysErrorLog> query, SysErrorLog sysErrorLog) {
        LambdaQueryWrapper<SysErrorLog> wrapper = new LambdaQueryWrapper<>();
        if (sysErrorLog != null) {
            if (sysErrorLog.getSysErrorSource() != null) {
                wrapper.eq(SysErrorLog::getSysErrorSource, sysErrorLog.getSysErrorSource());
            }
            if (sysErrorLog.getSysErrorMessage() != null) {
                wrapper.like(SysErrorLog::getSysErrorMessage, sysErrorLog.getSysErrorMessage());
            }
        }
        wrapper.orderByDesc(SysErrorLog::getSysErrorLogId);
        return baseMapper.selectPage(query.createPage(), wrapper);
    }

    @Override
    /**
     * 统计
    */
    public Map<String, Object> statistic() {
        Map<String, Object> result = new HashMap<>(8);

        // 总数
        long total = count();
        result.put("total", total);

        // 今日新增
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = count(Wrappers.<SysErrorLog>lambdaQuery()
                .ge(SysErrorLog::getCreateTime, todayStart));
        result.put("todayCount", todayCount);

        // 按处理状态统计: 0未处理 1已处理 2已忽略
        long pendingCount = count(Wrappers.<SysErrorLog>lambdaQuery().eq(SysErrorLog::getSysErrorStatus, 0));
        long handledCount = count(Wrappers.<SysErrorLog>lambdaQuery().eq(SysErrorLog::getSysErrorStatus, 1));
        long ignoredCount = count(Wrappers.<SysErrorLog>lambdaQuery().eq(SysErrorLog::getSysErrorStatus, 2));
        result.put("pendingCount", pendingCount);
        result.put("handledCount", handledCount);
        result.put("ignoredCount", ignoredCount);
        // 处理率 = (已处理 + 已忽略) / 总数
        double handleRate = total > 0 ? Math.round((handledCount + ignoredCount) * 10000.0 / total) / 100.0 : 0.0;
        result.put("handleRate", handleRate);

        // 按来源分组统计
        List<Map<String, Object>> sourceStats = new ArrayList<>();
        List<Map<String, Object>> sourceRows = baseMapper.selectMaps(
                new QueryWrapper<SysErrorLog>()
                        .select("sys_error_source AS source, COUNT(*) AS count")
                        .groupBy("sys_error_source"));
        for (Map<String, Object> row : sourceRows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("source", row.get("source"));
            item.put("count", ((Number) row.get("count")).longValue());
            sourceStats.add(item);
        }
        result.put("sourceStats", sourceStats);

        // 按错误Key分组取Top5
        List<Map<String, Object>> topErrors = new ArrayList<>();
        List<Map<String, Object>> topRows = baseMapper.selectMaps(
                new QueryWrapper<SysErrorLog>()
                        .select("sys_error_key AS errorKey, sys_error_message AS message, sys_error_source AS source, COUNT(*) AS count")
                        .groupBy("sys_error_key")
                        .orderByDesc("count")
                        .last("LIMIT 5"));
        for (Map<String, Object> row : topRows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("errorKey", row.get("errorKey"));
            item.put("message", row.get("message"));
            item.put("source", row.get("source"));
            item.put("count", ((Number) row.get("count")).longValue());
            topErrors.add(item);
        }
        result.put("topErrors", topErrors);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 按ID批量删除
    */
    public ReturnResult<Boolean> deleteByIds(List<Long> sysErrorLogIds) {
        if (sysErrorLogIds == null || sysErrorLogIds.isEmpty()) {
            return ReturnResult.illegal("异常日志ID不能为空");
        }
        return ReturnResult.of(baseMapper.deleteBatchIds(sysErrorLogIds) > 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新处理状态
    */
    public ReturnResult<Boolean> updateStatus(Long sysErrorLogId, Integer status) {
        if (null == sysErrorLogId) {
            return ReturnResult.illegal("异常日志ID不能为空");
        }
        if (null == status || (status < 0 || status > 2)) {
            return ReturnResult.illegal("状态值不合法");
        }
        SysErrorLog update = new SysErrorLog();
        update.setSysErrorLogId(sysErrorLogId);
        update.setSysErrorStatus(status);
        return ReturnResult.of(baseMapper.updateById(update) > 0);
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
        entity.setSysErrorKey(truncate(item.key()));
        entity.setSysErrorSource(truncate(item.source()));
        entity.setSysErrorMessage(truncate(item.message()));
        entity.setSysErrorStack(truncate(item.stack()));
        entity.setSysErrorFilename(truncate(item.filename()));
        entity.setSysErrorLineno(item.lineno());
        entity.setSysErrorColno(item.colno());
        entity.setSysErrorResourceUrl(truncate(item.resourceUrl()));
        entity.setSysErrorTagName(truncate(item.tagName()));
        entity.setSysErrorFirstAt(item.firstAt());
        entity.setSysErrorCount(item.count() == null ? 1 : item.count());
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

    @Override
    /**
     * 查询详情
    */
    public SysErrorLog getDetail(Long sysErrorLogId) {
        if (null == sysErrorLogId) {
            return null;
        }
        return getById(sysErrorLogId);
    }

    @Override
    /**
     * 按来源统计
    */
    public List<java.util.Map<String, Object>> statisticBySource(Long startTime, Long endTime) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysErrorLog> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (null != startTime) {
            wrapper.ge(SysErrorLog::getSysErrorReportTime, startTime);
        }
        if (null != endTime) {
            wrapper.le(SysErrorLog::getSysErrorReportTime, endTime);
        }
        wrapper.select(SysErrorLog::getSysErrorSource);
        List<SysErrorLog> list = list(wrapper);
        java.util.Map<String, Long> counter = new java.util.LinkedHashMap<>();
        for (SysErrorLog item : list) {
            String source = item.getSysErrorSource() == null ? "unknown" : item.getSysErrorSource();
            counter.merge(source, 1L, Long::sum);
        }
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        counter.forEach((type, count) -> {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("type", type);
            row.put("count", count);
            result.add(row);
        });
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    /**
     * 清理
    */
    public int cleanBefore(int days) {
        long threshold = System.currentTimeMillis() - Math.max(days, 0) * 24L * 3600L * 1000L;
        boolean removed = remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysErrorLog>()
                .lt(SysErrorLog::getSysErrorReportTime, threshold));
        return removed ? 1 : 0;
    }
}
