package com.chua.starter.admin.service;

import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ErrorResult;

/**
 * sys消息推送服务
 *
 * @author CH
 */
public interface SysMessagePushService {


    /**
     * 推送
     *
     * @param templateId 模板id
     * @param userId     用户id
     * @param param      参数
     * @return {@link ErrorResult}
     */
    ErrorResult publish(String templateId, String[] userId, JsonObject param);
}
