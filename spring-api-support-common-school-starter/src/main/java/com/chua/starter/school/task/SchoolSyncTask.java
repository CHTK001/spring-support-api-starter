package com.chua.starter.school.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.service.SchoolSyncConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "学校同步任务", description = "定时同步学校数据")
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
        queryWrapper.ne(SchoolSyncConfig::getSchoolSyncConfigStatus, 1);
        // 只查询已启用的配置
        queryWrapper.eq(SchoolSyncConfig::getSchoolSyncConfigEnabled, true);
        // 查询所有配置
        List<SchoolSyncConfig> configList = schoolSyncConfigService.list(queryWrapper);

        if (configList.isEmpty()) {
            log.info("没有需要同步的配置");
            return;
        }

        // 遍历执行同步
        for (SchoolSyncConfig config : configList) {
            try {
                log.info("开始同步配置: {}", config.getSchoolSyncConfigName());
                boolean success = schoolSyncConfigService.executeSync(config.getSchoolSyncConfigId());
                log.info("同步配置: {} 结果: {}", config.getSchoolSyncConfigName(), success ? "成功" : "失败");
            } catch (Exception e) {
                log.error("同步配置: {} 发生异常", config.getSchoolSyncConfigName(), e);
                // 更新同步状态为失败
                SchoolSyncConfig updateConfig = new SchoolSyncConfig();
                updateConfig.setSchoolSyncConfigId(config.getSchoolSyncConfigId());
                updateConfig.setSchoolSyncConfigStatus(3); // 3表示同步失败
                updateConfig.setUpdateTime(LocalDateTime.now());
                schoolSyncConfigService.updateById(updateConfig);
            }
        }

        log.info("学校数据同步定时任务执行完成");
    }

}