package com.chua.starter.system.support;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.execute.AuthSessionUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.service.SimpleSysUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 账号状态守卫拦截器
 *
 * <p>登录入口已校验账号状态，但 token 签发后（默认 30 天有效），
 * 若账号在此期间被禁用，旧会话仍可访问接口。本拦截器在请求进入 Controller 前
 * 按账号当前状态实时拦截，使禁用立即生效，前端收到 401 后跳回登录页。</p>
 *
 * <p>语义：禁用（sysUserStatus=0）为长期禁用，立即拒绝所有接口，需管理员重新启用。
 * 未登录（会话中无用户信息）的请求直接放行，由其余鉴权机制处理。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Component
public class UserStateGuardInterceptor implements HandlerInterceptor {

    /**
     * 用户服务
     */
    @Resource
    private SimpleSysUserService simpleSysUserService;

    /**
     * 账号状态缓存
     */
    @Resource
    private UserStateCache userStateCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 认证 / 公开端点直接放行：登录请求可能携带旧 token（甚至是已被禁用账号的 token），
        // 不应被状态守卫拦截，否则用户将无法重新登录
        if (isPublicEndpoint(request.getRequestURI())) {
            return true;
        }
        UserResume resume = AuthSessionUtils.getUserInfo(UserResume.class);
        if (resume == null || StringUtils.isBlank(resume.getUserId())) {
            return true;
        }
        // 非系统用户（如学生）放行：学生令牌的 userId 是学号，与
        // edu_student.student_account 对齐，**不是** sys_user 主键（学生按设计不建系统用户）。
        // 若仍按 sys_user 状态校验，getById(学号) 必查不到，全体学生会被误判为
        // 「账号已被禁用」而 401。学生是否可登录由登录时的 student_status 决定。
        if (resume.getRoles() != null && resume.getRoles().contains(AuthConstant.STUDENT)) {
            return true;
        }
        final Integer userId = parseUserId(resume.getUserId());
        if (userId == null) {
            return true;
        }

        UserStateCache.Snapshot snapshot = userStateCache.resolve(userId, () -> {
            SysUser user = simpleSysUserService.getById(userId);
            if (user == null) {
                return new UserStateCache.Snapshot(null);
            }
            return new UserStateCache.Snapshot(user.getSysUserStatus());
        });

        // 禁用：立即拒绝
        if (snapshot.status() == null || snapshot.status() != 1) {
            return reject(response, "账号已被禁用，请重新登录");
        }
        return true;
    }

    /**
     * 判断是否为无需登录态校验的公开端点（登录、登出、验证码、注册、公共接口、错误页）。
     *
     * @param requestUri 请求完整 URI（已小写）
     * @return true 直接放行
     */
    private boolean isPublicEndpoint(String requestUri) {
        if (StringUtils.isBlank(requestUri)) {
            return false;
        }
        String uri = requestUri.toLowerCase();
        return uri.endsWith("/login") || uri.contains("/login/")
                || uri.endsWith("/logout") || uri.contains("/logout/")
                || uri.contains("/captcha")
                || uri.contains("/register")
                || uri.contains("/public/") || uri.endsWith("/public")
                || uri.contains("/open/") || uri.endsWith("/open")
                || uri.endsWith("/error");
    }

    /**
     * 解析用户主键。
     *
     * @param userId 会话中的用户标识
     * @return 主键，无法解析时返回 null
     */
    private Integer parseUserId(String userId) {
        try {
            return Integer.valueOf(userId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 以 401 拒绝请求并返回统一 JSON。
     *
     * @param response 响应
     * @param msg      提示信息
     * @return 固定 false
     */
    private boolean reject(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = "{\"code\":\"401\",\"msg\":\"" + msg + "\",\"ok\":false,\"success\":false}";
        response.getWriter().write(body);
        return false;
    }
}
