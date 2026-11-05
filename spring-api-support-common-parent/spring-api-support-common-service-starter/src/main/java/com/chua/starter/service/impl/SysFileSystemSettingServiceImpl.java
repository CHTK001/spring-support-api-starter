// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.server.ServerSetting;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import com.chua.starter.service.mapper.SysFileSystemSettingMapper;
import com.chua.starter.service.server.FileHttpServer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 文件系统配置服务实现类
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
/**
 * Sys文件SystemSetting服务Impl类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class SysFileSystemSettingServiceImpl extends ServiceImpl<SysFileSystemSettingMapper, SysFileSystemSetting> implements SysFileSystemSettingService {

    private volatile FileHttpServer fileHttpServer; // 文件http服务端

    /**
     * SYS文件系统服务
     * 仅 startFileServer 运行期使用；构造期依赖会与本服务成环，故延迟注入
     */
    @Lazy
    @Autowired
    private SysFileSystemService sysFileSystemService;

    @Override
    /**
     * 获取已启用settingfor文件系统
    */
    public SysFileSystemSetting getEnabledSettingForFileSystem() {
        return getOne(Wrappers.<SysFileSystemSetting>lambdaQuery()
                .eq(SysFileSystemSetting::getSysFileSystemSettingEnabled, true)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 启用settingfor文件系统
     *
     * @param settingId settingId
     * @return enablesettingfor文件系统的结果
     */
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
    /**
     * 初始化默认settingfor文件系统
     *
     * @return 初始化默认settingfor文件系统的结果
     */
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
    /**
     * 开始文件服务端for文件系统
    */
    public synchronized ReturnResult<Boolean> startFileServerForFileSystem() {
        if (fileHttpServer != null && fileHttpServer.isRunning()) {
            return ReturnResult.ok(true);
        }
        SysFileSystemSetting setting = getEnabledSettingForFileSystem();
        if (setting == null) {
            return ReturnResult.error("未找到启用的配置");
        }
        String host = setting.getSysFileSystemSettingHttpServerHost();
        Integer port = setting.getSysFileSystemSettingHttpServerPort();
        String context = setting.getSysFileSystemSettingHttpServerContext();
        if (host == null || host.isEmpty()) {
            host = "0.0.0.0";
        }
        if (port == null || port <= 0) {
            return ReturnResult.error("HTTP 服务端口未配置");
        }
        if (context == null || context.isEmpty()) {
            context = "/";
        }
        try {
            ServerSetting serverSetting = FileHttpServer.defaultSetting(host, port, context);
            fileHttpServer = new FileHttpServer(
                    sysFileSystemService,
                    this,
                    serverSetting,
                    "./temp/filesystem");
            fileHttpServer.start();
            log.info("文件服务器启动: host={} port={} context={}", host, port, context);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("文件服务器启动失败", e);
            return ReturnResult.error("文件服务器启动失败: " + e.getMessage());
        }
    }

    @Override
    /**
     * 停止文件服务端for文件系统
    */
    public synchronized ReturnResult<Boolean> stopFileServerForFileSystem() {
        if (fileHttpServer == null) {
            return ReturnResult.ok(true);
        }
        try {
            fileHttpServer.stop();
            fileHttpServer = null;
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("文件服务器停止失败", e);
            return ReturnResult.error("文件服务器停止失败: " + e.getMessage());
        }
    }

    @Override
    /**
     * 获取文件服务端状态for文件系统
    */
    public ReturnResult<String> getFileServerStatusForFileSystem() {
        if (fileHttpServer != null && fileHttpServer.isRunning()) {
            return ReturnResult.ok("running");
        }
        return ReturnResult.ok("stopped");
    }

    @Override
    /**
     * 校验端口可用for文件系统
    */
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

    /**
     * Spring 关闭时停止内嵌 HTTP 文件服务器。
     */
    @PreDestroy
    public void onShutdown() {
        if (fileHttpServer != null) {
            try {
                fileHttpServer.stop();
            } catch (Exception e) {
                log.debug("[sys-fs] 停止文件服务失败，已忽略: {}", e.getMessage());
            }
        }
    }
}