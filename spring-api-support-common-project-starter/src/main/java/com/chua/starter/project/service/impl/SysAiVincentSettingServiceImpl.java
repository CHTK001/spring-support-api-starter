package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.mapper.SysAiVincentSettingMapper;
import com.chua.starter.project.service.SysAiVincentSettingService;
import com.chua.starter.project.service.SysAiVincentStyleService;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysAiVincentSetting;
import com.chua.starter.system.entity.SysAiVincentStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 文生图模型参数服务实现类
 *
 * @author CH
 * @since 2025-05-09
 */
@Service
@RequiredArgsConstructor
public class SysAiVincentSettingServiceImpl extends ServiceImpl<SysAiVincentSettingMapper, SysAiVincentSetting> implements SysAiVincentSettingService {

    /**
     * 事务模板，用于执行事务操作
     */
    final TransactionTemplate transactionTemplate;

    /**
     * 文生图风格服务接口，用于操作SysAiVincentStyle实体
     */
    final SysAiVincentStyleService sysAiVincentStyleService;

    /**
     * 根据模块ID删除模块设置
     *
     * @param sysAiModuleId 模块ID，用于标识要删除设置的模块。例如：123
     * @return ReturnResult<Boolean> 返回删除操作的结果，包括是否删除成功
     */
    @Override
    public ReturnResult<Boolean> deleteForModuleSetting(Integer sysAiModuleId) {
        return transactionTemplate.execute(status -> {
            sysAiVincentStyleService.remove(Wrappers.<SysAiVincentStyle>lambdaQuery().eq(SysAiVincentStyle::getSysAiModuleId, sysAiModuleId));
            return ReturnResult.ok(
                    this.remove(Wrappers.<SysAiVincentSetting>lambdaUpdate().eq(SysAiVincentSetting::getSysAiModuleId, sysAiModuleId))
            );
        });
    }

    /**
     * 保存模块设置
     *
     * @param sysAiVincentSetting 模块设置对象，包含要保存的设置信息。
     *                            例如：{
     *                            "sysAiModuleId": 123,
     *                            "sysAiVincentSupportedSize": "512x512",
     *                            "sysAiVincentSupportedMaxSize": 1024,
     *                            "sysAiVincentSupportedStyle": "anime",
     *                            "sysAiVincentSupportedNumber": 4,
     *                            "sysAiVincentSupportAsync": 1,
     *                            "sysAiVincentSupportAudio": 0,
     *                            "sysAiVincentSupportedQuality": "high",
     *                            "sysAiVincentSupportedFps": "24",
     *                            "sysAiVincentSupportRefImage": 1
     *                            }
     * @return ReturnResult<SysAiModule> 返回保存操作的结果，包括保存后的模块信息
     */
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

    /**
     * 更新模块设置
     *
     * @param sysAiVincentSetting 模块设置对象，包含要更新的设置信息。
     *                            例如：{
     *                            "sysAiVincentId": 456,
     *                            "sysAiModuleId": 123,
     *                            "sysAiVincentSupportedSize": "512x512",
     *                            "sysAiVincentSupportedMaxSize": 1024,
     *                            "sysAiVincentSupportedStyle": "anime",
     *                            "sysAiVincentSupportedNumber": 4,
     *                            "sysAiVincentSupportAsync": 1,
     *                            "sysAiVincentSupportAudio": 0,
     *                            "sysAiVincentSupportedQuality": "high",
     *                            "sysAiVincentSupportedFps": "24",
     *                            "sysAiVincentSupportRefImage": 1
     *                            }
     * @return ReturnResult<Boolean> 返回更新操作的结果，包括是否更新成功
     */
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

    /**
     * 根据模块ID获取配置信息
     *
     * @param sysAiModuleId 模块ID，用于标识要获取配置信息的模块。例如："123"
     * @return SysAiVincentSetting 返回模块的配置信息
     */
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
