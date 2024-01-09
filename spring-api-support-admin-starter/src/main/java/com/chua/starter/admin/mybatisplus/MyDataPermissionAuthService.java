package com.chua.starter.admin.mybatisplus;

import com.chua.common.support.bean.BeanUtils;
import com.chua.starter.mybatis.interceptor.MybatisPlusPermissionHandler;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * 数据权限
 * @author CH
 */
@Component
@Import(MybatisPlusPermissionHandler.class)
public class MyDataPermissionAuthService implements AuthService {
    @Override
    public CurrentUser getCurrentUser() {
        return BeanUtils.copyProperties(AuthClientExecute.getInstance().getUserResult(), CurrentUser.class);
    }
}
