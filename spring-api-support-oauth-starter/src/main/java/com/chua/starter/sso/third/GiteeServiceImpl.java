package com.chua.starter.sso.third;

import com.chua.starter.oauth.server.support.gitee.GiteeService;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;


/**
 * gitee服务impl
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/29
 */
@Service
public class GiteeServiceImpl implements GiteeService {
    @Override
    public void binder(AuthUser data) {
        System.out.println();
    }
}
