package com.chua.starter.sso.third;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.server.support.gitee.GiteeService;
import com.chua.starter.sso.impl.SysUserTripartiteBind;
import com.chua.starter.sso.mapper.SysUserTripartiteBindMapper;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * gitee服务impl
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/29
 */
@Service
public class GiteeServiceImpl implements GiteeService {

    @Resource
    private SysUserTripartiteBindMapper sysUserTripartiteBindMapper;
    @Override
    public void binder(AuthUser data, String token) {
        UserResult userResult = AuthClientExecute.getInstance().getUserResult();
        String id = userResult.getId();
        if(null != id) {
            Long count = sysUserTripartiteBindMapper.selectCount(Wrappers.<SysUserTripartiteBind>lambdaQuery().eq(SysUserTripartiteBind::getUserId, id));
            if(count == 0L) {
                SysUserTripartiteBind sysUserTripartiteBind = new SysUserTripartiteBind();
                sysUserTripartiteBind.setUserId(Integer.valueOf(id));
                sysUserTripartiteBind.setLoginCodeType("gitee");
                sysUserTripartiteBind.setLoginUsername(data.getUsername());
                sysUserTripartiteBind.setLoginUuid(data.getUuid());
                sysUserTripartiteBind.setLoginAvatar(data.getAvatar());
                sysUserTripartiteBind.setLoginName(data.getNickname());
                sysUserTripartiteBind.setLoginUuid(data.getUuid());
                sysUserTripartiteBindMapper.insert(sysUserTripartiteBind);
            }

            return;
        }
    }
}
