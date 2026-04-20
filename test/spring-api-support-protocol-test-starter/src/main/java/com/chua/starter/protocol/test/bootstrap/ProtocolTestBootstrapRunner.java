package com.chua.starter.protocol.test.bootstrap;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.proxy.support.entity.SystemServer;
import com.chua.starter.proxy.support.entity.SystemServerSetting;
import com.chua.starter.proxy.support.entity.SystemServerSettingFileStorage;
import com.chua.starter.proxy.support.service.server.SystemServerService;
import com.chua.starter.proxy.support.service.server.SystemServerSettingFileStorageService;
import com.chua.starter.proxy.support.service.server.SystemServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProtocolTestBootstrapRunner implements CommandLineRunner {

    private static final String DEMO_SERVER_NAME = "protocol-demo-http-proxy";

    private final SystemServerService systemServerService;
    private final SystemServerSettingService systemServerSettingService;
    private final SystemServerSettingFileStorageService fileStorageService;

    @Value("${protocol.test.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${protocol.test.bootstrap.auto-start:true}")
    private boolean autoStart;

    @Value("${protocol.test.proxy.port:19190}")
    private int proxyPort;

    @Value("${protocol.test.file-storage-path:${user.dir}/.runtime/protocol-test-storage}")
    private String fileStoragePath;

    @Override
    public void run(String... args) throws Exception {
        if (!bootstrapEnabled) {
            return;
        }

        Path storageDir = Path.of(fileStoragePath).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);
        Path sampleFile = storageDir.resolve("hello.txt");
        if (!Files.exists(sampleFile)) {
            Files.writeString(sampleFile,
                    "protocol-file-storage-sample@" + Instant.now(),
                    StandardCharsets.UTF_8);
        }

        SystemServer server = systemServerService.list(
                        Wrappers.<SystemServer>lambdaQuery()
                                .eq(SystemServer::getSystemServerName, DEMO_SERVER_NAME)
                ).stream()
                .findFirst()
                .orElseGet(() -> {
                    SystemServer entity = new SystemServer();
                    entity.setSystemServerName(DEMO_SERVER_NAME);
                    entity.setSystemServerHost("0.0.0.0");
                    entity.setSystemServerPort(proxyPort);
                    entity.setSystemServerType("HTTP");
                    entity.setSystemServerStatus(SystemServer.SystemServerStatus.STOPPED);
                    entity.setSystemServerDescription("protocol test http server");
                    entity.setSystemServerAutoStart(false);
                    entity.setSystemServerMaxConnections(2048);
                    entity.setSystemServerTimeout(10000);
                    systemServerService.save(entity);
                    return entity;
                });

        server.setSystemServerHost("0.0.0.0");
        server.setSystemServerPort(proxyPort);
        server.setSystemServerType("HTTP");
        if (server.getSystemServerStatus() == null) {
            server.setSystemServerStatus(SystemServer.SystemServerStatus.STOPPED);
        }
        systemServerService.updateById(server);

        Integer serverId = server.getSystemServerId();
        SystemServerSetting fileStorageSetting = ensureFilter(serverId, "fileStorage");

        if (fileStorageSetting != null) {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("openSetting", true);
            config.put("openPlugin", true);
            config.put("openRange", true);
            config.put("openPreview", true);
            config.put("openDownload", true);
            config.put("openWebjars", true);
            config.put("openRemoteFile", false);
            config.put("openWatermark", false);
            config.put("plugins", "");
            config.put("settings", "");
            // FileStorageServletFilterHandler 从 setting_item 表读取配置，必须走 updateServletFilterConfig
            systemServerSettingService.updateServletFilterConfig(fileStorageSetting.getSystemServerSettingId(), config);

            List<SystemServerSettingFileStorage> storages = fileStorageService.listByServerId(serverId);
            SystemServerSettingFileStorage storage = storages.isEmpty() ? new SystemServerSettingFileStorage() : storages.get(0);
            storage.setFileStorageServerId(serverId);
            storage.setFileStorageType("FILESYSTEM");
            storage.setFileStorageBucket("local");
            storage.setFileStorageEndpoint(storageDir.toString());
            storage.setFileStorageBasePath("/");
            // FILESYSTEM 实现底层会校验 accessKey，测试环境使用固定占位值
            storage.setFileStorageAccessKey("local");
            storage.setFileStorageSecretKey("local");
            storage.setFileStorageEnabled(true);
            storage.setFileStorageConnectionTimeout(5000L);
            fileStorageService.saveOne(storage);
        }

        // 这几类是协议页面上常见的动态代理相关过滤器，能装则装，装不上则跳过
        ensureFilter(serverId, "enhancedProxy");
        ensureFilter(serverId, "proxyResponse");
        ensureFilter(serverId, "dynamicExpression");

        if (autoStart) {
            ReturnResult<Boolean> startResult = systemServerService.startServer(serverId);
            if (!startResult.isOk()) {
                log.warn("协议测试代理服务启动返回: {}", startResult.getMsg());
            }
        }

        log.info("Protocol 测试环境初始化完成: serverId={}, proxyPort={}, fileStoragePath={}",
                serverId, proxyPort, storageDir);
    }

    private SystemServerSetting ensureFilter(Integer serverId, String filterType) {
        ReturnResult<List<SystemServerSetting>> result = systemServerSettingService.getByServerId(serverId);
        if (result.isOk() && result.getData() != null) {
            for (SystemServerSetting item : result.getData()) {
                if (filterType.equalsIgnoreCase(item.getSystemServerSettingType())) {
                    return item;
                }
            }
        }

        ReturnResult<SystemServerSetting> installResult = systemServerSettingService.installServletFilter(serverId, filterType);
        if (installResult.isOk()) {
            return installResult.getData();
        }
        log.debug("安装过滤器跳过: type={}, msg={}", filterType, installResult.getMsg());
        return null;
    }
}
