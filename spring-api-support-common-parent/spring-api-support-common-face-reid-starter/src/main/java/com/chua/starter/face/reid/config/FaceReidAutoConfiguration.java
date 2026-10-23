package com.chua.starter.face.reid.config;

import com.chua.starter.face.reid.controller.FaceReidController;
import com.chua.starter.face.reid.engine.LocalFaceDetector;
import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.properties.FaceReidProperties;
import com.chua.starter.face.reid.reid.FaceFeatureLibrary;
import com.chua.starter.face.reid.reid.FaceReidService;
import com.chua.starter.face.reid.setting.FaceReidSettingService;
import com.chua.starter.face.reid.setting.FaceReidSettingStore;
import com.chua.starter.face.reid.spi.FaceDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 人脸重识别自动配置。
 *
 * <p>装配四件事：</p>
 * <ol>
 *   <li>{@link FaceReidSettingService}：配置（出厂值 + 配置数据库覆盖）；</li>
 *   <li>{@link FaceDetector}：检测引擎，按配置的 {@code engine} 从 SPI 选择；</li>
 *   <li>{@link FaceFeatureLibrary}：特征库与跨帧轨迹；</li>
 *   <li>{@link FaceReidService} 与 {@link FaceReidController}：业务与接口。</li>
 * </ol>
 *
 * <p>开关：{@code spring.face.reid.enabled=false} 关闭整体。引擎不可用或名称不匹配时
 * 降级为 {@link FaceDetector.Unavailable}，模块照常启动，识别接口返回明确原因，
 * 已入库的特征仍可做离线检索。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FaceReidProperties.class)
@ConditionalOnProperty(prefix = FaceReidProperties.PRE, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class FaceReidAutoConfiguration {

    /**
     * 自动配置构造：启动日志。
     */
    public FaceReidAutoConfiguration() {
        log.info("人脸重识别(Re-ID)模块已启用");
    }

    /**
     * 配置服务。
     *
     * @param storeProvider 配置存储（可能不存在）
     * @param properties    出厂配置
     * @return 配置服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FaceReidSettingService faceReidSettingService(
            ObjectProvider<FaceReidSettingStore> storeProvider, FaceReidProperties properties) {
        FaceReidSettingStore store = storeProvider.getIfAvailable();
        return new FaceReidSettingService(store == null ? new FaceReidSettingStore.Noop() : store, properties);
    }

    /**
     * 检测引擎：优先使用宿主注册的 Bean，其次按名字从 SPI 列表挑选，
     * 都不匹配时降级为不可用实现。
     *
     * @param properties   出厂配置
     * @param configProvider 配置服务（用于读取运行期 engine 名称）
     * @return 引擎
     */
    @Bean
    @ConditionalOnMissingBean(FaceDetector.class)
    public FaceDetector faceDetector(FaceReidProperties properties,
                                     ObjectProvider<FaceReidSettingService> configProvider) {
        String engine = properties.getEngine();
        FaceReidSettingService settings = configProvider.getIfAvailable();
        if (settings != null) {
            engine = settings.config().engine();
        }
        FaceDetector selected = FaceReidEngineLoader.select(engine, properties);
        if (!selected.ready()) {
            log.warn("[FaceReid] 检测引擎 '{}' 不可用，重识别将降级（仅保留已入库特征的检索能力）", engine);
        } else {
            log.info("[FaceReid] 检测引擎: {} (dimension={})", selected.name(), selected.dimension());
        }
        return selected;
    }

    /**
     * 特征库：以出厂配置初始化，随后由 {@link FaceReidService} 订阅的配置变更热更新。
     *
     * @param properties 出厂配置
     * @return 特征库
     */
    @Bean
    @ConditionalOnMissingBean
    public FaceFeatureLibrary faceFeatureLibrary(FaceReidProperties properties) {
        return new FaceFeatureLibrary(FaceReidConfig.of(properties));
    }

    /**
     * 重识别服务：把引擎与特征库连起来，并订阅配置变更做热更新。
     *
     * @param detector         检测引擎
     * @param library          特征库
     * @param settingService   配置服务
     * @param properties       出厂配置
     * @return 服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FaceReidService faceReidService(FaceDetector detector,
                                           FaceFeatureLibrary library,
                                           FaceReidSettingService settingService,
                                           FaceReidProperties properties) {
        settingService.initialize();
        FaceReidService service = new FaceReidService(detector, library,
                properties.getDefaultCameraId(), settingService.config().maxFacesPerImage());
        settingService.addListener(service::apply);
        service.apply(settingService.config());
        return service;
    }

    /**
     * 接口层：仅 Web 环境装配。
     *
     * @param faceReidService        重识别服务
     * @param faceReidSettingService 配置服务
     * @return 控制器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FaceReidController faceReidController(FaceReidService faceReidService,
                                                 FaceReidSettingService faceReidSettingService) {
        return new FaceReidController(faceReidService, faceReidSettingService);
    }

    /**
     * 配置数据库存储装配：宿主类路径上具备 {@code SysSettingService} 时生效。
     *
     * <p>{@link com.chua.starter.face.reid.setting.SysSettingStore} 内部以弱耦合方式
     * 访问该服务，因此本模块无需引入整套数据层依赖。</p>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.chua.starter.service.SysSettingService")
    static class SettingStoreConfiguration {

        /**
         * 基于 {@code sys_setting} 的配置存储。
         *
         * @param provider 宿主容器（用于按类型取配置服务）
         * @return 存储
         */
        @Bean
        @ConditionalOnMissingBean
        public FaceReidSettingStore faceReidSettingStore(ObjectProvider<Object> provider) {
            return new com.chua.starter.face.reid.setting.SysSettingStore(provider);
        }
    }

    /**
     * 本地引擎引擎信息工具。
     *
     * @author CH
     * @since 4.0.0.43
     */
    static final class FaceReidEngineLoader {

        /**
         * 工具类。
         */
        private FaceReidEngineLoader() {
        }

        /**
         * 按名称选择引擎。
         *
         * @param engine     引擎名
         * @param properties 出厂配置
         * @return 引擎（永不返回 null）
         */
        static FaceDetector select(String engine, FaceReidProperties properties) {
            FaceReidProperties.DetectorOptions options = FaceReidProperties.DetectorOptions.defaults();
            LocalFaceDetector local = new LocalFaceDetector(options,
                    properties.getMaxImagePixels(), properties.getMaxFacesPerImage());
            if (local.supports(engine)) {
                return local;
            }
            return new FaceDetector.Unavailable(
                    "未找到名称匹配的人脸检测引擎: " + engine + "（可用: " + LocalFaceDetector.ENGINE_NAME + "）");
        }
    }
}
