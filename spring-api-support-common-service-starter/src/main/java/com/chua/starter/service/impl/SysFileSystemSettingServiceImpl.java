package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemSettingService;
import com.chua.starter.service.mapper.SysFileSystemSettingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 文件系统配置服务实现类
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Service
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
        update(Wrappers.<SysFileSystemSetting>lambdaUpdate()
                .set(SysFileSystemSetting::getSysFileSystemSettingEnabled, false));
        boolean result = update(Wrappers.<SysFileSystemSetting>lambdaUpdate()
                .set(SysFileSystemSetting::getSysFileSystemSettingEnabled, true)
                .eq(SysFileSystemSetting::getSysFileSystemSettingId, settingId));
        return result ? ReturnResult.ok(true) : ReturnResult.error("启用配置失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> initDefaultSettingForFileSystem() {
        log.info("初始化默认配置");
        long count = count();
        if (count > 0) {
            return ReturnResult.ok(true);
        }
        SysFileSystemSetting defaultSetting = SysFileSystemSetting.getDefaultSetting();
        boolean result = save(defaultSetting);
        return result ? ReturnResult.ok(true) : ReturnResult.error("初始化默认配置失败");
    }

    @Override
    public ReturnResult<Boolean> startFileServerForFileSystem() {
        // TODO 文件服务器将在 filesystem-starter 中实现，使用 common-starter 的 Server
        return ReturnResult.error("文件服务器功能将在 filesystem-starter 中实现");
    }

    @Override
    public ReturnResult<Boolean> stopFileServerForFileSystem() {
        // TODO 文件服务器将在 filesystem-starter 中实现，使用 common-starter 的 Server
        return ReturnResult.error("文件服务器功能将在 filesystem-starter 中实现");
    }

    @Override
    public ReturnResult<String> getFileServerStatusForFileSystem() {
        return ReturnResult.ok("stopped");
    }

    @Override
    public ReturnResult<Boolean> checkPortAvailableForFileSystem(Integer port) {
        if (port == null || port < 1 || port > 65535) {
            return ReturnResult.error("端口号无效");
        }
        try (ServerSocket socket = new ServerSocket(port)) {
            return ReturnResult.ok(true);
        } catch (IOException e) {
            return ReturnResult.ok(false);
        }
    }
}
