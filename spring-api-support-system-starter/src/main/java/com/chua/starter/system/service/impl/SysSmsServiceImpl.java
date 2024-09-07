package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.config.PushSetting;
import com.chua.common.support.lang.message.entity.TemplateInfo;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.lang.message.result.TemplateResult;
import com.chua.starter.service.SmsService;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysSecret;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.pojo.sms.SmsRequest;
import com.chua.starter.system.service.SysDictItemService;
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
    final SysTemplateCategoryService sysTemplateCategoryService;
    final SysDictItemService sysDictItemService;
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
        SysDictItem sysDictItem = sysDictItemService.getById(sysSecret.getSysSecretDictItemId());
        if(null == sysDictItem) {
            return ReturnResult.error("字典不存在");
        }
        SysTemplateCategory templateCategory = sysTemplateCategoryService.getOne(Wrappers.<SysTemplateCategory>lambdaQuery()
                .eq(SysTemplateCategory::getSysTemplateCategoryCode, "SMS")
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
                sysTemplate.setSysDictItem1(sysDictItem.getSysDictItemId());
                sysTemplate.setSysTemplateStatus(1);
                sysTemplate.setSysTemplateApprove(2);
                sysTemplate.setSysTemplateContent(datum.getTemplateContent());
                sysTemplate.setSysTemplateCode(datum.getTemplateCode());
                sysTemplate.setSysTemplateName(datum.getTemplateName());
                sysTemplate.setSysTemplateSort(2);
                sysTemplate.setSysTemplateDefault(0);
                sysTemplate.setSysTemplateType(0);
                sysTemplate.setSysTemplateDisabled(1);
                sysTemplate.setSysTemplateOrigin("sms同步");
                sysTemplate.setSysTemplateOriginUuid(String.valueOf(sysSecret.getSysSecretId()));
                sysTemplate.setSysDictItem2(null != templateCategory ? templateCategory.getSysTemplateCategoryId() : null);

                LambdaQueryWrapper<SysTemplate> wrapper = Wrappers.<SysTemplate>lambdaQuery()
                        .eq(SysTemplate::getSysDictItem2, sysTemplate.getSysDictItem2())
                        .eq(SysTemplate::getSysTemplateCode, sysTemplate.getSysTemplateCode())
                        .eq(SysTemplate::getSysDictItem1, sysTemplate.getSysDictItem1());

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
