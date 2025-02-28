package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysAiVincentSetting;
import com.chua.starter.system.mapper.SysAiVincentSettingMapper;
import com.chua.starter.system.service.SysAiVincentSettingService;
import org.springframework.stereotype.Service;

/**
 * sys_ai_vincent_setting
 *
 * @author yd
 * @since 2023-05-09
 */
@Service
public class SysAiVincentSettingServiceImpl extends ServiceImpl<SysAiVincentSettingMapper, SysAiVincentSetting> implements SysAiVincentSettingService {

    @Override
    public ReturnResult<Boolean> deleteForModuleSetting(Integer sysAiModuleId) {
        return ReturnResult.ok(
                this.remove(Wrappers.<SysAiVincentSetting>lambdaUpdate().eq(SysAiVincentSetting::getSysAiModuleId, sysAiModuleId))
        );
    }

    @Override
    public ReturnResult<SysAiModule> saveForModuleSetting(SysAiVincentSetting sysAiVincentSetting) {
        this.save(sysAiVincentSetting);
        return ReturnResult.ok();
    }

    @Override
    public ReturnResult<Boolean> updateForModuleSetting(SysAiVincentSetting sysAiVincentSetting) {
        if (null == sysAiVincentSetting.getSysAiVincentId()) {
            if (null == sysAiVincentSetting.getSysAiModuleId()) {
                return ReturnResult.illegal("ai模型ID不能为空");
            }
            saveForModuleSetting(sysAiVincentSetting);
            return ReturnResult.ok();
        }
        return ReturnResult.preconditioning(
                this.updateById(sysAiVincentSetting)
        ).toResult();
    }

    @Override
    public SysAiVincentSetting config(String sysAiModuleId) {
        return getOne(Wrappers.<SysAiVincentSetting>lambdaQuery().eq(SysAiVincentSetting::getSysAiModuleId, sysAiModuleId), false);
    }
}
