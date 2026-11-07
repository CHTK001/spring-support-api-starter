package com.chua.starter.system.configuration;

import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.system.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 缓存管理器兜底配置。
 * <p>
 * redis-starter 注册了 7 个 CacheManager 且均无 @Primary，未限定 cacheManager 的
 * {@code @Cacheable} 在解析缓存管理器时会抛 NoUniqueBeanDefinitionException。凡带
 * {@code @EnableCaching} 且扫描 {@code com.chua.starter.system} 的应用都需要这份兜底，
 * 因此放在 common-system-starter 而非某个具体服务端模块。
 * </p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Configuration
public class DefaultCacheManagerConfiguration {

    /**
     * {@code aiCacheManager} 兜底 Bean。
     *
     * <p>ai-starter 的 {@code AiCacheAutoConfiguration} 受 {@code chua.ai.cache.enabled=true}
     * 控制：开关关闭时该类不注册，但 {@code WorldSimServiceImpl} 的构造器用
     * {@code @Qualifier("aiCacheManager")} 强依赖它（== {@code NoSuchBeanDefinitionException}，
     * 整个应用起不来），所以由本模块按平台注销条件（{@code @ConditionalOnMissingBean}）
     * 补上一个与主管理器同等的 {@code aiCacheManager} Bean：不创建 Caffeine，直接用框架
     * 给的主缓存管理器，系统平台的描述则是「.ai 缓存不进 Caffeine，走 primary 统一管理」。</p>
     *
     * <p>必须按名字取 {@code multiLevelCacheManager}，不能按类型取 {@code CacheManager}：
     * 类型注入会走 {@code determinePrimaryCandidate}，而 Redis 缓存配置的执行顺序在部分实例里
     * 晚于本 Bean（2026-11-07 租户实例 8083：本 Bean 在构造 {@code feedbackMenuInitializer} 时
     * 被急切创建，此时 {@code multiLevelCacheManager} 尚未注册，候选里有
     * {@code defaultCacheManager}/{@code cacheWithRedis}/{@code redis-cache-*} 而没有一个
     * {@code @Primary}，直接抛 more than one 'primary'；同一套 jar 起平台实例却正常，
     * 说明这是时序问题而不是装配冲突）。主管理器存在时就是它，不存在时才退回空实现。</p>
     *
     * @param primary 框架级 {@code @Primary} 缓存管理器（按名称 {@code multiLevelCacheManager} 解析）
     * @return aiCacheManager
     */
    @Bean("aiCacheManager")
    @ConditionalOnMissingBean(name = {"aiCacheManager"})
    public CacheManager aiCacheManager(
            @Qualifier("multiLevelCacheManager") ObjectProvider<CacheManager> primary) {
        CacheManager delegate = primary.getIfAvailable();
        return delegate != null ? delegate : new NoOpCacheManager();
    }

    /**
     * h2 档位：内存库通常不伴生 Redis，用空实现避免缓存注解触发真实连接。
     *
     * <p>必须带 {@code @Primary}：redis-starter 无条件注册 7 个 CacheManager 且都不带
     * {@code @Primary}，本档不给主的话，按类型注入 {@code CacheManager} 的地方
     * （如 {@code sysAnnouncementScheduleTask}）会因"找不到唯一候选"而启动失败
     * （实测 2026-10-14：去掉 {@code @Primary} 后报 expected single matching bean
     * but found 8）。</p>
     *
     * <p>代价是本档要求容器里**没有第二个** {@code @Primary} 的 CacheManager：
     * {@code AiCacheAutoConfiguration.aiCacheManager}（本地 Caffeine）也带
     * {@code @Primary}，两者同时在场就变成 more than one 'primary'（同一位置实测）。
     * 采集平台本地跑 h2 时不引 AI 模块（见该模块 pom 中的本地说明）正是为了避开它。</p>
     */
    @Configuration
    @Profile("h2")
    public static class EmbeddedCacheManagerConfiguration {

        /**
         * primary缓存Manager。
         *
         * @return 空实现的缓存管理器
         */
        @Bean
        @Primary
        public CacheManager primaryCacheManager() {
            return new NoOpCacheManager();
        }
    }

    /**
     * 非 h2 档位：复用一小时过期的 Redis 缓存管理器作为主管理器。
     *
     * <p>注意：此处不再标注 {@code @Primary}。框架级缓存模块（cache-starter）已经注册了
     * {@code multiLevelCacheManager} 并标记为 {@code @Primary}（按配置的层级顺序组合
     * Caffeine、Redis 等），框架级是唯一主管理器。两者同时标记 {@code @Primary} 时，Spring 启动
     * 解析引用 {@code CacheManager} 的注入点（如 {@code sysAnnouncementScheduleTask}）会报
     * {@code NoUniqueBeanDefinitionException（more than one 'primary' bean found）}，
     * 导致应用起不来。</p>
     *
     * <p>本 Bean 仍然是 {@code redis-cache-3600} 的一个别称；按 {@code @Qualifier("redis-cache-3600")}
     * 或 {@code CACHE_WITH_REDIS} 显式调用的场景不受影响。</p>
     */
    @Configuration
    @Profile("!h2")
    public static class RedisCacheManagerConfiguration {

        /**
         * redis-cache-3600 缓存Manager 别称，复用一小时过期的 redis 缓存管理器。
         *
         * @param cacheManager redis 缓存管理器
         * @return 缓存Manager 对象
         */
        @Bean
        public CacheManager defaultCacheManager(@Qualifier(CacheConstant.REDIS_CACHE_HOUR) CacheManager cacheManager) {
            return cacheManager;
        }

        /**
         * cacheWithRedis缓存Manager，供指名该缓存管理器的注解解析，复用一小时过期的 redis 缓存管理器。
         *
         * @param cacheManager redis 缓存管理器
         * @return 缓存Manager 对象
         */
        @Bean(SystemConstant.CACHE_WITH_REDIS)
        public CacheManager cacheWithRedisCacheManager(@Qualifier(CacheConstant.REDIS_CACHE_HOUR) CacheManager cacheManager) {
            return cacheManager;
        }
    }
}