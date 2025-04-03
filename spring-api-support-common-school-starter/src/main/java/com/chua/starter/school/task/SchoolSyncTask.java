package com.chua.starter.school.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.service.SchoolSyncConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学校同步定时任务
 *
 * @author CH
 */
@Slf4j
@Component
public class SchoolSyncTask {

    @Autowired
    private SchoolSyncConfigService schoolSyncConfigService;

    /**
     * 每小时执行一次同步任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncSchoolData() {
        log.info("开始执行学校数据同步定时任务");
        
        // 查询所有需要同步的配置
        LambdaQueryWrapper<SchoolSyncConfig> queryWrapper = new LambdaQueryWrapper<>();
        // 排除同步状态为正在同步的配置
        queryWrapper.ne(SchoolSyncConfig::getSyncStatus, 1);
        // 查询所有配置
        List<SchoolSyncConfig> configList = schoolSyncConfigService.list(queryWrapper);

        if (configList.isEmpty()) {
            log.info("没有需要同步的配置");
            return;
        }

        // 遍历执行同步
        for (SchoolSyncConfig config : configList) {
            // 判断是否需要执行同步
            if (!needSync(config)) {
                continue;
            }

            log.info("开始同步配置: {}", config.getConfigName());
            boolean success = schoolSyncConfigService.executeSync(config.getConfigId());
            log.info("同步配置: {} 结果: {}", config.getConfigName(), success ? "成功" : "失败");
        }

        log.info("学校数据同步定时任务执行完成");
    }

    /**
     * 判断是否需要执行同步
     *
     * @param config 同步配置
     * @return 是否需要同步
     */
    private boolean needSync(SchoolSyncConfig config) {
        // 如果没有设置同步周期，则不执行同步
        if (config.getSyncPeriod() == null || config.getSyncPeriod() <= 0) {
            return false;
        }

        // 如果没有上次同步时间，则执行同步
        if (config.getLastSyncTime() == null) {
            return true;
        }

        // 计算下次同步时间
        LocalDateTime nextSyncTime = config.getLastSyncTime().plusHours(config.getSyncPeriod());

        // 如果当前时间大于下次同步时间，则执行同步
        return LocalDateTime.now().isAfter(nextSyncTime);
    }
} 