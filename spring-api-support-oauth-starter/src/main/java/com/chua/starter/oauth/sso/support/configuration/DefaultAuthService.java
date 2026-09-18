package com.chua.starter.oauth.sso.support.configuration;

import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 认证服务 默认实现(返回空用户,0 值)
 * <p>当无其他模块提供 AuthService 实现时,以此兜底,避免依赖方 NPE。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Component
@ConditionalOnMissingBean(AuthService.class)
public class DefaultAuthService implements AuthService {

    @Override
    public CurrentUser getCurrentUser() {
        CurrentUser user = new CurrentUser();
        user.setUserId("0");
        user.setUsername("");
        user.setName("");
        return user;
    }
}