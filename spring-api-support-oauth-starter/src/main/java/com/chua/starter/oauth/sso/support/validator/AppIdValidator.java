package com.chua.starter.oauth.sso.support.validator;

import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;

/**
 * AppId 验证器接口
 * <p>
 * 用于验证客户端提供的 appId 是否合法，支持算法验证
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-01-05
 */
public interface AppIdValidator {

    /**
     * 验证 AppId
     * <p>
     * 客户端会使用算法处理 appId 后发送给服务端，服务端需要验证处理后的值是否正确
     * </p>
     *
     * @param processedAppId 客户端算法处理后的 appId
     * @param appKey         数据库中的 AppKey 信息
     * @return 验证结果
     */
    default ValidationResult validate(String processedAppId, SysAppKey appKey) {
        return ValidationResult.failure("请实现 validate(OauthAuthInformation, SysAppKey) 方法");
    }

    /**
     * 验证 AppId（从认证信息中获取签名参数）
     * <p>
     * 支持从 Header 中获取签名参数进行验证
     * </p>
     *
     * @param authInformation 认证信息
     * @param appKey         数据库中的 AppKey 信息
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
     * @param appId 原始 appId
     * @return 是否支持
     */
    default boolean supports(String appId) {
        return true;
    }

    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}

