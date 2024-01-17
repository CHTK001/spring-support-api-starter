package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysSetting;
public interface SysSettingService extends IService<SysSetting>{


    /**
     * @param name
     * @return {@link SysSetting}
     */
    SysSetting getSetting(String name);
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
}
