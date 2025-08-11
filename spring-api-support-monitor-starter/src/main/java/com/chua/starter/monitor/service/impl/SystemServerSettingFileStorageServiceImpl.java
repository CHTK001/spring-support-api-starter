package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemServerSettingFileStorage;
import com.chua.starter.monitor.mapper.SystemServerSettingFileStorageMapper;
import com.chua.starter.monitor.service.SystemServerSettingFileStorageService;
import com.chua.starter.monitor.service.SystemServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemServerSettingFileStorageServiceImpl extends ServiceImpl<SystemServerSettingFileStorageMapper, SystemServerSettingFileStorage>
        implements SystemServerSettingFileStorageService {

    private final SystemServerSettingService systemServerSettingService;

    @Override
    public List<SystemServerSettingFileStorage> listByServerId(Integer serverId) {
        return lambdaQuery().eq(SystemServerSettingFileStorage::getFileStorageServerId, serverId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> replaceAllForServer(Integer serverId, List<SystemServerSettingFileStorage> configs) {
        try {
            // 先删除旧配置
            lambdaUpdate().eq(SystemServerSettingFileStorage::getFileStorageServerId, serverId).remove();
            // 批量插入新配置
            if (configs != null && !configs.isEmpty()) {
                for (SystemServerSettingFileStorage cfg : configs) {
                    cfg.setFileStorageServerId(serverId);
                }
                saveBatch(configs);
            }
            // 保存后热应用
            systemServerSettingService.applyConfigToRunningServer(serverId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("保存文件存储配置失败", e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> saveOne(SystemServerSettingFileStorage config) {
        try {
            saveOrUpdate(config);
            systemServerSettingService.applyConfigToRunningServer(config.getFileStorageServerId());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            return ReturnResult.error(e.getMessage());
        }
    }
}


