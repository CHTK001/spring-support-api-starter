package com.chua.starter.face.reid.config;

import com.chua.starter.face.reid.controller.FaceReidController;
import com.chua.starter.face.reid.reid.FaceReidService;
import com.chua.starter.face.reid.setting.FaceReidSettingService;
import com.chua.starter.face.reid.spi.FaceDetector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 自动装配测试：验证模块在宿主应用中能被正确装配、且不依赖数据层。
 *
 * <p>用 {@link WebApplicationContextRunner} 只加载本模块的自动配置，
 * 证明「只引入本 starter 就能用」——无需 MyBatis-Plus / Redis / 数据库。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
class FaceReidAutoConfigurationTest {

    /**
     * 上下文运行器。
     */
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FaceReidAutoConfiguration.class));

    /**
     * 默认配置下各 Bean 齐备，本地引擎就绪。
     */
    @Test
    @DisplayName("默认装配可用")
    void shouldRegisterBeansByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(FaceReidService.class);
            assertThat(context).hasSingleBean(FaceReidController.class);
            assertThat(context).hasSingleBean(FaceReidSettingService.class);
            assertThat(context).hasSingleBean(FaceDetector.class);

            FaceDetector detector = context.getBean(FaceDetector.class);
            assertThat(detector.name()).isEqualTo("local");
            assertThat(detector.ready()).isTrue();
            assertThat(detector.dimension()).isPositive();

            FaceReidService service = context.getBean(FaceReidService.class);
            assertThat(service.available()).isTrue();
            assertThat(service.engineReady()).isTrue();
        });
    }

    /**
     * enabled=false 时不装配，避免无谓占用。
     */
    @Test
    @DisplayName("关闭开关后不装配")
    void shouldBackOffWhenDisabled() {
        contextRunner
                .withPropertyValues("spring.face.reid.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FaceReidService.class));
    }

    /**
     * 未知引擎名降级为不可用，而不是启动失败。
     */
    @Test
    @DisplayName("未知引擎降级不崩溃")
    void shouldDegradeForUnknownEngine() {
        contextRunner
                .withPropertyValues("spring.face.reid.engine=arcface-not-on-classpath")
                .run(context -> {
                    FaceDetector detector = context.getBean(FaceDetector.class);
                    assertThat(detector.ready()).isFalse();

                    FaceReidService service = context.getBean(FaceReidService.class);
                    assertThat(service.available()).isFalse();
                    assertThat(service.unavailableReason()).isNotBlank();
                });
    }

    /**
     * 自定义配置项被正确绑定。
     */
    @Test
    @DisplayName("配置项正确绑定")
    void shouldBindProperties() {
        contextRunner
                .withPropertyValues(
                        "spring.face.reid.threshold=0.93",
                        "spring.face.reid.topK=25",
                        "spring.face.reid.maxFaces=500")
                .run(context -> {
                    FaceReidService service = context.getBean(FaceReidService.class);
                    assertThat(service.library().config().threshold()).isEqualTo(0.93);
                    assertThat(service.library().config().topK()).isEqualTo(25);
                    assertThat(service.library().config().maxFaces()).isEqualTo(500);
                });
    }
}
