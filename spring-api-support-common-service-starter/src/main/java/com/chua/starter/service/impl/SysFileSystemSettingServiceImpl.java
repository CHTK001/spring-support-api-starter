package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.oss.FileStorage;
import com.chua.common.support.oss.impl.FileSystemFileStorage;
import com.chua.common.support.oss.setting.BucketSetting;
import com.chua.common.support.protocol.ServerSetting;
import com.chua.common.support.protocol.filter.FileStorageServletFilter;
import com.chua.common.support.protocol.filter.storage.FileStorageFactory;
import com.chua.common.support.protocol.server.ProtocolServer;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemSettingService;
import com.chua.starter.service.mapper.SysFileSystemSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * 文件服务器实例缓存
     */
    private static final ConcurrentHashMap<String, ProtocolServer> FILE_SERVER_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 文件服务器实例Key
     */
    private static final String FILE_SERVER_KEY = "file_system_server";

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

    @Override
    public ReturnResult<Boolean> startFileServerForFileSystem() {
        log.info("启动文件服务器");

        try {
            // 检查是否已启动
            if (FILE_SERVER_INSTANCES.containsKey(FILE_SERVER_KEY)) {
                ProtocolServer existingServer = FILE_SERVER_INSTANCES.get(FILE_SERVER_KEY);
                if (existingServer != null) {
                    log.warn("文件服务器已在运行中");
                    return ReturnResult.error("文件服务器已在运行中");
                }
            }

            // 获取配置
            SysFileSystemSetting setting = getEnabledSettingForFileSystem();
            if (setting == null) {
                log.error("未找到有效的文件系统配置");
                return ReturnResult.error("未找到有效的文件系统配置");
            }

            // 验证配置
            if (setting.getSysFileSystemSettingHttpServerPort() == null) {
                return ReturnResult.error("HTTP服务端口未配置");
            }
            if (StringUtils.isEmpty(setting.getSysFileSystemSettingStorageRootPath())) {
                return ReturnResult.error("文件存储根路径未配置");
            }

            // 检查端口是否可用
            Integer port = setting.getSysFileSystemSettingHttpServerPort();
            if (!checkPortAvailable(port)) {
                log.error("端口 {} 已被占用", port);
                return ReturnResult.error("端口 " + port + " 已被占用");
            }

            // 创建服务器实例
            ProtocolServer serverInstance = createFileServerInstance(setting);
            if (serverInstance == null) {
                return ReturnResult.error("创建文件服务器实例失败");
            }

            // 启动服务器
            serverInstance.start();

            // 保存实例
            FILE_SERVER_INSTANCES.put(FILE_SERVER_KEY, serverInstance);

            // 更新配置状态
            setting.setSysFileSystemSettingHttpServerEnabled(true);
            updateById(setting);

            log.info("文件服务器启动成功，端口: {}, 存储路径: {}", 
                    port, setting.getSysFileSystemSettingStorageRootPath());
            return ReturnResult.ok(true);

        } catch (Exception e) {
            log.error("启动文件服务器失败", e);
            return ReturnResult.error("启动文件服务器失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopFileServerForFileSystem() {
        log.info("停止文件服务器");

        try {
            ProtocolServer serverInstance = FILE_SERVER_INSTANCES.get(FILE_SERVER_KEY);
            if (serverInstance == null) {
                log.warn("文件服务器未运行");
                return ReturnResult.error("文件服务器未运行");
            }

            // 停止服务器
            serverInstance.stop();

            // 移除实例
            FILE_SERVER_INSTANCES.remove(FILE_SERVER_KEY);

            // 更新配置状态
            SysFileSystemSetting setting = getEnabledSettingForFileSystem();
            if (setting != null) {
                setting.setSysFileSystemSettingHttpServerEnabled(false);
                updateById(setting);
            }

            log.info("文件服务器停止成功");
            return ReturnResult.ok(true);

        } catch (Exception e) {
            log.error("停止文件服务器失败", e);
            return ReturnResult.error("停止文件服务器失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getFileServerStatusForFileSystem() {
        ProtocolServer serverInstance = FILE_SERVER_INSTANCES.get(FILE_SERVER_KEY);
        if (serverInstance != null) {
            return ReturnResult.ok("running");
        }
        return ReturnResult.ok("stopped");
    }

    @Override
    public ReturnResult<Boolean> checkPortAvailableForFileSystem(Integer port) {
        if (port == null || port < 1 || port > 65535) {
            return ReturnResult.error("端口号无效");
        }
        return ReturnResult.ok(checkPortAvailable(port));
    }

    /**
     * 创建文件服务器实例
     *
     * @param setting 配置
     * @return 服务器实例
     */
    private ProtocolServer createFileServerInstance(SysFileSystemSetting setting) {
        try {
            // 获取服务器配置
            String host = StringUtils.defaultString(setting.getSysFileSystemSettingHttpServerHost(), "0.0.0.0");
            Integer port = setting.getSysFileSystemSettingHttpServerPort();
            String contextPath = StringUtils.defaultString(setting.getSysFileSystemSettingHttpServerContext(), "/file");
            String storagePath = setting.getSysFileSystemSettingStorageRootPath();

            log.debug("创建文件服务器实例: host={}, port={}, context={}, storage={}",
                    host, port, contextPath, storagePath);

            // 创建服务器设置
            ServerSetting serverSetting = ServerSetting.builder()
                    .serverId("file_system_server")
                    .host(host)
                    .port(port)
                    .build();

            // 创建HTTP服务器实例
            ServiceProvider<ProtocolServer> serviceProvider = ServiceProvider.of(ProtocolServer.class);
            ProtocolServer serverInstance = serviceProvider.getNewExtension("http", serverSetting);

            if (serverInstance == null) {
                log.error("无法创建HTTP服务器实例");
                return null;
            }

            // 创建FileStorageSetting配置
            FileStorageFactory.FileStorageSetting fileStorageSetting = FileStorageFactory.FileStorageSetting.builder()
                    .openDownload(Boolean.TRUE.equals(setting.getSysFileSystemSettingDownloadEnabled()))
                    .openPreview(Boolean.TRUE.equals(setting.getSysFileSystemSettingPreviewEnabled()))
                    .openWebjars(false)
                    .openRemoteFile(false)
                    .build();

            // 创建FileStorageServletFilter
            FileStorageServletFilter fileStorageFilter = new FileStorageServletFilter(fileStorageSetting);

            // 添加本地文件存储
            BucketSetting bucketSetting = BucketSetting.builder()
                    .bucket(contextPath)
                    .endpoint(storagePath)
                    .build();
            FileStorage localStorage = new FileSystemFileStorage(bucketSetting);
            fileStorageFilter.addFileStorage(contextPath, localStorage);

            // 添加过滤器到服务器
            serverInstance.addFilter(fileStorageFilter);

            return serverInstance;

        } catch (Exception e) {
            log.error("创建文件服务器实例失败", e);
            return null;
        }
    }

    /**
     * 检查端口是否可用
     *
     * @param port 端口号
     * @return 是否可用
     */
    private boolean checkPortAvailable(Integer port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
