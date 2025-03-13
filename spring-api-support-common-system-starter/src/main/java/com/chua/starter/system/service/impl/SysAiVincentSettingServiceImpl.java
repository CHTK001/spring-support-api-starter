package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysAiVincentSetting;
import com.chua.starter.system.entity.SysAiVincentStyle;
import com.chua.starter.system.mapper.SysAiVincentSettingMapper;
import com.chua.starter.system.service.SysAiVincentSettingService;
import com.chua.starter.system.service.SysAiVincentStyleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * sys_ai_vincent_setting
 *
 * @author CH
 * @since 2025-05-09
 */
@Service
@RequiredArgsConstructor
public class SysAiVincentSettingServiceImpl extends ServiceImpl<SysAiVincentSettingMapper, SysAiVincentSetting> implements SysAiVincentSettingService {

    final TransactionTemplate transactionTemplate;
    final SysAiVincentStyleService sysAiVincentStyleService;

    @Override
    public ReturnResult<Boolean> deleteForModuleSetting(Integer sysAiModuleId) {
        return transactionTemplate.execute(status -> {
            sysAiVincentStyleService.remove(Wrappers.<SysAiVincentStyle>lambdaQuery().eq(SysAiVincentStyle::getSysAiModuleId, sysAiModuleId));
            return ReturnResult.ok(
                    this.remove(Wrappers.<SysAiVincentSetting>lambdaUpdate().eq(SysAiVincentSetting::getSysAiModuleId, sysAiModuleId))
            );
        });
    }

    @Override
    public ReturnResult<SysAiModule> saveForModuleSetting(SysAiVincentSetting sysAiVincentSetting) {
        return transactionTemplate.execute(status -> {
            this.save(sysAiVincentSetting);
            List<SysAiVincentStyle> styles = sysAiVincentSetting.getStyles();
            if (null != styles) {
                for (SysAiVincentStyle style : styles) {
                    style.setSysAiModuleId(sysAiVincentSetting.getSysAiModuleId());
                }
                sysAiVincentStyleService.saveBatch(styles);
            }

            return ReturnResult.ok();
        });
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
        SysAiVincentSetting sysAiVincentSetting1 = getById(sysAiVincentSetting.getSysAiVincentId());
        if (null == sysAiVincentSetting1) {
            return ReturnResult.illegal("ai模型ID不存在");
        }
        return transactionTemplate.execute(status -> {
            List<SysAiVincentStyle> styles = sysAiVincentSetting.getStyles();
            if (null != styles) {
                for (SysAiVincentStyle style : styles) {
                    style.setSysAiModuleId(sysAiVincentSetting1.getSysAiModuleId());
                }
                sysAiVincentStyleService.saveBatch(styles);
            }

            return ReturnResult.preconditioning(
                    this.updateById(sysAiVincentSetting)
            ).toResult();
        });

    }

    @Override
    public SysAiVincentSetting config(String sysAiModuleId) {
        SysAiVincentSetting vincentSetting = getOne(Wrappers.<SysAiVincentSetting>lambdaQuery().eq(SysAiVincentSetting::getSysAiModuleId, sysAiModuleId), false);

        if (null == vincentSetting) {
            return null;
        }

        List<SysAiVincentStyle> list = sysAiVincentStyleService.list(Wrappers.<SysAiVincentStyle>lambdaQuery().eq(SysAiVincentStyle::getSysAiModuleId, sysAiModuleId));
        vincentSetting.setStyles(list);
        return vincentSetting;
    }
}
