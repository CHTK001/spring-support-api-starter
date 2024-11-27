package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.config.PushSetting;
import com.chua.common.support.lang.message.entity.TemplateInfo;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.lang.message.result.TemplateResult;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.SmsService;
import com.chua.starter.service.setting.SmsSetting;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysSecret;
import com.chua.starter.system.entity.SysSmsLog;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.mapper.SysSmsLogMapper;
import com.chua.starter.system.pojo.sms.SmsRequest;
import com.chua.starter.system.service.SysDictItemService;
import com.chua.starter.system.service.SysSmsLogService;
import com.chua.starter.system.service.SysSmsService;
import com.chua.starter.system.service.SysTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    final SysDictItemService sysDictItemService;
    final SysSmsLogService sysSmsLogService;
    @Override
    public ReturnResult<List<PushResult>> send(SmsRequest request) {
        SysTemplate sysTemplateServiceById = sysTemplateService.getById(request.getSysTemplateId());
        if(null == sysTemplateServiceById) {
            return ReturnResult.error("模板不存在");
        }
        SmsSetting smsSetting = GlobalSettingFactory.getInstance().get("sms", SmsSetting.class);
        String sysTemplateCode = sysTemplateServiceById.getSysTemplateCode();
        ReturnResult<List<PushResult>> send = smsService.send(sysTemplateCode, request);
        List<PushResult> data = send.getData();
        List<SysSmsLog> sysSmsLogs = new ArrayList<>(data.size());
        for (PushResult item : data) {
            SysSmsLog sysSmsLog = new SysSmsLog();
            sysSmsLog.setSysSmsAppSign(smsSetting.getAppSign());
            sysSmsLog.setSysSmsAppType(smsSetting.getAppType());
            sysSmsLog.setSysSmsAppId(smsSetting.getAppId());

            sysSmsLog.setSysSmsCode(item.getCode().name());
            sysSmsLog.setSysSmsMessage(item.getMessage());
            sysSmsLog.setSysSmsLogParam(Json.toPrettyFormat(request.getParams()));
            sysSmsLog.setSysSmsLogTarget(item.getTarget());
            sysSmsLog.setSysSmsCost(BigDecimal.valueOf(item.getCost()));
            sysSmsLog.setSysSmsLogTemplateId(request.getSysTemplateId());

            sysSmsLogs.add(sysSmsLog);
        }

        try {
            sysSmsLogService.saveBatch(sysSmsLogs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return send;
    }

    @Override
    public ReturnResult<Boolean> syncTemplate(SysSecret sysSecret) {
        SysDictItem sysDictItem = sysDictItemService.getById(sysSecret.getSysSecretDictItemId());
        if(null == sysDictItem) {
            return ReturnResult.error("字典不存在");
        }
        SysDictItem templateCategory = sysDictItemService.getOne(Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getSysDictItemCode, "SMS")
        );
        PushSetting pushSetting = PushSetting.builder()
                .accessKeyId(sysSecret.getSysSecretAppId())
                .accessKeySecret(sysSecret.getSysSecretAppSecret())
                .build();
        String sysDictItemCode = sysDictItem.getSysDictItemCode();
        ReturnResult<TemplateResult> templateResultReturnResult = smsService.syncTemplate(sysDictItemCode, pushSetting);
        if(templateResultReturnResult.isOk()) {
            TemplateResult templateResult = templateResultReturnResult.getData();
            for (TemplateInfo datum : templateResult.getData()) {
                SysTemplate sysTemplate = new SysTemplate();
                sysTemplate.setSysDictItemId1(sysDictItem.getSysDictItemId());
                sysTemplate.setSysDictItemId2(templateCategory.getSysDictItemId());
                sysTemplate.setSysTemplateStatus(1);
                sysTemplate.setSysTemplateApprove(2);
                sysTemplate.setSysTemplateContent(datum.getTemplateContent());
                sysTemplate.setSysTemplateCode(datum.getTemplateCode());
                sysTemplate.setSysTemplateName(datum.getTemplateName());
                sysTemplate.setSysTemplateSort(2);
                sysTemplate.setSysTemplateDefault(0);
                sysTemplate.setSysTemplateDelete(0);
                sysTemplate.setSysTemplateType(0);
                sysTemplate.setSysTemplateDisabled(1);
                sysTemplate.setSysTemplateOrigin("sms同步");
                sysTemplate.setSysSecretId(sysSecret.getSysSecretId());
                sysTemplate.setSysTemplateOriginUuid(String.valueOf(sysSecret.getSysSecretId()));
                sysTemplate.setSysDictItemId2(null != templateCategory ? templateCategory.getSysDictItemId() : null);

                LambdaQueryWrapper<SysTemplate> wrapper = Wrappers.<SysTemplate>lambdaQuery()
                        .eq(SysTemplate::getSysDictItemId2, sysTemplate.getSysDictItemId2())
                        .eq(SysTemplate::getSysTemplateCode, sysTemplate.getSysTemplateCode())
                        .eq(SysTemplate::getSysDictItemId1, sysTemplate.getSysDictItemId1())
                        .eq(null != sysTemplate.getSysDictItemId3() , SysTemplate::getSysDictItemId3, sysTemplate.getSysDictItemId3());

                if(sysTemplateService.count(wrapper) > 0) {
                    try {
                        sysTemplateService.update(sysTemplate, wrapper);
                    } catch (Exception e) {
                        continue;
                    }
                    continue;
                }

                try {
                    sysTemplateService.save(sysTemplate);
                } catch (Exception e) {
                    continue;
                }
            }

        }
        return ReturnResult.of(templateResultReturnResult);
    }
}
