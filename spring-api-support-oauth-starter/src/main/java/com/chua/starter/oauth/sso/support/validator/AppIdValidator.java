package com.chua.starter.oauth.sso.support.validator;

import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;

/**
 * appid 验证器接口
 * <p>
 * 用于验证客户端提供的 appid 是否合法，支持算法验证
 * </p>
 *
 * @版本 1.0.0
 * @since 2025-01-05
 * @author CH
 */
public interface AppIdValidator {

    /**
     * 验证 appid
     * <p>
     * 客户端会使用算法处理 appid 后发送给服务端，服务端需要验证处理后的值是否正确
     * </p>
     *
     * @param processedAppId 客户端算法处理后的 appid
     * @param appKey         数据库中的 app键 信息
     * @return 验证结果
     */
    default ValidationResult validate(String processedAppId, SysAppKey appKey) {
        return ValidationResult.failure("请实现 validate(OauthAuthInformation, SysAppKey) 方法");
    }

    /**
     * 验证 appid（从认证信息中获取签名参数）
     * <p>
     * 支持从 头部 中获取签名参数进行验证
     * </p>
     *
     * @param authInformation 认证信息
     * @param appKey         数据库中的 app键 信息
     * @return 验证结果
     */
    default ValidationResult validate(OauthAuthInformation authInformation, SysAppKey appKey) {
        // 默认实现：调用旧方法（向后兼容）
        String processedAppId = authInformation.getRequestHeader(authInformation.getRequest(), "appKey");
        return validate(processedAppId, appKey);
    }

    /**
     * 是否支持当前验证方式
     *
     * @param appId 原始 appid
     * @return 是否支持
     */
    default boolean supports(String appId) {
        return true;
    }

/**
 * 验证结果
 * @author CH
 * @since 4.0.0
 */
class ValidationResult {

    /**
     * 是否校验通过
     */
    private final boolean valid;

    /**
     * 失败原因消息
     */
    private final String message;

    /**
     * 私有构造器, 通过静态工厂方法创建实例。
     *
     * @param valid   是否校验通过
     * @param message 失败原因消息
     */
    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * 创建成功结果。
     *
     * @return 成功结果
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    /**
     * 创建失败结果。
     *
     * @param message 失败原因消息
     * @return 失败结果
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    /**
     * 是否校验通过。
     *
     * @return 校验通过返回 true
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 获取失败原因消息。
     *
     * @return 失败原因消息
     */
    public String getMessage() {
        return message;
    }
}
}

