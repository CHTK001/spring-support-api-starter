package com.chua.starter.system.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.starter.service.SmsService;
import com.chua.starter.system.entity.SysSecret;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.pojo.sms.SmsRequest;
import com.chua.starter.system.service.SysSmsService;
import com.chua.starter.system.service.SysTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短信服务
 * @author CH
 * @since 2024/9/6
 */
@Service
@RequiredArgsConstructor
public class SysSmsServiceImpl implements SysSmsService {

    final SmsService smsService;
    final SysTemplateService sysTemplateService;
    @Override
    public ReturnResult<List<PushResult>> send(SmsRequest request) {
        SysTemplate sysTemplateServiceById = sysTemplateService.getById(request.getSysTemplateId());
        if(null == sysTemplateServiceById) {
            return ReturnResult.error("模板不存在");
        }

        String sysTemplateCode = sysTemplateServiceById.getSysTemplateCode();
        return smsService.send(sysTemplateCode, request);
    }

    @Override
    public ReturnResult<Boolean> syncTemplate(SysSecret sysSecret) {
        smsService.syncTemplate(sysSecret.getSysSecretDictItemId())
        return null;
    }
}
