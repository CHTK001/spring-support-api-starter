package com.chua.starter.achievement.config;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.starter.achievement.mapper.AchievementTierMapper;
import com.chua.starter.achievement.mapper.UserAchievementMapper;
import com.chua.starter.achievement.service.AchievementPushService;
import com.chua.starter.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 成就模块自动配置
 *
 * <p>注册 {@link AchievementService} Bean，注入上游 {@link KvEngine}（由 spring-support-redis-starter 注册），
 * 以及 MyBatis 映射器 和推送服务。无需额外配置，依赖 spring-boot-启动-数据-Redis 即可。</p>
 *
 * @since 2026-08-07
 * @author CH
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AchievementAutoConfiguration {

    /**
     * 统一 KV 引擎，由 spring-支持-Redis-启动 提供
     */
    private final KvEngine kvEngine;

    /**
     * 成就段位定义 映射器
     */
    private final AchievementTierMapper tierMapper;

    /**
     * 用户成就记录 映射器
     */
    private final UserAchievementMapper userAchievementMapper;

    /**
     * 成就推送服务
     */
    private final AchievementPushService pushService;

    /**
     * 创建成就服务 Bean。
     *
     * @return AchievementService 实例
     */
    @Bean
    public AchievementService achievementService() {
        return new AchievementService(kvEngine, tierMapper, userAchievementMapper, pushService);
    }
}
