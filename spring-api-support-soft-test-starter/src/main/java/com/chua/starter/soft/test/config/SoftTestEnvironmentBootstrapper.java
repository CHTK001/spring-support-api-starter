package com.chua.starter.soft.test.config;

import com.chua.starter.server.support.entity.ServerHost;
import com.chua.starter.server.support.service.ServerHostService;
import com.chua.starter.soft.support.entity.SoftTarget;
import com.chua.starter.soft.support.service.SoftManagementService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@Order(0)
public class SoftTestEnvironmentBootstrapper implements ApplicationRunner {

    private static final List<String> RESET_TABLES = List.of(
            "soft_config_snapshot",
            "soft_operation_log",
            "soft_installation",
            "server_service",
            "soft_target",
            "server_host"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ServerHostService serverHostService;
    private final SoftManagementService softManagementService;
    private final Environment environment;

    public SoftTestEnvironmentBootstrapper(
            JdbcTemplate jdbcTemplate,
            ServerHostService serverHostService,
            SoftManagementService softManagementService,
            Environment environment
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.serverHostService = serverHostService;
        this.softManagementService = softManagementService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.getProperty("soft.test.bootstrap.enabled", Boolean.class, Boolean.TRUE)) {
            log.info("[soft-test] 环境引导已禁用");
            return;
        }
        if (environment.getProperty("soft.test.bootstrap.reset-state", Boolean.class, Boolean.TRUE)) {
            resetRuntimeState();
        }
        bootstrapSoftTargets(bootstrapServerHosts());
    }

    private void resetRuntimeState() {
        for (String table : RESET_TABLES) {
            jdbcTemplate.execute("TRUNCATE TABLE `" + table + "`");
        }
        log.info("[soft-test] 已重置测试运行态表: {}", RESET_TABLES);
    }

    private List<ServerHost> bootstrapServerHosts() {
        List<ServerHost> hosts = new ArrayList<>();
        hosts.add(serverHostService.saveHost(localServerHost()));
        if (hasRemotePassword()) {
            hosts.add(serverHostService.saveHost(remoteServerHost()));
            return hosts;
        }
        log.warn("[soft-test] 未检测到远程主机密码，跳过远程 server_host 初始化");
        return hosts;
    }

    private void bootstrapSoftTargets(List<ServerHost> hosts) {
        for (ServerHost host : hosts) {
            softManagementService.saveTarget(buildSoftTarget(host));
        }
    }

    private ServerHost localServerHost() {
        ServerHost host = new ServerHost();
        host.setServerName("本机 Windows");
        host.setServerType("LOCAL");
        host.setOsType(detectLocalOs());
        host.setArchitecture(detectArchitecture());
        host.setHost("127.0.0.1");
        host.setPort(0);
        host.setUsername(currentUsername());
        host.setPassword(null);
        host.setPrivateKey(null);
        host.setBaseDirectory(localBaseDirectory());
        host.setTags("soft,local,windows");
        host.setEnabled(Boolean.TRUE);
        host.setDescription("soft-test 本机真实联调入口");
        host.setMetadataJson("{\"softEnabled\":true,\"displayMode\":\"default\"}");
        return host;
    }

    private ServerHost remoteServerHost() {
        ServerHost host = new ServerHost();
        host.setServerName("远程 Linux " + remoteHost());
        host.setServerType("SSH");
        host.setOsType("LINUX");
        host.setArchitecture("AMD64");
        host.setHost(remoteHost());
        host.setPort(remotePort());
        host.setUsername(remoteUsername());
        host.setPassword(remotePassword());
        host.setPrivateKey(null);
        host.setBaseDirectory(remoteBaseDirectory());
        host.setTags("soft,remote,linux,ssh");
        host.setEnabled(Boolean.TRUE);
        host.setDescription("soft-test 远程 Linux 真实联调入口");
        host.setMetadataJson("{\"softEnabled\":true,\"displayMode\":\"default\"}");
        return host;
    }

    private SoftTarget buildSoftTarget(ServerHost host) {
        SoftTarget target = new SoftTarget();
        boolean local = "LOCAL".equalsIgnoreCase(host.getServerType());
        target.setTargetName(host.getServerName());
        target.setTargetCode("server-" + host.getServerId());
        target.setTargetType(host.getServerType());
        target.setOsType(host.getOsType());
        target.setArchitecture(host.getArchitecture());
        target.setHost(local ? "127.0.0.1" : host.getHost());
        target.setPort(host.getPort());
        target.setUsername(host.getUsername());
        target.setPassword(host.getPassword());
        target.setPrivateKey(host.getPrivateKey());
        target.setBaseDirectory(host.getBaseDirectory());
        target.setEnabled(Boolean.TRUE);
        target.setDescription(local ? "soft-test 本机真实安装目标" : "soft-test 远程 Linux 真实安装目标");
        target.setMetadataJson("{\"source\":\"soft-test-bootstrap\",\"serverId\":"
                + host.getServerId()
                + ",\"serverCode\":\""
                + host.getServerCode()
                + "\"}");
        return target;
    }

    private boolean hasRemotePassword() {
        return StringUtils.hasText(remotePassword());
    }

    private String remoteHost() {
        return environment.getProperty("soft.test.remote.host", "8.139.4.229");
    }

    private Integer remotePort() {
        return environment.getProperty("soft.test.remote.port", Integer.class, 22);
    }

    private String remoteUsername() {
        return environment.getProperty("soft.test.remote.username", "root");
    }

    private String remotePassword() {
        return environment.getProperty("soft.test.remote.password");
    }

    private String remoteBaseDirectory() {
        return environment.getProperty("soft.test.remote.base-directory", "/opt/soft-runtime");
    }

    private String localBaseDirectory() {
        return environment.getProperty("soft.test.local.base-directory", "H:/workspace/2/tmp/soft-runtime/local");
    }

    private String currentUsername() {
        String username = environment.getProperty("soft.test.local.username");
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        username = System.getenv("USERNAME");
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        return System.getProperty("user.name", "local");
    }

    private String detectLocalOs() {
        String osName = System.getProperty("os.name", "");
        if (osName.toLowerCase().contains("win")) {
            return "WINDOWS";
        }
        if (osName.toLowerCase().contains("mac")) {
            return "MACOS";
        }
        return "LINUX";
    }

    private String detectArchitecture() {
        String arch = System.getProperty("os.arch", "");
        if ("amd64".equalsIgnoreCase(arch) || "x86_64".equalsIgnoreCase(arch)) {
            return "AMD64";
        }
        if ("aarch64".equalsIgnoreCase(arch) || "arm64".equalsIgnoreCase(arch)) {
            return "ARM64";
        }
        return arch.toUpperCase();
    }
}
