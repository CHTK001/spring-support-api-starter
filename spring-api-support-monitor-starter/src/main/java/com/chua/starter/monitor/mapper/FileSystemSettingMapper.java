package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.FileSystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文件系统配置管理 Mapper 接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Mapper
public interface FileSystemSettingMapper extends BaseMapper<FileSystemSetting> {

    /**
     * 获取启用的配置
     *
     * @return 启用的配置
     */
    FileSystemSetting findEnabledSetting();

    /**
     * 根据配置名称查询
     *
     * @param settingName 配置名称
     * @return 配置信息
     */
    FileSystemSetting findBySettingName(@Param("settingName") String settingName);

    /**
     * 禁用所有配置
     *
     * @return 更新的记录数
     */
    int disableAllSettings();

    /**
     * 启用指定配置
     *
     * @param settingId 配置ID
     * @return 更新的记录数
     */
    int enableSetting(@Param("settingId") Integer settingId);
}
