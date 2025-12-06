package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.date.ComparableDateTime;
import com.chua.common.support.lang.date.DateTime;
import com.chua.report.client.starter.entity.JobCat;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.job.pojo.JobStatistic;
import com.chua.starter.monitor.job.pojo.JobStatisticResult;
import com.chua.starter.monitor.mapper.MonitorJobLogMapper;
import com.chua.starter.monitor.sender.MonitorSender;
import com.chua.starter.monitor.service.MonitorJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监控日志
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
public class MonitorJobLogServiceImpl extends ServiceImpl<MonitorJobLogMapper, MonitorJobLog> implements MonitorJobLogService {

    private final SyncServer syncServer;
    private final MonitorSender monitorSender;

    @Override
    public JobStatisticResult time(MonitorJobLog entity) {
        LocalDateTime startDate = entity.getStartDate();
        LocalDateTime endDate = entity.getEndDate();
        if(null == startDate || null == endDate) {
            return new JobStatisticResult();
        }
        endDate = endDate.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1).minusSeconds(1);
        entity.setEndDate(endDate);
        List<JobStatistic> time = baseMapper.time(entity);
        JobStatisticResult result = new JobStatisticResult();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> tpl = new HashMap<>();
        for (JobStatistic statistic : time) {
            tpl.put(statistic.getJobLogTriggerDate().format(formatter) +
                    ("00000".equals(statistic.getJobLogTriggerCode()) ? "SUCCESS": "FAILURE") , statistic.getCnt());
        }
        List<Long> success = new LinkedList<>();
        List<Long> failure = new LinkedList<>();

        LocalDateTime start = startDate;
        while (start.isBefore(endDate) || start.equals(endDate)) {
            String format = start.format(formatter);
            success.add(tpl.getOrDefault(format + "SUCCESS", 0L));
            failure.add(tpl.getOrDefault(format + "FAILURE", 0L));
            start = start.plusDays(1);
        }

        result.setSuccessCount(success.toArray(new Long[0]));
        result.setFailureCount(failure.toArray(new Long[0]));
        return result;
    }

    @Override
    public Boolean clear(MonitorJobLog entity) {
        Integer clearType = entity.getClearType();
        if(null == clearType) {
            return true;
        }
        if(clearType >= 1 && clearType <= 4) {
            entity = createNewDate(clearType);
            LocalDateTime startDate = entity.getStartDate();
            LocalDateTime endDate = entity.getEndDate();
            if(null == startDate || null == endDate) {
                return true;
            }
            baseMapper.delete(Wrappers.<MonitorJobLog>lambdaUpdate()
                    .between(MonitorJobLog::getJobLogTriggerDate, startDate, endDate)
            );
        }

        if(clearType >= 5 && clearType <= 8) {
            baseMapper.deleteNumber(getNumberByClearType(clearType));
        }

        if(clearType == 9) {
            baseMapper.delete(Wrappers.lambdaUpdate());
        }
        return true;
    }

    @Override
    public String logCat(int jobLogId) {
        MonitorJobLog monitorJobLog = baseMapper.selectById(jobLogId);
        if(null == monitorJobLog) {
            return "";
        }

        if (syncServer == null) {
            return "";
        }
        
        // 从 SyncServer 获取所有在线客户端
        Map<String, ClientInfo> allClients = syncServer.getAllClients();
        ClientInfo matchedClient = allClients.values().stream()
                .filter(clientInfo -> {
                    String appName = clientInfo.getAppName();
                    String address = clientInfo.getIpAddress() + ":" + clientInfo.getServerPort();
                    return appName != null && appName.equals(monitorJobLog.getJobLogApp()) &&
                            monitorJobLog.getJobLogTriggerAddress() != null &&
                            monitorJobLog.getJobLogTriggerAddress().contains(address);
                })
                .findFirst()
                .orElse(null);

        if (matchedClient == null) {
            return "";
        }
        
        JobCat jobCat = new JobCat();
        jobCat.setLogId(monitorJobLog.getJobLogId());
        jobCat.setFromLineNum(0);
        jobCat.setDate(monitorJobLog.getJobLogTriggerTime());
        
        // 通过 SyncServer 发送消息
        syncServer.send(matchedClient.getClientId(), "monitor/job/logcat", 
                java.util.Map.of("jobCat", Json.toJson(jobCat), "jobLog", monitorJobLog));
        return monitorJobLog.getJobLogTriggerMsg();
    }

    private Integer getNumberByClearType(Integer clearType) {
        if(clearType == 5) {
            return 1_000;
        }
        if(clearType == 6) {
            return 10_000;
        }

        if(clearType == 7) {
            return 30_000;
        }

        return 100_000;
    }

    /**
     * 创建时间
     * @param clearType 类型
     * @return
     */
    private MonitorJobLog createNewDate(Integer clearType) {
        MonitorJobLog res = new MonitorJobLog();
        if(clearType == 1) {
            ComparableDateTime comparableDateTime = DateTime.now().minusMonths(1);
            res.setEndDate(comparableDateTime.toLocalDateTime());
            res.setStartDate(comparableDateTime.toLocalDateTime().minusYears(1));
            return res;
        }

        if(clearType == 2) {
            ComparableDateTime comparableDateTime = DateTime.now().minusMonths(3);
            res.setEndDate(comparableDateTime.toLocalDateTime());
            res.setStartDate(comparableDateTime.toLocalDateTime().minusYears(1));
            return res;
        }

        if(clearType == 3) {
            ComparableDateTime comparableDateTime = DateTime.now().minusMonths(6);
            res.setEndDate(comparableDateTime.toLocalDateTime());
            res.setStartDate(comparableDateTime.toLocalDateTime().minusYears(1));
            return res;
        }

        if(clearType == 4) {
            ComparableDateTime comparableDateTime = DateTime.now().minusMonths(12);
            res.setEndDate(comparableDateTime.toLocalDateTime());
            res.setStartDate(comparableDateTime.toLocalDateTime().minusYears(1));
            return res;
        }

        return null;

    }
}
