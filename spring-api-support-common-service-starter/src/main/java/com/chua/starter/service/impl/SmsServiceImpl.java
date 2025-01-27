package com.chua.starter.service.impl;

import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessagePush;
import com.chua.common.support.lang.message.config.PushSetting;
import com.chua.common.support.lang.message.entity.TemplateInfo;
import com.chua.common.support.lang.message.factory.TemplateFactory;
import com.chua.common.support.lang.message.option.TemplateSetting;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.lang.message.result.TemplateResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.SmsService;
import com.chua.starter.service.setting.SmsSetting;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.sms.SmsRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信服务实现
 * @author CH
 * @since 2024/9/6
 */
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public ReturnResult<List<PushResult>> send(String sysTemplateCode, SysProject sysProject, SmsRequest request) {
        String sysProjectDictItemCode = sysProject.getSysProjectDictItemCode();
        MessagePush messagePush = ServiceProvider.of(MessagePush.class).getNewExtension(sysProjectDictItemCode,
                PushSetting.builder()
                        .accessKeyId(sysProject.getSysProjectAppId())
                        .accessKeySecret(sysProject.getSysProjectAppSecret())
                        .endpoint(sysProject.getSysProjectEndpoint())
                        .build());
        TemplateSetting setting = TemplateSetting.builder()
                .templateId(sysTemplateCode)
                .templateParam(JsonObject.create(request.getParams()))
                .signCode(sysProject.getSysProjectSign())
                .build();
        List<PushResult> publish = messagePush.publish(request.getTarget(), setting);
        List<PushResult> list = publish.stream().filter(item -> !item.isSuccess()).toList();
        if(CollectionUtils.isNotEmpty(list)) {
            if(list.size() == publish.size()) {
                return ReturnResult.error("发送失败" + list.get(0).getMessage());
            }
            return ReturnResult.illegal("部分发送失败" + list.stream()
                    .map(PushResult::getTarget).collect(Collectors.joining(","))
            );
        }
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
        List<TemplateInfo> data = new LinkedList<>();
        TemplateResult template = templateFactory.getTemplate(1, 50);
        data.addAll(template.getData());
        if(template.getTotal() >= 50) {
            int page = 2;
            while (template.getTotal() >= 50) {
                template = templateFactory.getTemplate(page, 50);
                List<TemplateInfo> data1 = template.getData();
                if(CollectionUtils.isEmpty(data1)) {
                    break;
                }
                data.addAll(data1);
                if(page > 10) {
                    break;
                }
                page++;
            }
        }
        template.setData(data);
        return ReturnResult.ok(template);
    }
}
