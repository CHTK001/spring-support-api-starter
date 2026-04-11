package com.chua.starter.system.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.chua.common.support.core.utils.ClassUtils;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.annotations.PrivilegedVisibility;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 高权账号可见性处理器
 *
 * @author CH
 */
@Slf4j
public class PrivilegedVisibilityDataPermissionHandler implements MultiDataPermissionHandler {
    private static final String SUPER_ADMIN = AuthConstant.SUPER_ADMIN;
    private static final String ADMIN = AuthConstant.ADMIN;
    private static final String SA_USERNAME = "sa";
    private static final Map<String, PrivilegedVisibilityInfo> CACHE = new ConcurrentReferenceHashMap<>(256);
    private final Supplier<AuthService> authServiceSupplier;

    public PrivilegedVisibilityDataPermissionHandler() {
        this(() -> null);
    }

    public PrivilegedVisibilityDataPermissionHandler(Supplier<AuthService> authServiceSupplier) {
        this.authServiceSupplier = authServiceSupplier;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        try {
            PrivilegedVisibilityInfo info = getInfo(mappedStatementId);
            if (null == info || !info.enabled) {
                return null;
            }

            CurrentUser currentUser = getCurrentUser();
            if (null == currentUser || isSuperAdmin(currentUser)) {
                return null;
            }

            String sql = buildConditionSql(table, info, currentUser);
            if (sql == null || sql.isBlank()) {
                return null;
            }
            return CCJSqlParserUtil.parseCondExpression(sql);
        } catch (Exception e) {
            log.error("高权账号可见性处理异常, mappedStatementId: {}", mappedStatementId, e);
            throw new RuntimeException("高权账号可见性处理失败: " + mappedStatementId, e);
        }
    }

    private PrivilegedVisibilityInfo getInfo(String mappedStatementId) {
        return CACHE.computeIfAbsent(mappedStatementId, this::parseInfo);
    }

    private PrivilegedVisibilityInfo parseInfo(String mappedStatementId) {
        try {
            int lastDot = mappedStatementId.lastIndexOf('.');
            if (lastDot <= 0) {
                return null;
            }
            String className = mappedStatementId.substring(0, lastDot);
            String methodName = normalizeMethodName(mappedStatementId.substring(lastDot + 1));
            Class<?> mapperClass = ClassUtils.forName(className);

            PrivilegedVisibility methodAnnotation = findMethodAnnotation(mapperClass, methodName);
            if (methodAnnotation != null) {
                return new PrivilegedVisibilityInfo(methodAnnotation);
            }

            PrivilegedVisibility classAnnotation = mapperClass.getAnnotation(PrivilegedVisibility.class);
            if (classAnnotation != null) {
                return new PrivilegedVisibilityInfo(classAnnotation);
            }
        } catch (Exception e) {
            log.debug("无法加载 Mapper 类或解析高权账号注解: {}", mappedStatementId, e);
        }
        return null;
    }

    private String normalizeMethodName(String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return methodName;
        }
        if (methodName.endsWith("_mpCount")) {
            return methodName.substring(0, methodName.length() - 8);
        }
        if (methodName.endsWith("_COUNT")) {
            return methodName.substring(0, methodName.length() - 6);
        }
        if (methodName.endsWith("_count")) {
            return methodName.substring(0, methodName.length() - 6);
        }
        return methodName;
    }

    private PrivilegedVisibility findMethodAnnotation(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            PrivilegedVisibility annotation = method.getAnnotation(PrivilegedVisibility.class);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private String buildConditionSql(Table table, PrivilegedVisibilityInfo info, CurrentUser currentUser) throws JSQLParserException {
        String currentAlias = currentTableAlias(table);
        if (info.userAlias != null && !info.userAlias.isBlank()) {
            if (!matchesAlias(currentAlias, info.userAlias)) {
                return null;
            }
            return buildForUserAlias(info.userAlias, currentUser);
        }

        if (info.usernameColumn == null || info.usernameColumn.isBlank()) {
            return null;
        }

        String usernameAlias = columnAlias(info.usernameColumn);
        if (usernameAlias != null && !matchesAlias(currentAlias, usernameAlias)) {
            return null;
        }
        return buildForUsernameColumn(info.usernameColumn, currentUser);
    }

    private String buildForUserAlias(String userAlias, CurrentUser currentUser) {
        String usernameColumn = userAlias + ".sys_user_username";
        if (isAdmin(currentUser)) {
            return usernameColumn + " <> '" + SA_USERNAME + "'";
        }
        return usernameColumn + " <> '" + SA_USERNAME + "'"
                + " AND NOT EXISTS ("
                + "SELECT 1 FROM sys_user_role privileged_sur "
                + "LEFT JOIN sys_role privileged_sr ON privileged_sr.sys_role_id = privileged_sur.sys_role_id "
                + "WHERE privileged_sur.sys_user_id = " + userAlias + ".sys_user_id "
                + "AND privileged_sr.sys_role_code IN ('" + ADMIN + "', '" + SUPER_ADMIN + "')"
                + ")";
    }

    private String buildForUsernameColumn(String usernameColumn, CurrentUser currentUser) {
        if (isAdmin(currentUser)) {
            return usernameColumn + " <> '" + SA_USERNAME + "'";
        }
        return usernameColumn + " <> '" + SA_USERNAME + "'"
                + " AND NOT EXISTS ("
                + "SELECT 1 FROM sys_user privileged_su "
                + "LEFT JOIN sys_user_role privileged_sur ON privileged_sur.sys_user_id = privileged_su.sys_user_id "
                + "LEFT JOIN sys_role privileged_sr ON privileged_sr.sys_role_id = privileged_sur.sys_role_id "
                + "WHERE privileged_su.sys_user_username = " + usernameColumn + " "
                + "AND privileged_sr.sys_role_code IN ('" + ADMIN + "', '" + SUPER_ADMIN + "')"
                + ")";
    }

    private boolean isSuperAdmin(CurrentUser currentUser) {
        return currentUser.hasRole(SUPER_ADMIN) || SA_USERNAME.equalsIgnoreCase(currentUser.getUsername());
    }

    private boolean isAdmin(CurrentUser currentUser) {
        return currentUser.hasRole(ADMIN) && !isSuperAdmin(currentUser);
    }

    private String currentTableAlias(Table table) {
        if (table.getAlias() != null && table.getAlias().getName() != null) {
            return table.getAlias().getName();
        }
        return table.getName();
    }

    private boolean matchesAlias(String currentAlias, String expectedAlias) {
        return currentAlias != null && expectedAlias != null && currentAlias.equalsIgnoreCase(expectedAlias);
    }

    private String columnAlias(String columnExpression) {
        int dotIndex = columnExpression.indexOf('.');
        return dotIndex > 0 ? columnExpression.substring(0, dotIndex) : null;
    }

    private CurrentUser getCurrentUser() {
        if (authServiceSupplier == null) {
            return null;
        }
        AuthService authService = authServiceSupplier.get();
        return authService == null ? null : authService.getCurrentUser();
    }

    private static class PrivilegedVisibilityInfo {
        final boolean enabled;
        final String userAlias;
        final String usernameColumn;

        private PrivilegedVisibilityInfo(PrivilegedVisibility annotation) {
            this.enabled = annotation.enabled();
            this.userAlias = annotation.userAlias();
            this.usernameColumn = annotation.usernameColumn();
        }
    }
}
