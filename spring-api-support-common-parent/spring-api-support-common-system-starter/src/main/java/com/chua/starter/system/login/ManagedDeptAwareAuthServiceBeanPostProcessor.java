package com.chua.starter.system.login;

import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.oauth.client.support.execute.AuthSessionUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.service.SysDeptPrincipalRelService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
   * 为部门负责人补齐负责人范围缓存，避免 OAuth-客户端 直接依赖业务模块。
 *
 * @since 2025/8/1
 * @author CH
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ManagedDeptAwareAuthServiceBeanPostProcessor implements BeanPostProcessor {
    /**
     * 鉴权服务 Bean 名称
     */
    private static final String AUTH_SERVICE_BEAN_NAME = "authService";

    /**
     * 部门负责人角色编码
     */
    private static final String DEPT_LEADER = "DEPT_LEADER";

    /**
      * 负责部门标识集合扩展键
     */
    private static final String MANAGED_DEPT_IDS = "managedDeptIds";

    /**
      * 负责部门树标识集合扩展键
     */
    private static final String MANAGED_DEPT_TREE_IDS = "managedDeptTreeIds";

    /**
     * 部门负责人关系服务提供者
     */
    private final ObjectProvider<SysDeptPrincipalRelService> sysDeptPrincipalRelServiceProvider;

    /**
     * 构造后置处理器。
     *
     * @param sysDeptPrincipalRelServiceProvider 部门负责人关系服务提供者
     */
    public ManagedDeptAwareAuthServiceBeanPostProcessor(
            ObjectProvider<SysDeptPrincipalRelService> sysDeptPrincipalRelServiceProvider) {
        this.sysDeptPrincipalRelServiceProvider = sysDeptPrincipalRelServiceProvider;
    }

    /**
     * Bean 初始化后将部门负责人范围补齐到当前用户。
     *
     * @param bean     Bean 实例
     * @param beanName Bean 名称
     * @return 可能被代理包装后的 Bean
     * @throws BeansException Bean 生命周期异常
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof AuthService authService) || !AUTH_SERVICE_BEAN_NAME.equals(beanName)) {
            return bean;
        }
        return (AuthService) () -> enrichManagedDeptScope(authService.getCurrentUser());
    }

    /**
     * 为部门负责人补齐负责人范围缓存。
     *
     * @param currentUser 当前用户
     * @return 补齐后的当前用户
     */
    private CurrentUser enrichManagedDeptScope(CurrentUser currentUser) {
        if (currentUser == null
                || currentUser.getRoles() == null
                || !currentUser.getRoles().contains(DEPT_LEADER)) {
            return currentUser;
        }

        Map<String, Object> ext = currentUser.getExt();
        if (hasManagedDeptScope(ext)) {
            return currentUser;
        }

        Integer userId = parseUserId(currentUser.getUserId());
        SysDeptPrincipalRelService sysDeptPrincipalRelService = sysDeptPrincipalRelServiceProvider.getIfAvailable();
        if (userId == null || sysDeptPrincipalRelService == null) {
            return currentUser;
        }

        List<Integer> managedDeptIds = sysDeptPrincipalRelService.listManagedDeptIds(userId);
        List<String> managedDeptTreeIds = sysDeptPrincipalRelService.listManagedDeptTreeIds(managedDeptIds);
        Map<String, Object> mergedExt = ext == null ? new LinkedHashMap<>() : new LinkedHashMap<>(ext);
        mergedExt.put(MANAGED_DEPT_IDS, managedDeptIds);
        mergedExt.put(MANAGED_DEPT_TREE_IDS, managedDeptTreeIds);
        currentUser.setExt(mergedExt);
        syncSessionUserExt(mergedExt);
        return currentUser;
    }

    /**
     * 判断用户扩展属性中是否已存在负责人范围。
     *
     * @param ext 用户扩展属性
     * @return true 表示已存在
     */
    private boolean hasManagedDeptScope(Map<String, Object> ext) {
        if (ext == null) {
            return false;
        }
        Object managedDeptTreeIds = ext.get(MANAGED_DEPT_TREE_IDS);
        if (managedDeptTreeIds instanceof List<?> list) {
            return !list.isEmpty();
        }
        if (managedDeptTreeIds instanceof String value) {
            return !value.isBlank();
        }
        return managedDeptTreeIds != null;
    }

    /**
      * 将用户 标识 字符串解析为整数。
     *
     * @param userId 用户 标识
     * @return 解析后的整数，解析失败时为 空
     */
    private Integer parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * 将补齐的负责人范围同步到会话中的用户信息。
     *
     * @param ext 用户扩展属性
     */
    private void syncSessionUserExt(Map<String, Object> ext) {
        UserResume userResume = AuthSessionUtils.getUserInfo(UserResume.class);
        if (userResume == null) {
            return;
        }
        Map<String, Object> sessionExt = userResume.getExt() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(userResume.getExt());
        sessionExt.putAll(ext);
        userResume.setExt(sessionExt);
        AuthSessionUtils.setUserInfo(userResume);
    }
}
