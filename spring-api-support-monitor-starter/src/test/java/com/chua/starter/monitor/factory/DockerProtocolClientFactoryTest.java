package com.chua.starter.monitor.factory;

import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockerProtocolClientFactoryTest {

    private final MonitorSysGenServerSettingService serverSettingService =
            Mockito.mock(MonitorSysGenServerSettingService.class);
    private final MonitorSysGenServerService serverService =
            Mockito.mock(MonitorSysGenServerService.class);

    private final DockerProtocolClientFactory factory =
            new DockerProtocolClientFactory(serverSettingService, serverService);

    @Test
    void createUnixSocketClientUsesUnixUrlInsteadOfHostHack() throws Exception {
        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(4);
        setting.setMonitorSysGenServerSettingDockerSocketPath("/var/run/docker.sock");
        setting.setMonitorSysGenServerSettingDockerConnectTimeoutMillis(15_000);

        DockerProtocolClient client = invokePrivate(factory, "createUnixSocketClient",
                new Class<?>[]{MonitorSysGenServerSetting.class}, setting);

        assertThat(readDockerHost(client)).isEqualTo("unix:///var/run/docker.sock");
    }

    @Test
    void createShellClientFallsBackToUnixSocketForLoopbackHost(@TempDir Path tempDir) throws Exception {
        Path socketFile = Files.createFile(tempDir.resolve("docker.sock"));

        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(5);
        setting.setMonitorSysGenServerSettingDockerSocketPath(socketFile.toString());
        setting.setMonitorSysGenServerSettingDockerConnectTimeoutMillis(20_000);

        MonitorSysGenServer server = new MonitorSysGenServer();
        server.setMonitorSysGenServerId(5);
        server.setMonitorSysGenServerName("docker-local-shell-temp");
        server.setMonitorSysGenServerHost("127.0.0.1");
        server.setMonitorSysGenServerPort(22);
        server.setMonitorSysGenServerUsername("root");
        server.setMonitorSysGenServerPassword("secret");
        server.setMonitorSysGenServerTimeout(20_000);

        Mockito.when(serverService.getByServerId(5)).thenReturn(server);

        DockerProtocolClient client = invokePrivate(factory, "createShellClient",
                new Class<?>[]{MonitorSysGenServerSetting.class}, setting);

        assertThat(readDockerHost(client)).startsWith("unix://");
        assertThat(readDockerHost(client)).contains("docker.sock");
    }

    @Test
    void createShellClientRejectsRemoteHostWhenOnlyBrokenShellModeIsAvailable() {
        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(3);

        MonitorSysGenServer server = new MonitorSysGenServer();
        server.setMonitorSysGenServerId(3);
        server.setMonitorSysGenServerName("docker-remote");
        server.setMonitorSysGenServerHost("192.168.110.100");
        server.setMonitorSysGenServerPort(22);
        server.setMonitorSysGenServerUsername("root");
        server.setMonitorSysGenServerPassword("secret");
        server.setMonitorSysGenServerTimeout(20_000);

        Mockito.when(serverService.getByServerId(3)).thenReturn(server);

        assertThatThrownBy(() -> invokePrivate(factory, "createShellClient",
                new Class<?>[]{MonitorSysGenServerSetting.class}, setting))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SHELL模式不可用");
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return (T) method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    private static String readDockerHost(DockerProtocolClient client) throws Exception {
        Field field = DockerProtocolClient.class.getDeclaredField("dockerHost");
        field.setAccessible(true);
        return (String) field.get(client);
    }
}
