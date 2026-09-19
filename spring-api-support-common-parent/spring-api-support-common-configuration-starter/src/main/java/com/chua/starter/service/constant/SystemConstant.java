package com.chua.starter.service.constant;

/**
 * 系统常量
 * <p>常量集合类，禁止实例化与继承，使用处通过类名限定访问</p>
 * @since 2024/8/8
 * @author CH
 */
public final class SystemConstant {

    /** 创建 系统常量 实例 */
    private SystemConstant() {
    }

    /**
     * 默认缓存
     */
    public static final String CACHE_WITH_REDIS = "redis";

    /**
     * 日志名称
     */
    public static final String LOG_NAME = "log";

    /**
     * SQL日志名称
     */
    public static final String SQL_NAME = "sql";

    /**
     * 超级管理员事件
     */
    public static final String SUPER_ADMIN_EVENT_SQL_MESSAGE = "SUPER_ADMIN_EVENT_SQL_MESSAGE";
}
