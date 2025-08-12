package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataSetting;

public interface SystemDataSettingService extends IService<SystemDataSetting> {
    ReturnResult<Boolean> saveOrUpdateSetting(SystemDataSetting setting);
    ReturnResult<Boolean> deleteById(Integer id);

    /** 读取控制台权限配置(JSON字符串) */
    ReturnResult<String> getConsoleConfig(Integer id);

    /** 保存控制台权限配置(JSON字符串) */
    ReturnResult<Boolean> saveConsoleConfig(Integer id, String jsonConfig);
}


