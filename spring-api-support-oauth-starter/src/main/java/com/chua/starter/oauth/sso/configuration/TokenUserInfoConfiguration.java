package com.chua.starter.oauth.sso.configuration;

import com.chua.starter.oauth.sso.service.impl.TokenBasedUserInfoService;
import com.chua.starter.oauth.sso.support.user.UserInfoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 令牌用户信息服务配置类
 *
 * @author CH
 */
@Configuration
public class TokenUserInfoConfiguration {

    /**
     * 令牌用户信息服务实现
     *
     * @return 用户信息服务实例
     */
    @Bean
    @ConditionalOnMissingBean(UserInfoService.class)
    public UserInfoService tokenBasedUserInfoService() {
        return new TokenBasedUserInfoService();
    }
}