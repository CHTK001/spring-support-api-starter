package com.chua.starter.system.constant;

/**
 * 系统常量
 * <p>定义系统模块中使用的各类常量，避免魔法值</p>
 *
 * @author CH
 * @since 2024/8/7
 */
public final class SystemConstant {

    private SystemConstant() {
    }

    /**
     * Redis缓存管理器名称
     */
    public static final String CACHE_WITH_REDIS = "cacheWithRedis";

    // ==================== 用户状态 ====================
    
    /**
     * 用户状态：启用
     */
    public static final int USER_STATUS_ENABLED = 1;
    
    /**
     * 用户状态：禁用
     */
    public static final int USER_STATUS_DISABLED = 0;

    // ==================== 角色状态 ====================
    
    /**
     * 角色状态：启用
     */
    public static final int ROLE_STATUS_ENABLED = 1;
    
    /**
     * 角色状态：禁用
     */
    public static final int ROLE_STATUS_DISABLED = 0;

    // ==================== 系统内置标识 ====================
    
    /**
     * 系统内置：是
     */
    public static final int IN_SYSTEM_YES = 1;
    
    /**
     * 系统内置：否
     */
    public static final int IN_SYSTEM_NO = 0;

    // ==================== 删除标识 ====================
    
    /**
     * 未删除
     */
    public static final int NOT_DELETED = 0;
    
    /**
     * 已删除
     */
    public static final int DELETED = 1;

    // ==================== 权限标识 ====================
    
    /**
     * 权限启用
     */
    public static final int PERMISSION_ENABLED = 1;
    
    /**
     * 权限禁用
     */
    public static final int PERMISSION_DISABLED = 0;

    // ==================== 树形结构 ====================
    
    /**
     * 根节点ID
     */
    public static final int ROOT_NODE_ID = 0;

    // ==================== HTTP状态码 ====================
    
    /**
     * HTTP状态码：成功
     */
    public static final int HTTP_STATUS_OK = 200;
}
