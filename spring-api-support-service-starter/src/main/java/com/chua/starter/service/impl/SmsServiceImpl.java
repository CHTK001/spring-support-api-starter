package com.chua.starter.service.impl;

import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessagePush;
import com.chua.common.support.lang.message.config.PushSetting;
import com.chua.common.support.lang.message.factory.TemplateFactory;
import com.chua.common.support.lang.message.option.TemplateSetting;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.lang.message.result.TemplateResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.SmsService;
import com.chua.starter.service.setting.SmsSetting;
import com.chua.starter.system.pojo.sms.SmsRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短信服务实现
 * @author CH
 * @since 2024/9/6
 */
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public ReturnResult<List<PushResult>> send(String sysTemplateCode, SmsRequest request) {
        SmsSetting smsSetting = GlobalSettingFactory.getInstance().get("sms", SmsSetting.class);
        if(!smsSetting.isValid()) {
            return ReturnResult.fail("短信设置 - 未设置");
        }

        MessagePush messagePush = smsSetting.getMessagePush();
        TemplateSetting setting = TemplateSetting.builder()
                .templateId(sysTemplateCode)
                .templateParam(JsonObject.create(request.getParams()))
                .signCode(smsSetting.getAppSign())
                .build();
        List<PushResult> publish = messagePush.publish(request.getTarget(), setting);
        return ReturnResult.of(publish);
    }

    @Override
    public ReturnResult<TemplateResult> syncTemplate(String sysDictItemCode, PushSetting pushSetting) {
        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        MessagePush messagePush = serviceProvider.getNewExtension(sysDictItemCode, pushSetting);
        if(null == messagePush) {
            return ReturnResult.error("短信设置 - 未设置");
        }

        TemplateFactory templateFactory = messagePush.getTemplateFactory();
        TemplateResult template = templateFactory.getTemplate(1, 50);
        return ReturnResult.ok(template);
    }
}
