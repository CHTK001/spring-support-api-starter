package com.chua.starter.project.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessageCode;
import com.chua.common.support.lang.message.MessagePush;
import com.chua.common.support.lang.message.TemplateMessagePush;
import com.chua.common.support.lang.message.config.PushSetting;
import com.chua.common.support.lang.message.entity.TemplateInfo;
import com.chua.common.support.lang.message.factory.TemplateFactory;
import com.chua.common.support.lang.message.option.TemplateSetting;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.lang.message.result.TemplateResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.project.service.SmsService;
import com.chua.starter.project.service.SysSmsLogService;
import com.chua.starter.service.utils.SettingGroupUtils;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.entity.SysSmsLog;
import com.chua.starter.system.pojo.sms.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信服务实现
 *
 * @author CH
 * @since 2024/9/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SettingGroupUtils settingGroupUtils;
    private final SysSmsLogService smsLogService;

    @Override
    public ReturnResult<List<PushResult>> send(String sysTemplateCode, SysProject sysProject, SmsRequest request) {
        // 检查是否使用项目接口
        if (!settingGroupUtils.isSmsUseProjectInterface()) {
            // 不使用项目接口，返回模拟成功数据
            String[] target = request.getTarget();
            List<PushResult> rs = new ArrayList<>(target.length);
            for (String s : target) {
                rs.add(PushResult.builder().code(MessageCode.SUCCESS).message("模拟发送成功").target(s).build());
            }
            return ReturnResult.of(rs);
        }
        
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
        long startTime = System.currentTimeMillis();
        List<PushResult> publish = messagePush.publish(request.getTarget(), setting);
        long cost = System.currentTimeMillis() - startTime;
        
        // 保存发送日志
        saveSmsLog(sysProject, sysTemplateCode, request, publish, cost);
        
        List<PushResult> list = publish.stream().filter(item -> !item.isSuccess()).toList();
        if(CollectionUtils.isNotEmpty(list)) {
            if(list.size() == publish.size()) {
                return ReturnResult.error("发送失败" + list.getFirst().getMessage());
            }
            return ReturnResult.illegal("部分发送失败" + list.stream()
                    .map(PushResult::getTarget).collect(Collectors.joining(","))
            );
        }
        return ReturnResult.of(publish);
    }

    /**
     * 保存短信发送日志
     *
     * @param sysProject      项目配置
     * @param sysTemplateCode 模板代码
     * @param request         请求参数
     * @param results         发送结果
     * @param cost            耗时
     */
    private void saveSmsLog(SysProject sysProject, String sysTemplateCode, 
                            SmsRequest request, List<PushResult> results, long cost) {
        try {
            for (PushResult result : results) {
                SysSmsLog smsLog = new SysSmsLog();
                smsLog.setSysProjectId(sysProject.getSysProjectId());
                smsLog.setSysSmsLogTarget(result.getTarget());
                smsLog.setSysSmsLogTemplateId(sysTemplateCode);
                smsLog.setSysSmsLogParam(Json.toJson(request.getParams()));
                smsLog.setSysSmsCost(BigDecimal.valueOf(cost));
                smsLog.setSysSmsCode(result.getCode() != null ? result.getCode().name() : null);
                smsLog.setSysSmsMessage(result.getMessage());
                smsLog.setSysSmsAppType(sysProject.getSysProjectDictItemCode());
                smsLog.setSysSmsAppId(sysProject.getSysProjectAppId());
                smsLog.setSysSmsAppSign(sysProject.getSysProjectSign());
                smsLogService.save(smsLog);
            }
        } catch (Exception e) {
            log.error("保存短信发送日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<TemplateResult> syncTemplate(String sysDictItemCode, PushSetting pushSetting) {
        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        MessagePush messagePush = serviceProvider.getNewExtension(sysDictItemCode, pushSetting);
        if(null == messagePush) {
            return ReturnResult.error("短信设置 - 未设置");
        }
        TemplateMessagePush templateMessagePush = (TemplateMessagePush) messagePush;
        TemplateFactory templateFactory = templateMessagePush.getTemplateFactory();
        TemplateResult template = templateFactory.getTemplate(1, 50);
        List<TemplateInfo> data = new LinkedList<>(template.getData());
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
