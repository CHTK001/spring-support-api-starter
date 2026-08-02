package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.entity.SysUserTripartiteBind;
import com.chua.starter.system.mapper.SysUserTripartiteBindMapper;
import com.chua.starter.system.service.GiteeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Gitee第三方登录服务实现
 * <p>处理Gitee账号与系统账号的绑定逻辑</p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GiteeServiceImpl implements GiteeService {

    private final SysUserTripartiteBindMapper sysUserTripartiteBindMapper;

    @Override
    public void binder(AuthUser data, UserResume userResume) {
        String userId = userResume.getUserId();
        if (userId == null) {
            log.warn("用户ID为空，无法绑定Gitee账号");
            return;
        }

        log.debug("开始绑定Gitee账号, 用户ID: {}, Gitee用户名: {}", userId, data.getUsername());

        // 查询是否已存在绑定记录
        SysUserTripartiteBind existingBind = sysUserTripartiteBindMapper.selectOne(
                Wrappers.<SysUserTripartiteBind>lambdaQuery()
                        .eq(SysUserTripartiteBind::getUserId, userId)
                        .eq(SysUserTripartiteBind::getLoginCodeType, "gitee"));

        if (existingBind == null) {
            // 创建新绑定记录
            createNewBind(data, userResume, userId);
        } else {
            // 更新已有绑定记录
            updateExistingBind(existingBind, data, userResume, userId);
        }
    }

    /**
     * 创建新的绑定记录
     *
     * @param data       Gitee认证用户信息
     * @param userResume 系统用户信息
     * @param userId     用户ID
     */
    private void createNewBind(AuthUser data, UserResume userResume, String userId) {
        SysUserTripartiteBind bind = new SysUserTripartiteBind();
        bind.setUserId(Integer.valueOf(userId));
        bind.setLoginCodeType("gitee");
        bind.setLoginUsername(data.getUsername());
        bind.setLoginUuid(data.getUuid());
        bind.setLoginAvatar(data.getAvatar());
        bind.setLoginName(data.getNickname());
        bind.setCreateTime(LocalDateTime.now());
        bind.setCreateBy(Integer.valueOf(userId));
        bind.setCreateName(userResume.getUsername());

        sysUserTripartiteBindMapper.insert(bind);
        log.info("Gitee账号绑定成功, 用户ID: {}, Gitee用户名: {}", userId, data.getUsername());
    }

    /**
     * 更新已有的绑定记录
     *
     * @param existingBind 已有绑定记录
     * @param data         Gitee认证用户信息
     * @param userResume   系统用户信息
     * @param userId       用户ID
     */
    private void updateExistingBind(SysUserTripartiteBind existingBind, AuthUser data,
                                    UserResume userResume, String userId) {
        existingBind.setLoginUsername(data.getUsername());
        existingBind.setLoginAvatar(data.getAvatar());
        existingBind.setUpdateTime(LocalDateTime.now());
        existingBind.setUpdateBy(Integer.valueOf(userId));
        existingBind.setUpdateName(userResume.getUsername());

        sysUserTripartiteBindMapper.updateById(existingBind);
        log.info("Gitee账号绑定更新成功, 用户ID: {}, Gitee用户名: {}", userId, data.getUsername());
    }
}
