package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;

/**
 * 无效鉴权信息
 *
 */
public final class InvalidAuthorization implements Authorization {

    /** 实例 */
    public static final InvalidAuthorization INSTANCE = new InvalidAuthorization();

    @Override
    /** Authentication */
    public ReturnResult<String> authentication() {
        return ReturnResult.error("未认证");
    }
}
