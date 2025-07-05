package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;

/**
 * 鉴权信息
 *
 * @author CH
 */
public interface Authorization {
    /**
     * 认证
     *
     * @return 结果
     */
    ReturnResult<String> authentication();

}
