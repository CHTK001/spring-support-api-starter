package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;

/**
 * 认证处理器接口
 * <p>
 * 定义不同认证协议的处理逻辑，通过SPI机制支持扩展
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
public interface AuthorizationHandler {

    /**
     * 执行认证
     *
     * @param authInformation OAuth认证信息
     * @param token           Token（可选）
     * @param cookie          Cookie（可选）
     * @param userCode        用户代码/AppKey（可选）
     * @return 认证结果
     */
    ReturnResult<String> authenticate(OauthAuthInformation authInformation, 
                                     String token, 
                                     String cookie, 
                                     String userCode);

    /**
     * 是否支持当前认证方式
     *
     * @param subProtocol 子协议类型
     * @param token       Token
     * @param userCode    用户代码
     * @return 是否支持
     */
    default boolean supports(String subProtocol, String token, String userCode) {
        return false;
    }
}
