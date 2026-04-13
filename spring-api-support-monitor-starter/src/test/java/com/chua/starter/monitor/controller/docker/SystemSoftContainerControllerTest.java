package com.chua.starter.monitor.controller.docker;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.entity.SystemSoftContainerStatus;
import com.chua.starter.monitor.service.docker.SystemSoftContainerService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class SystemSoftContainerControllerTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                SystemSoftContainer.class
        );
    }

    private final SystemSoftContainerService systemSoftContainerService =
            Mockito.mock(SystemSoftContainerService.class);
    private final SystemSoftContainerController controller =
            new SystemSoftContainerController(systemSoftContainerService);

    @SuppressWarnings("unchecked")
    @Test
    void pageBuildsFilterWrapperForServerStatusAndKeyword() {
        Mockito.when(systemSoftContainerService.page(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>(2, 20));

        controller.page(2, 20, 4, 7, "error", "nginx");

        ArgumentCaptor<Page<SystemSoftContainer>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper<SystemSoftContainer>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        Mockito.verify(systemSoftContainerService)
                .page(pageCaptor.capture(), wrapperCaptor.capture());

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);

        AbstractWrapper<?, ?, ?> wrapper = wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment())
                .contains("system_server_id")
                .contains("system_soft_id")
                .contains("system_soft_container_status")
                .contains("system_soft_container_name")
                .contains("system_soft_container_image")
                .contains("system_soft_container_docker_id");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains(4, 7, "%nginx%")
                .contains(SystemSoftContainerStatus.CREATE_FAILED)
                .contains(SystemSoftContainerStatus.START_FAILED)
                .contains(SystemSoftContainerStatus.RESTART_FAILED)
                .contains(SystemSoftContainerStatus.STOP_FAILED)
                .contains(SystemSoftContainerStatus.DELETE_FAILED)
                .contains(SystemSoftContainerStatus.DEAD);
    }
}
