package com.chua.starter.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.entity.SysAiModule;
import com.chua.starter.project.entity.SysAiVincentSetting;

/**
 * 接口SysAiVincentSettingService用于处理与SysAiVincentSetting相关的操作
 * 它继承了IService接口，用于提供基本的CRUD操作
 */
public interface SysAiVincentSettingService extends IService<SysAiVincentSetting> {

    /**
     * 根据模块ID删除模块设置
     *
     * @param sysAiModuleId 模块ID，用于标识要删除设置的模块
     * @return ReturnResult<Boolean> 返回删除操作的结果，包括是否删除成功
     */
    ReturnResult<Boolean> deleteForModuleSetting(Integer sysAiModuleId);

    /**
     * 保存模块设置
     *
     * @param sysAiVincentSetting 模块设置对象，包含要保存的设置信息
     * @return ReturnResult<SysAiModule> 返回保存操作的结果，包括保存后的模块信息
     */
    ReturnResult<SysAiModule> saveForModuleSetting(SysAiVincentSetting sysAiVincentSetting);

    /**
     * 更新模块设置
     *
     * @param sysAiVincentSetting 模块设置对象，包含要更新的设置信息
     * @return ReturnResult<Boolean> 返回更新操作的结果，包括是否更新成功
     */
    ReturnResult<Boolean> updateForModuleSetting(SysAiVincentSetting sysAiVincentSetting);

    /**
     * 根据模块ID获取配置信息
     *
     * @param sysAiModuleId 模块ID，用于标识要获取配置信息的模块
     * @return List<SysAiModule> 返回模块的配置信息列表
     */
    SysAiVincentSetting config(String sysAiModuleId);
}
