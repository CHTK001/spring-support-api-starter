package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemSettingService;
import com.chua.starter.service.mapper.SysFileSystemSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件系统配置服务实现类
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileSystemSettingServiceImpl extends ServiceImpl<SysFileSystemSettingMapper, SysFileSystemSetting> implements SysFileSystemSettingService {

    @Override
    public SysFileSystemSetting getEnabledSettingForFileSystem() {
        return getOne(Wrappers.<SysFileSystemSetting>lambdaQuery()
                .eq(SysFileSystemSetting::getSysFileSystemSettingEnabled, true)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> enableSettingForFileSystem(Integer settingId) {
        log.info("启用配置: settingId={}", settingId);

        // 先禁用所有配置
        update(Wrappers.<SysFileSystemSetting>lambdaUpdate()
                .set(SysFileSystemSetting::getSysFileSystemSettingEnabled, false));

        // 启用指定配置
        boolean result = update(Wrappers.<SysFileSystemSetting>lambdaUpdate()
                .set(SysFileSystemSetting::getSysFileSystemSettingEnabled, true)
                .eq(SysFileSystemSetting::getSysFileSystemSettingId, settingId));

        return result ? ReturnResult.ok(true) : ReturnResult.error("启用配置失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> initDefaultSettingForFileSystem() {
        log.info("初始化默认配置");

        // 检查是否已有配置
        long count = count();
        if (count > 0) {
            log.info("已存在配置，跳过初始化");
            return ReturnResult.ok(true);
        }

        // 创建默认配置
        SysFileSystemSetting defaultSetting = SysFileSystemSetting.getDefaultSetting();
        boolean result = save(defaultSetting);

        return result ? ReturnResult.ok(true) : ReturnResult.error("初始化默认配置失败");
    }
}
