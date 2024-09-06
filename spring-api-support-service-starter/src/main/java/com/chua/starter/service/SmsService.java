package com.chua.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.starter.system.pojo.sms.SmsRequest;

import java.util.List;

/**
 * 短信服务
 *
 * @author CH
 * @since 2024/9/6
 */
public interface SmsService {

    /**
     * 发送短信
     *
     * @param sysTemplateCode 短信模板
     * @param request         请求
     * @return 发送结果
     */
    ReturnResult<List<PushResult>> send(String sysTemplateCode, SmsRequest request);

}
