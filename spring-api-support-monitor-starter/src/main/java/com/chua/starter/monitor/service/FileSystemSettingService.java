package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.FileSystemSetting;

/**
 * 文件系统配置管理服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface FileSystemSettingService extends IService<FileSystemSetting> {

    /**
     * 获取启用的配置
     *
     * @return 启用的配置
     */
    FileSystemSetting getEnabledSetting();

    /**
     * 根据配置名称查询
     *
     * @param settingName 配置名称
     * @return 配置信息
     */
    FileSystemSetting findBySettingName(String settingName);

    /**
     * 启用指定配置（会禁用其他配置）
     *
     * @param settingId 配置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> enableSetting(Integer settingId);

    /**
     * 初始化默认配置
     *
     * @return 操作结果
     */
    ReturnResult<Boolean> initDefaultSetting();

    /**
     * 更新配置并刷新缓存
     *
     * @param setting 配置信息
     * @return 操作结果
     */
    ReturnResult<Boolean> updateSettingWithCache(FileSystemSetting setting);

    /**
     * 清除配置缓存
     */
    void clearCache();
}
