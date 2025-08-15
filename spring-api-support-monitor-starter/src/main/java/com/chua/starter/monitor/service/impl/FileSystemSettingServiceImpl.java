package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.FileSystemSetting;
import com.chua.starter.monitor.mapper.FileSystemSettingMapper;
import com.chua.starter.monitor.service.FileSystemSettingService;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.CompletableFuture;
import com.chua.starter.monitor.task.FileSystemMergeQueue;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件系统配置管理服务实现类
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemSettingServiceImpl extends ServiceImpl<FileSystemSettingMapper, FileSystemSetting>
        implements FileSystemSettingService {

    @Autowired
    private FileSystemMergeQueue fileSystemMergeQueue;

    @Override
    @Cacheable(value = "fileSystemSetting", key = "'enabled'")
    public FileSystemSetting getEnabledSetting() {
        log.debug("获取启用的文件系统配置");
        FileSystemSetting setting = baseMapper.findEnabledSetting();
        if (setting == null) {
            log.warn("未找到启用的文件系统配置，返回默认配置");
            return FileSystemSetting.getDefaultSetting();
        }
        return setting;
    }

    @Override
    public FileSystemSetting findBySettingName(String settingName) {
        log.debug("根据配置名称查询: settingName={}", settingName);
        return baseMapper.findBySettingName(settingName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "fileSystemSetting", allEntries = true)
    public ReturnResult<Boolean> enableSetting(Integer settingId) {
        log.info("启用文件系统配置: settingId={}", settingId);

        try {
            FileSystemSetting setting = getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            // 先禁用所有配置
            baseMapper.disableAllSettings();

            // 再启用指定配置
            int result = baseMapper.enableSetting(settingId);
            if (result > 0) {
                log.info("文件系统配置启用成功: settingId={}", settingId);
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("启用配置失败");
            }
        } catch (Exception e) {
            log.error("启用文件系统配置失败: settingId={}", settingId, e);
            return ReturnResult.error("启用配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> initDefaultSetting() {
        log.info("初始化默认文件系统配置");

        try {
            // 检查是否已存在配置
            FileSystemSetting existingSetting = findBySettingName("默认配置");
            if (existingSetting != null) {
                log.info("默认配置已存在，跳过初始化");
                return ReturnResult.ok(true);
            }

            // 创建默认配置
            FileSystemSetting defaultSetting = FileSystemSetting.getDefaultSetting();
            save(defaultSetting);

            log.info("默认文件系统配置初始化成功: settingId={}", defaultSetting.getFileSystemSettingId());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("初始化默认文件系统配置失败", e);
            return ReturnResult.error("初始化默认配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "fileSystemSetting", allEntries = true)
    public ReturnResult<Boolean> updateSettingWithCache(FileSystemSetting setting) {
        log.info("更新文件系统配置并刷新缓存: settingId={}", setting.getFileSystemSettingId());

        try {
            boolean result = updateById(setting);
            if (result) {
                log.info("文件系统配置更新成功: settingId={}", setting.getFileSystemSettingId());
                // 如果手动合并开启（即自动合并关闭），清空自动合并队列
                if (Boolean.TRUE.equals(setting.getFileSystemSettingManualMergeEnabled())) {
                    try {
                        // 延迟少许时间，等待缓存失效与新配置生效
                        CompletableFuture.runAsync(() -> {
                            try { Thread.sleep(200L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                            fileSystemMergeQueue.clear();
                            log.info("检测到关闭自动合并，已清空合并队列");
                        });
                    } catch (Exception ignore) { }
                }
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("更新配置失败");
            }
        } catch (Exception e) {
            log.error("更新文件系统配置失败: settingId={}", setting.getFileSystemSettingId(), e);
            return ReturnResult.error("更新配置失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "fileSystemSetting", allEntries = true)
    public void clearCache() {
        log.info("清除文件系统配置缓存");
    }
}
