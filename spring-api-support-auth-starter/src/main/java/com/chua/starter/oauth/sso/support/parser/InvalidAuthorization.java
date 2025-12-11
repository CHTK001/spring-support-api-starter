package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;

/**
 * 无效鉴权信息
 *
 * @author CH
 */
public final class InvalidAuthorization implements Authorization {

    public static final InvalidAuthorization INSTANCE = new InvalidAuthorization();

    @Override
    public ReturnResult<String> authentication() {
        return ReturnResult.noAuth();
    }
}
