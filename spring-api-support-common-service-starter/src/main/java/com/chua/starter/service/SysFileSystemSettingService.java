package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemSetting;

/**
 * 文件系统配置服务接口
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
public interface SysFileSystemSettingService extends IService<SysFileSystemSetting> {

    /**
     * 获取启用的配置
     *
     * @return 启用的配置
     */
    SysFileSystemSetting getEnabledSettingForFileSystem();

    /**
     * 启用指定配置（会禁用其他配置）
     *
     * @param settingId 配置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> enableSettingForFileSystem(Integer settingId);

    /**
     * 初始化默认配置
     *
     * @return 操作结果
     */
    ReturnResult<Boolean> initDefaultSettingForFileSystem();
}
