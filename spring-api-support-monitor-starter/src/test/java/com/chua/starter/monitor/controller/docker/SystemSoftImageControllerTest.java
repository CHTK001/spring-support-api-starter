package com.chua.starter.monitor.controller.docker;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftImageStatus;
import com.chua.starter.monitor.service.docker.SystemSoftImageService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class SystemSoftImageControllerTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                SystemSoftImage.class
        );
    }

    private final SystemSoftImageService systemSoftImageService =
            Mockito.mock(SystemSoftImageService.class);
    private final SystemSoftImageController controller =
            new SystemSoftImageController(systemSoftImageService);

    @SuppressWarnings("unchecked")
    @Test
    void pageBuildsFilterWrapperForServerStatusAndKeyword() {
        Mockito.when(systemSoftImageService.page(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>(1, 10));

        controller.page(1, 10, 4, 7, SystemSoftImageStatus.PULLING, "nginx");

        ArgumentCaptor<Page<SystemSoftImage>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper<SystemSoftImage>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        Mockito.verify(systemSoftImageService)
                .page(pageCaptor.capture(), wrapperCaptor.capture());

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(10);

        AbstractWrapper<?, ?, ?> wrapper = wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment())
                .contains("system_soft_image_server_id")
                .contains("system_soft_id")
                .contains("system_soft_image_status")
                .contains("system_soft_image_name")
                .contains("system_soft_image_tag")
                .contains("system_soft_image_full_name")
                .contains("system_soft_image_image_id");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains(4, 7, "%nginx%", SystemSoftImageStatus.PULLING);
    }
}
