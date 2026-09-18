package com.chua.starter.system.login;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.oauth.client.support.user.UserRoleResolver;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统用户角色解析器
 * <p>
 * 为 OAuth 客户端（http / http-lite 协议）补充用户角色编码，
 * 解决 OAuth 服务端 /验证 返回 角色 为空导致的权限不足问题。
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Service
@Spi("default")
public class SystemUserRoleResolver implements UserRoleResolver {

    @Autowired
    /** SYS用户rolemapper */
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    /** 解析 */
    public Set<String> resolve(String userId, String username) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            Integer uid = Integer.valueOf(userId.trim());
            List<String> codes = sysUserRoleMapper.selectRoleCodesByUserId(uid);
            if (codes == null || codes.isEmpty()) {
                return null;
            }
            return new HashSet<>(codes);
        } catch (Exception e) {
            return null;
        }
    }
}
