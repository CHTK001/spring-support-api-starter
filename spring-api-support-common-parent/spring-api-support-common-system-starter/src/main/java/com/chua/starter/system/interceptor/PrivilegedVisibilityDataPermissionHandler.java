package com.chua.starter.system.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.chua.common.support.reflection.ReflectUtils;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.annotations.PrivilegedVisibility;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 高权账号可见性处理器
 *
 * @since 2025/5/1
 * @author CH
 */
@Slf4j
public class PrivilegedVisibilityDataPermissionHandler implements MultiDataPermissionHandler {
    /**
     * 超级管理员角色编码
     */
    private static final String SUPER_ADMIN = AuthConstant.SUPER_ADMIN;

    /**
     * 管理员角色编码
     */
    private static final String ADMIN = AuthConstant.ADMIN;

    /**
     * 超级管理员用户名
     */
    private static final String SA_USERNAME = "sa";

    /**
     * 数据权限注解允许的表别名与列标识符白名单。
     */
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");

    /**
     * 注解信息缓存
     */
    private static final Map<String, PrivilegedVisibilityInfo> CACHE = new ConcurrentReferenceHashMap<>(256);

    /**
     * 鉴权服务提供者
     */
    private final Supplier<AuthService> authServiceSupplier;

    /**
     * 用户 映射器（用于查询管理员角色信息）
     */
    @Autowired(required = false)
    private SysUserMapper sysUserMapper; // sys用户映射器

    /**
     * 用户角色 映射器（用于查询管理员角色标识列表）
     */
    @Autowired(required = false)
    private SysUserRoleMapper sysUserRoleMapper; // sys用户角色映射器

    /**
     * 空构造器，鉴权服务提供者为空。
     */
    public PrivilegedVisibilityDataPermissionHandler() {
        this(() -> null);
    }

    /**
     * 带鉴权服务提供者的构造器。
     *
     * @param authServiceSupplier 鉴权服务提供者
     */
    public PrivilegedVisibilityDataPermissionHandler(Supplier<AuthService> authServiceSupplier) {
        this.authServiceSupplier = authServiceSupplier;
    }

    /**
     * 根据当前用户与应用注解生成数据权限 SQL 片段。
     *
     * @param table            数据表
     * @param where            原有查询条件
     * @param mappedStatementId 映射器 方法标识
     * @return 数据权限 SQL 表达式，无需限制时为 空
     * @throws RuntimeException 解析 SQL 失败时抛出
     */
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

    /**
     * 从缓存获取或解析注解信息。
     *
     * @param mappedStatementId 映射器 方法标识
     * @return 注解信息，无注解时为 空
     */
    private PrivilegedVisibilityInfo getInfo(String mappedStatementId) {
        return CACHE.computeIfAbsent(mappedStatementId, this::parseInfo);
    }

    /**
     * 解析 映射器 方法与类上的注解信息。
     *
     * @param mappedStatementId 映射器 方法标识
     * @return 注解信息，无法解析时为 空
     */
    private PrivilegedVisibilityInfo parseInfo(String mappedStatementId) {
        try {
            int lastDot = mappedStatementId.lastIndexOf('.');
            if (lastDot <= 0) {
                return null;
            }
            String className = mappedStatementId.substring(0, lastDot);
            String methodName = normalizeMethodName(mappedStatementId.substring(lastDot + 1));
            Class<?> mapperClass = ReflectUtils.forName(className);

            PrivilegedVisibility methodAnnotation = ReflectUtils.findMethodAnnotation(
                    mapperClass, methodName, PrivilegedVisibility.class);
            if (methodAnnotation != null) {
                return new PrivilegedVisibilityInfo(methodAnnotation);
            }

            PrivilegedVisibility classAnnotation = ReflectUtils.findClassAnnotation(
                    mapperClass, PrivilegedVisibility.class);
            if (classAnnotation != null) {
                return new PrivilegedVisibilityInfo(classAnnotation);
            }
        } catch (Exception e) {
            log.debug("无法加载 Mapper 类或解析高权账号注解: {}", mappedStatementId, e);
        }
        return null;
    }

    /**
     * 规范化 映射器 方法名（去掉 MyBatis 自动生成的后缀）。
     *
     * @param methodName 原始方法名
     * @return 规范化后的方法名
     */
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

