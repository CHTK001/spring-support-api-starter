package com.chua.starter.video.config;

import com.chua.starter.video.controller.VideoConfigController;
import com.chua.starter.video.controller.VideoSearchController;
import com.chua.starter.video.job.VideoCatalogSyncJob;
import com.chua.starter.video.job.VideoConfigSyncJob;
import com.chua.starter.video.live.controller.LiveChannelController;
import com.chua.starter.video.live.service.HlsProxyService;
import com.chua.starter.video.live.service.LiveCatalogLoader;
import com.chua.starter.video.live.service.LiveChannelService;
import com.chua.starter.video.properties.VideoProperties;
import com.chua.starter.video.repository.mapper.VideoCatalogMapper;
import com.chua.starter.video.repository.VideoCatalogRepository;
import com.chua.starter.video.repository.VideoIndex;
import com.chua.starter.video.service.VideoCatalogSyncService;
import com.chua.starter.video.service.VideoPlatformService;
import com.chua.starter.video.setting.SysSettingStore;
import com.chua.starter.video.setting.VideoConfigSyncService;
import com.chua.starter.video.setting.VideoSettingService;
import com.chua.starter.video.setting.VideoSettingStore;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 视频模块自动配置。
 *
 * <p>统一装配视频检索、直播目录与 HLS 代理，并把配置落到「配置数据库」：
 * 宿主引入通用配置服务（{@code sys_setting}）时，视频/直播配置存入其中，
 * 复用 {@code /v2/setting} 管理端；未引入时回退 {@link VideoProperties} 与内置 JSON。</p>
 *
 * <p>开关：{@code spring.video.enabled=false} 关闭整体；
 * {@code spring.video.live-enabled=false} 单独关闭直播。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(VideoProperties.class)
