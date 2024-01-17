package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.starter.admin.entity.SysSetting;

import java.util.List;

public interface SysSettingService extends IService<SysSetting>{


    /**
     * 获取配置
     * @param name 名称
     * @return {@link SysSetting}
     */
    List<SysSetting> getSettings(String name);
    /**
     * 更新配置
     * @param sysSetting 配置
     */
    boolean updateSetting(SysSetting sysSetting);

    /**
     * 删除配置
     * @param settingId 配置
     * @return boolean
     */
    boolean deleteSetting(Integer settingId);

    /**
     * 保存配置
     * @param sysSetting 配置
     * @return boolean
     */
    boolean saveSetting(SysSetting sysSetting);

    /**
     * 批处理
     * @param sysSettings 配置
     * @return boolean
     */
    ErrorResult batchSetting(List<SysSetting> sysSettings);
}
