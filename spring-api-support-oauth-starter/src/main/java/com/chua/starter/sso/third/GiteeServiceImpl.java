package com.chua.starter.sso.third;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.server.support.gitee.GiteeService;
import com.chua.starter.sso.impl.SysUserTripartiteBind;
import com.chua.starter.sso.mapper.SysUserMapper;
import com.chua.starter.sso.mapper.SysUserTripartiteBindMapper;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;


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

    @Resource
    private SysUserMapper sysUserMapper;
    @Override
    public void binder(AuthUser data, UserResult userResult) {
        String id = userResult.getId();
        if(null != id) {
            SysUserTripartiteBind selectOne = sysUserTripartiteBindMapper.selectOne(Wrappers.<SysUserTripartiteBind>lambdaQuery()
                    .eq(SysUserTripartiteBind::getUserId, id).eq(SysUserTripartiteBind::getLoginCodeType, "gitee"));
            if(selectOne == null) {
                SysUserTripartiteBind sysUserTripartiteBind = new SysUserTripartiteBind();
                sysUserTripartiteBind.setUserId(Integer.valueOf(id));
                sysUserTripartiteBind.setLoginCodeType("gitee");
                sysUserTripartiteBind.setLoginUsername(data.getUsername());
                sysUserTripartiteBind.setLoginUuid(data.getUuid());
                sysUserTripartiteBind.setLoginAvatar(data.getAvatar());
                sysUserTripartiteBind.setLoginName(data.getNickname());
                sysUserTripartiteBind.setCreateTime(LocalDateTime.now());
                sysUserTripartiteBind.setCreateBy(Integer.valueOf(id));
                sysUserTripartiteBind.setCreateName(userResult.getUsername());
                sysUserTripartiteBind.setLoginUuid(data.getUuid());
                sysUserTripartiteBindMapper.insert(sysUserTripartiteBind);
                return;
            }
            selectOne.setLoginUsername(data.getUsername());
            selectOne.setLoginAvatar(data.getAvatar());
            selectOne.setUpdateTime(LocalDateTime.now());
            selectOne.setUpdateBy(Integer.valueOf(id));
            selectOne.setUpdateName(userResult.getUsername());
            sysUserTripartiteBindMapper.updateById(selectOne);
            return;
        }
    }

    @Override
    public boolean doLogin(AuthUser data) {
        return sysUserTripartiteBindMapper.count(data.getUuid());
    }
}