@ConditionalOnProperty(prefix = VideoProperties.PRE, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class VideoAutoConfiguration {

    /**
     * 本地索引 Mapper 包（宿主自行扫描时使用）。
     */
    public static final String INDEX_MAPPER_PACKAGE = "com.chua.starter.video.repository.mapper";

    /**
     * 视频模块自动配置构造方法。
     */
    public VideoAutoConfiguration() {
        log.info("视频服务模块已启用");
    }

    /**
     * 创建视频配置数据库服务（无存储时降级为本地配置）。
     *
     * @param storeProvider 配置存储提供者
     * @return VideoSettingService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public VideoSettingService videoSettingService(ObjectProvider<VideoSettingStore> storeProvider) {
        VideoSettingStore store = storeProvider.getIfAvailable();
        return new VideoSettingService(store == null ? new VideoSettingStore.Noop() : store);
    }

    /**
     * 创建视频平台业务服务。
     *
     * @param properties         视频服务配置
     * @param videoSettingService 视频配置数据库服务
     * @param repositoryProvider 本地索引仓储（未引入数据库时为空）
     * @return VideoPlatformService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public VideoPlatformService videoPlatformService(VideoProperties properties,
                                                     VideoSettingService videoSettingService,
                                                     ObjectProvider<VideoIndex> repositoryProvider) {
        return new VideoPlatformService(properties, videoSettingService, repositoryProvider.getIfAvailable());
    }

    /**
     * 创建视频/直播配置同步服务。
     *
     * @param videoSettingService 视频配置数据库服务
     * @param properties          视频服务配置
     * @param liveChannelProvider 直播目录服务（可选）
     * @return VideoConfigSyncService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public VideoConfigSyncService videoConfigSyncService(VideoSettingService videoSettingService,
                                                         VideoProperties properties,
                                                         ObjectProvider<LiveChannelService> liveChannelProvider) {
        return new VideoConfigSyncService(videoSettingService, properties, liveChannelProvider.getIfAvailable());
    }

    /**
     * 创建视频检索控制器。
     *
     * @param videoPlatformService 视频平台业务服务
     * @return 控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public VideoSearchController videoSearchController(VideoPlatformService videoPlatformService) {
        return new VideoSearchController(videoPlatformService);
    }

    /**
     * 创建直播目录加载器。
     *
     * @return LiveCatalogLoader 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public LiveCatalogLoader liveCatalogLoader() {
        return new LiveCatalogLoader();
    }

    /**
     * 创建直播频道目录服务。
     *
     * @param properties          视频服务配置
     * @param liveCatalogLoader   目录加载器
     * @param videoSettingService 视频配置数据库服务
     * @return LiveChannelService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = VideoProperties.PRE, name = "live-enabled", havingValue = "true",
            matchIfMissing = true)
    public LiveChannelService liveChannelService(VideoProperties properties,
                                                 LiveCatalogLoader liveCatalogLoader,
                                                 VideoSettingService videoSettingService) {
        return new LiveChannelService(properties, liveCatalogLoader, videoSettingService);
    }

    /**
     * 创建 HLS 反向代理服务。
     *
     * @param properties         视频服务配置
     * @param liveChannelService 直播频道目录
     * @return HlsProxyService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = VideoProperties.PRE, name = "live-enabled", havingValue = "true",
            matchIfMissing = true)
    public HlsProxyService hlsProxyService(VideoProperties properties, LiveChannelService liveChannelService) {
        return new HlsProxyService(properties, liveChannelService);
    }

    /**
     * 创建直播频道控制器。
     *
     * @param liveChannelService 直播频道目录
     * @param hlsProxyService    HLS 代理服务
     * @return 控制器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = VideoProperties.PRE, name = "live-enabled", havingValue = "true",
            matchIfMissing = true)
    public LiveChannelController liveChannelController(LiveChannelService liveChannelService,
                                                       HlsProxyService hlsProxyService) {
        return new LiveChannelController(liveChannelService, hlsProxyService);
    }

    /**
     * 创建视频配置热加载控制器。
     *
     * @param videoSettingService 视频配置数据库服务
     * @param liveChannelService  直播频道目录
     * @param videoConfigSyncService 配置同步服务
     * @return 控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public VideoConfigController videoConfigController(VideoSettingService videoSettingService,
                                                       ObjectProvider<LiveChannelService> liveChannelService,
                                                       VideoConfigSyncService videoConfigSyncService,
                                                       ObjectProvider<VideoCatalogSyncService> catalogSyncService) {
        return new VideoConfigController(videoSettingService,
                liveChannelService.getIfAvailable(), videoConfigSyncService,
                catalogSyncService.getIfAvailable());
    }

    /**
     * 配置数据库存储装配：仅当宿主引入通用配置服务时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.chua.starter.service.SysSettingService")
    static class SettingStoreConfiguration {

        /**
         * 创建基于 {@code sys_setting} 的视频配置存储。
         *
         * @param provider 通用配置服务
         * @return VideoSettingStore 实例
         */
        @Bean
        @ConditionalOnMissingBean
        public VideoSettingStore sysSettingStore(
                ObjectProvider<com.chua.starter.service.SysSettingService> provider) {
            return new SysSettingStore(provider);
        }
    }

    /**
     * 任务注册装配：仅当宿主引入 job-starter 时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.chua.starter.job.support.annotation.Job")
    static class JobConfiguration {

        /**
         * 注册视频/直播配置同步任务（job-starter 扫描 @Job 并调度）。
         *
         * @param syncService 配置同步服务
         * @return VideoConfigSyncJob 实例
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = VideoProperties.PRE, name = "config-sync-enabled",
                havingValue = "true", matchIfMissing = true)
        public VideoConfigSyncJob videoConfigSyncJob(VideoConfigSyncService syncService) {
            return new VideoConfigSyncJob(syncService);
        }
    }

    /**
     * 本地索引装配：仅当宿主引入 MyBatis-Plus 时生效。
     *
     * <p>装配检索仓储、目录同步服务与同步任务；未引入数据库的宿主不受影响，
     * 列表与详情继续直连资源站。</p>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
    @ConditionalOnProperty(prefix = VideoProperties.PRE, name = "catalog-store-enabled",
            havingValue = "true", matchIfMissing = true)
    @MapperScan(VideoAutoConfiguration.INDEX_MAPPER_PACKAGE)
    static class CatalogStoreConfiguration {

        /**
         * 创建本地索引仓储。
         *
         * @param mapper 索引 Mapper
         * @return VideoCatalogRepository 实例
         */
        @Bean
        @ConditionalOnMissingBean
        public VideoCatalogRepository videoCatalogRepository(VideoCatalogMapper mapper) {
            return new VideoCatalogRepository(mapper);
        }

        /**
         * 创建目录同步服务。
         *
         * @param platform   视频平台服务
         * @param repository 本地索引仓储
         * @param properties 视频配置
         * @return VideoCatalogSyncService 实例
         */
        @Bean
        @ConditionalOnMissingBean
        public VideoCatalogSyncService videoCatalogSyncService(VideoPlatformService platform,
                                                              VideoIndex repository,
                                                              VideoProperties properties) {
            return new VideoCatalogSyncService(platform, repository, properties);
        }

        /**
         * 注册目录同步任务（job-starter 扫描 @Job 并调度）。
         *
         * @param syncService 目录同步服务
         * @return VideoCatalogSyncJob 实例
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "com.chua.starter.job.support.annotation.Job")
        @ConditionalOnProperty(prefix = VideoProperties.PRE, name = "catalog-sync-enabled",
                havingValue = "true", matchIfMissing = true)
        public VideoCatalogSyncJob videoCatalogSyncJob(VideoCatalogSyncService syncService) {
            return new VideoCatalogSyncJob(syncService);
        }
    }
}