    /**
     * 根据注解配置与当前用户构建条件 SQL。
     *
     * <p>MyBatis-Plus 数据权限接口仅接收表达式 AST，没有 JDBC 参数绑定上下文，无法安全改写为
     * {@code PreparedStatement} 占位符。本方法因此采用中文豁免约束：注解中的表别名和列名必须通过
     * 标识符白名单，SQL 值仅允许固定常量 {@code sa} 与经正整数校验的角色主键。</p>
     *
     * @param table       数据表
     * @param info        注解信息
     * @param currentUser 当前用户
     * @return 条件 SQL 字符串，不适用时为 null
     * @throws JSQLParserException SQL 解析异常
     * @throws IllegalArgumentException 注解标识符不合法时抛出
     */
    private String buildConditionSql(Table table, PrivilegedVisibilityInfo info, CurrentUser currentUser) throws JSQLParserException {
        String currentAlias = currentTableAlias(table);
        if (info.userAlias != null && !info.userAlias.isBlank()) {
            String safeUserAlias = requireSafeIdentifier(info.userAlias, "用户表别名");
            if (!matchesAlias(currentAlias, safeUserAlias)) {
                return null;
            }
            return buildForUserAlias(safeUserAlias, currentUser);
        }

        if (info.usernameColumn == null || info.usernameColumn.isBlank()) {
            return null;
        }

        String safeUsernameColumn = requireSafeColumnExpression(info.usernameColumn);
        String usernameAlias = columnAlias(safeUsernameColumn);
        if (usernameAlias != null && !matchesAlias(currentAlias, usernameAlias)) {
            return null;
        }
        return buildForUsernameColumn(safeUsernameColumn, currentUser);
    }

    /**
     * 为用户表别名构建高权账号过滤 SQL。
     *
     * @param userAlias   用户表别名
     * @param currentUser 当前用户
     * @return 条件 SQL 字符串
     */
    private String buildForUserAlias(String userAlias, CurrentUser currentUser) {
        String usernameColumn = userAlias + ".sys_user_username";
        if (isAdmin(currentUser)) {
            return usernameColumn + " <> '" + SA_USERNAME + "'";
        }
        List<Integer> adminRoleIds = getAdminRoleIds();
        if (adminRoleIds.isEmpty()) {
            return usernameColumn + " <> '" + SA_USERNAME + "'";
        }
        return usernameColumn + " <> '" + SA_USERNAME + "'"
                + " AND NOT EXISTS ("
                + "SELECT 1 FROM sys_user_role privileged_sur "
                + "LEFT JOIN sys_role privileged_sr ON privileged_sr.sys_role_id = privileged_sur.sys_role_id "
                + "WHERE privileged_sur.sys_user_id = " + userAlias + ".sys_user_id "
                + "AND privileged_sr.sys_role_id IN (" + formatIdList(adminRoleIds) + ")"
                + ")";
    }

    /**
     * 为用户名列表达式构建高权账号过滤 SQL。
     *
     * @param usernameColumn 用户名列表达式
     * @param currentUser    当前用户
     * @return 条件 SQL 字符串
     */
    private String buildForUsernameColumn(String usernameColumn, CurrentUser currentUser) {
        if (isAdmin(currentUser)) {
            return usernameColumn + " <> '" + SA_USERNAME + "'";
        }
        List<Integer> adminRoleIds = getAdminRoleIds();
        if (adminRoleIds.isEmpty()) {
            return usernameColumn + " <> '" + SA_USERNAME + "'";
        }
        return usernameColumn + " <> '" + SA_USERNAME + "'"
                + " AND NOT EXISTS ("
                + "SELECT 1 FROM sys_user privileged_su "
                + "LEFT JOIN sys_user_role privileged_sur ON privileged_sur.sys_user_id = privileged_su.sys_user_id "
                + "LEFT JOIN sys_role privileged_sr ON privileged_sr.sys_role_id = privileged_sur.sys_role_id "
                + "WHERE privileged_su.sys_user_username = " + usernameColumn + " "
                + "AND privileged_sr.sys_role_id IN (" + formatIdList(adminRoleIds) + ")"
                + ")";
    }

    /**
     * 获取管理员角色标识列表。
     *
     * @return 管理员角色ID列表
     */
    private List<Integer> getAdminRoleIds() {
        if (sysUserRoleMapper == null) {
            return Collections.emptyList();
        }
        return sysUserRoleMapper.selectRoleIdsByCodes(List.of(ADMIN, SUPER_ADMIN));
    }

