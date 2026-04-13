package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.SystemSoftImageMapper;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.pojo.docker.DockerCreateContainerRequest;
import com.chua.starter.monitor.service.docker.DockerCallbackService;
import com.chua.starter.monitor.service.docker.SystemSoftContainerService;
import com.chua.starter.monitor.service.docker.SystemSoftRecordService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSoftImageServiceImplTest {

    private final SystemSoftImageMapper systemSoftImageMapper = mock(SystemSoftImageMapper.class);
    private final DockerProtocolClientFactory dockerProtocolClientFactory = mock(DockerProtocolClientFactory.class);
    private final SystemSoftMapper systemSoftMapper = mock(SystemSoftMapper.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
    private final DockerCallbackService dockerCallbackService = mock(DockerCallbackService.class);
    private final SystemSoftRecordService systemSoftRecordService = mock(SystemSoftRecordService.class);
    private final SystemSoftContainerService systemSoftContainerService = mock(SystemSoftContainerService.class);
    private final DockerProtocolClient dockerProtocolClient = mock(DockerProtocolClient.class);
    private final DockerClient dockerClient = mock(DockerClient.class);
    private final CreateContainerCmd createContainerCmd = mock(CreateContainerCmd.class);
    private final CreateContainerResponse createContainerResponse = mock(CreateContainerResponse.class);
    private final SocketSessionTemplate socketSessionTemplate = mock(SocketSessionTemplate.class);

    private SystemSoftImageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SystemSoftImageServiceImpl(
                systemSoftImageMapper,
                dockerProtocolClientFactory,
                systemSoftMapper,
                transactionTemplate
        );
        ReflectionTestUtils.setField(service, "dockerCallbackService", dockerCallbackService);
        ReflectionTestUtils.setField(service, "systemSoftRecordService", systemSoftRecordService);
        ReflectionTestUtils.setField(service, "systemSoftContainerService", systemSoftContainerService);

        when(dockerCallbackService.getSocketSessionTemplate()).thenReturn(socketSessionTemplate);
        when(systemSoftRecordService.save(any())).thenReturn(true);
        when(systemSoftRecordService.updateById(any())).thenReturn(true);
        when(systemSoftContainerService.syncContainerStatus(any())).thenReturn(ReturnResult.ok(1));
        when(dockerProtocolClientFactory.createClient(3)).thenReturn(dockerProtocolClient);
        when(dockerProtocolClient.getDockerClient()).thenReturn(dockerClient);
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
    }

    @Test
    void createContainerShouldResolveImageNameAndAutoStartByDefault() {
        SystemSoftImage image = new SystemSoftImage();
        image.setSystemSoftId(11);
        image.setSystemSoftImageServerId(3);
        image.setSystemSoftImageName("redis");
        image.setSystemSoftImageTag("7.2");
        when(systemSoftImageMapper.selectById(1)).thenReturn(image);
        when(createContainerResponse.getId()).thenReturn("container-001");
        when(dockerProtocolClient.startContainerById("container-001")).thenReturn(true);

        DockerCreateContainerRequest request = new DockerCreateContainerRequest();
        request.setContainerName("monitor-test-redis");

        ReturnResult<Boolean> result = service.createContainer(1, request);

        assertThat(result.isOk()).isTrue();
        verify(dockerClient).createContainerCmd("redis:7.2");
        verify(dockerProtocolClient).startContainerById("container-001");
        verify(systemSoftRecordService).updateById(any());
    }

    @Test
    void createContainerShouldSkipStartWhenAutoStartDisabled() {
        SystemSoftImage image = new SystemSoftImage();
        image.setSystemSoftId(12);
        image.setSystemSoftImageServerId(3);
        image.setSystemSoftImageName("nginx");
        image.setSystemSoftImageTag("1.27");
        when(systemSoftImageMapper.selectById(2)).thenReturn(image);
        when(createContainerResponse.getId()).thenReturn("container-002");

        DockerCreateContainerRequest request = new DockerCreateContainerRequest();
        request.setContainerName("monitor-test-nginx");
        request.setAutoStart(false);

        ReturnResult<Boolean> result = service.createContainer(2, request);

        assertThat(result.isOk()).isTrue();
        verify(dockerClient).createContainerCmd("nginx:1.27");
        verify(dockerProtocolClient, never()).startContainerById(anyString());
    }
}
