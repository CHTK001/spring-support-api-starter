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
 * 为部门负责人补齐负责人范围缓存，避免 oauth-client 直接依赖业务模块。
 *
 * @author CH
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ManagedDeptAwareAuthServiceBeanPostProcessor implements BeanPostProcessor {
    private static final String AUTH_SERVICE_BEAN_NAME = "authService";
    private static final String DEPT_LEADER = "DEPT_LEADER";
    private static final String MANAGED_DEPT_IDS = "managedDeptIds";
    private static final String MANAGED_DEPT_TREE_IDS = "managedDeptTreeIds";

    private final ObjectProvider<SysDeptPrincipalRelService> sysDeptPrincipalRelServiceProvider;

    public ManagedDeptAwareAuthServiceBeanPostProcessor(
            ObjectProvider<SysDeptPrincipalRelService> sysDeptPrincipalRelServiceProvider) {
        this.sysDeptPrincipalRelServiceProvider = sysDeptPrincipalRelServiceProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof AuthService authService) || !AUTH_SERVICE_BEAN_NAME.equals(beanName)) {
            return bean;
        }
        return (AuthService) () -> enrichManagedDeptScope(authService.getCurrentUser());
    }

    private CurrentUser enrichManagedDeptScope(CurrentUser currentUser) {
        if (currentUser == null || !currentUser.hasRole(DEPT_LEADER)) {
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