    /**
     * 将整数列表格式化为 SQL 子句值列表。
     *
     * <p>角色主键来自数据库而非请求，但仍逐项校验为正整数，确保表达式构造器只能接收获取的安全数值。</p>
     *
     * @param ids 整数标识列表
     * @return 格式化后的字符串，如 "1,2,3"
     * @throws IllegalArgumentException 列表包含空值或非正整数时抛出
     */
    private String formatIdList(List<Integer> ids) {
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("管理员角色标识必须为正整数");
        }
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 判断当前用户是否为超级管理员。
     *
     * @param currentUser 当前用户
     * @return true 表示为超级管理员
     */
    private boolean isSuperAdmin(CurrentUser currentUser) {
        return currentUser.hasRole(SUPER_ADMIN) || SA_USERNAME.equalsIgnoreCase(currentUser.getUsername());
    }

    /**
     * 判断当前用户是否为管理员（非超级管理员）。
     *
     * @param currentUser 当前用户
     * @return true 表示为管理员
     */
    private boolean isAdmin(CurrentUser currentUser) {
        return currentUser.hasRole(ADMIN) && !isSuperAdmin(currentUser);
    }

    /**
     * 获取表别名，无别名时返回表名。
     *
     * @param table 数据表
     * @return 表别名或表名
     */
    private String currentTableAlias(Table table) {
        if (table.getAlias() != null && table.getAlias().getName() != null) {
            return table.getAlias().getName();
        }
        return table.getName();
    }

    /**
     * 判断别名是否匹配（忽略大小写）。
     *
     * @param currentAlias 当前别名
     * @param expectedAlias 期望别名
     * @return true 表示匹配
     */
    private boolean matchesAlias(String currentAlias, String expectedAlias) {
        return currentAlias != null && expectedAlias != null && currentAlias.equalsIgnoreCase(expectedAlias);
    }

    /**
     * 校验单个 SQL 标识符。
     *
     * @param value 标识符
     * @param label 业务标签
     * @return 去除首尾空白后的标识符
     * @throws IllegalArgumentException 标识符不符合白名单时抛出
     */
    private String requireSafeIdentifier(String value, String label) {
        String identifier = value == null ? "" : value.trim();
        if (!SAFE_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(label + "不符合 SQL 标识符白名单: " + value);
        }
        return identifier;
    }

    /**
     * 校验形如 {@code table.column} 的列表达式。
     *
     * @param expression 原始列表达式
     * @return 去除首尾空白后的列表达式
     * @throws IllegalArgumentException 表达式格式不合法时抛出
     */
    private String requireSafeColumnExpression(String expression) {
        String value = expression == null ? "" : expression.trim();
        int separator = value.indexOf('.');
        if (separator <= 0 || separator != value.lastIndexOf('.') || separator == value.length() - 1) {
            throw new IllegalArgumentException("用户名列必须使用 table.column 格式: " + expression);
        }
        requireSafeIdentifier(value.substring(0, separator), "用户名列表别名");
        requireSafeIdentifier(value.substring(separator + 1), "用户名列");
        return value;
    }

    /**
     * 提取列表达式中的表别名。
     *
     * @param columnExpression 列表达式
     * @return 表别名，无别名时为 空
     */
    private String columnAlias(String columnExpression) {
        int dotIndex = columnExpression.indexOf('.');
        return dotIndex > 0 ? columnExpression.substring(0, dotIndex) : null;
    }

    /**
     * 获取当前登录用户。
     *
     * @return 当前用户，无鉴权服务时为 空
     */
    private CurrentUser getCurrentUser() {
        if (authServiceSupplier == null) {
            return null;
        }
        AuthService authService = authServiceSupplier.get();
        return authService == null ? null : authService.getCurrentUser();
    }

    /**
     * 高权账号可见性注解信息容器。
     * @author CH
     * @since 4.0.0
     */
    private static class PrivilegedVisibilityInfo {
        /**
         * 是否启用
         */
        final boolean enabled;

        /**
         * 目标用户表别名
         */
        final String userAlias;

        /**
         * 目标用户名列表达式
         */
        final String usernameColumn;

        /**
         * 由注解构建容器。
         *
         * @param annotation 高权账号可见性注解
         */
        private PrivilegedVisibilityInfo(PrivilegedVisibility annotation) {
            this.enabled = annotation.enabled();
            this.userAlias = annotation.userAlias();
            this.usernameColumn = annotation.usernameColumn();
        }
    }
}
