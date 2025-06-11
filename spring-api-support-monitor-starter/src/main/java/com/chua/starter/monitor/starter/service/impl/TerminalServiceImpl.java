package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.protocol.ClientSetting;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.jpom.func.assets.model.MachineSshModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineSshServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import com.chua.starter.monitor.starter.service.MonitorSysGenService;
import com.chua.starter.monitor.starter.service.TerminalService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author CH
 * @since 2024/6/19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {

    private static final Map<String, CacheClient> SERVER_MAP = new ConcurrentHashMap<>();
    final MonitorSysGenService monitorSysGenService;
    final MachineSshServer machineSshServer;

    @Override
    public SshClient getClient(String genId) {
        MonitorSysGen monitorSysGen = monitorSysGenService.getById(genId);
        CacheClient cacheClient = SERVER_MAP.get(genId);
        if (null == cacheClient) {
            SshClient sshClient = createClient(monitorSysGen);
            SERVER_MAP.put(genId, new CacheClient(sshClient));
            return sshClient;
        }
        return cacheClient.getSshClient();
    }

    @Override
    public SshClient createClientBySshId(String sshId) {
        if (sshId == null || sshId.isEmpty()) {
            log.error("sshId不能为空");
            return null;
        }

        // 检查缓存
        CacheClient cacheClient = SERVER_MAP.get("ssh_" + sshId);
        if (null != cacheClient) {
            return cacheClient.getSshClient();
        }

        try {
            // 通过sshId获取MachineSshModel
            MachineSshModel machineSshModel = machineSshServer.getByKeyFull(sshId, true, null);

            // 创建SshClient
            SshClient sshClient = new SshClient(ClientSetting.builder()
                    .username(machineSshModel.getUser())
                    .password(machineSshModel.getPassword())
                    .host(machineSshModel.getHost())
                    .port(machineSshModel.getPort())
                    .build()
            );
            sshClient.connect();
            // 缓存客户端
            SERVER_MAP.put("ssh_" + sshId, new CacheClient(sshClient));
            return sshClient;
        } catch (Exception e) {
            log.error("创建SSH客户端失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private SshClient createClient(MonitorSysGen monitorSysGen) {
        SshClient sshClient = new SshClient(ClientSetting.builder()
                .username(monitorSysGen.getGenUser())
                .password(monitorSysGen.getGenPassword())
                .host(monitorSysGen.getGenHost())
                .port(monitorSysGen.getGenPort())
                .build()
        );
        sshClient.connect();
        return sshClient;
    }

    @Data
    @AllArgsConstructor
    static class CacheClient {
        private SshClient sshClient;
    }
}
