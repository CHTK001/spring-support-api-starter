package com.chua.starter.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统安全配置属性
 * <p>
 * 用于配置登录安全相关参数，包括账户锁定策略、密码策略、导入导出限制等
 * </p>
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
@ConfigurationProperties(prefix = SystemSecurityProperties.PRE)
public class SystemSecurityProperties {

    public static final String PRE = "plugin.system.security";

    /**
     * 登录失败最大尝试次数
     * <p>超过此次数后账户将被锁定</p>
     */
    private int maxLoginAttempts = 6;

    /**
     * 账户锁定时间（分钟）
     * <p>账户被锁定后，需等待此时间后才能再次尝试登录</p>
     */
    private int lockoutDurationMinutes = 5;

    /**
     * 登录错误计数过期时间（小时）
     * <p>在此时间内未达到锁定阈值，错误计数会自动清除</p>
     */
    private int loginErrorExpireHours = 24;

    /**
     * 密码盐值长度
     */
    private int saltLength = 14;

    /**
     * 用户导入时的默认密码
     */
    private String defaultImportPassword = "123456";

    /**
     * 用户导出最大数量限制
     */
    private int maxExportCount = 10000;

    /**
     * 第三方登录回调加密密钥
     * <p>用于加密第三方登录回调时传递的登录信息，密钥长度必须为16位</p>
     */
    private String thirdPartyCallbackKey = "1234567890Oil#@1";
}
