package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.starter.system.entity.SysSecret;
import com.chua.starter.system.pojo.sms.SmsRequest;

import java.util.List;

/**
 * 短信服务
 * @author CH
 * @since 2024/9/6
 */
public interface SysSmsService {
    /**
     * 发送短信
     * @param request 请求
     * @return result
     */
    ReturnResult<List<PushResult>> send(SmsRequest request);

    /**
     * 同步短信模板
     * @param sysSecret 短信配置
     * @return result
     */
    ReturnResult<Boolean> syncTemplate(SysSecret sysSecret);
}
