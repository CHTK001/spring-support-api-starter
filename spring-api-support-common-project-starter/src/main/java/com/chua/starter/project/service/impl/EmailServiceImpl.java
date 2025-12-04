package com.chua.starter.project.service.impl;

import com.chua.advanced.support.lang.mail.Email;
import com.chua.advanced.support.lang.mail.MailConfiguration;
import com.chua.advanced.support.lang.mail.MailSender;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessageCode;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.project.service.EmailService;
import com.chua.starter.project.service.SysEmailLogService;
import com.chua.starter.service.setting.SmtpSetting;
import com.chua.starter.service.utils.SettingGroupUtils;
import com.chua.starter.system.entity.SysEmailLog;
import com.chua.starter.system.entity.SysEmailTemplate;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.email.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务实现
 *
 * @author CH
 * @since 2024/9/4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SettingGroupUtils settingGroupUtils;
    private final SysEmailLogService emailLogService;


    @Override
    public ReturnResult<Boolean> send(Email email) {
        // 检查是否使用项目接口
        if (!settingGroupUtils.isEmailUseProjectInterface()) {
            // 不使用项目接口，返回模拟成功
            return ReturnResult.success(true);
        }

        if(StringUtils.isBlank(email.getTo())) {
            return ReturnResult.fail("收件人不能为空");
        }
        SmtpSetting smtpSetting = GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
        MailSender mailSender = smtpSetting.getMailSender();
        if(StringUtils.isEmpty(smtpSetting.getFrom())) {
            return ReturnResult.fail("邮件设置 - 发送账号未设置");
        }

        if(null == mailSender) {
            return ReturnResult.fail("暂不支持邮件发送/邮件设置未设置");
        }
        try {
            mailSender.send(smtpSetting.getFrom(), email);
            return ReturnResult.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            if(message.contains("User not found")) {
                return ReturnResult.illegal("收件人邮箱不存在");
            }
            if(message.contains("553") || message.contains("Authentication failed")) {
                return ReturnResult.noAuth("邮箱验证失败，请检查邮箱设置");
            }
            return ReturnResult.fail("发送失败!");
        }
    }

    @Override
    public ReturnResult<List<PushResult>> send(String sysTemplateCode,
                                               SysProject sysProject,
                                               SysEmailTemplate emailTemplate,
                                               EmailRequest request,
                                               Integer traceId) {
        // 检查是否使用项目接口
        if (!settingGroupUtils.isEmailUseProjectInterface()) {
            // 不使用项目接口，返回模拟成功数据
            String[] target = request.getTarget();
            List<PushResult> rs = new ArrayList<>(target.length);
            for (String s : target) {
                rs.add(PushResult.builder().code(MessageCode.SUCCESS).message("模拟发送成功").target(s).build());
            }
            return ReturnResult.of(rs);
        }

        MailConfiguration mailConfiguration = new MailConfiguration();
        mailConfiguration.setSmtpHost(sysProject.getSysProjectSmtpHost());
        mailConfiguration.setPassword(sysProject.getSysProjectSmtpPassword());
        mailConfiguration.setSmtpPort(sysProject.getSysProjectSmtpPort());
        MailSender mailSender = ServiceProvider.of(MailSender.class).getNewExtension("html",mailConfiguration);
        String[] target = request.getTarget();
        List<PushResult> rs = new ArrayList<>(target.length);
        for (String s : target) {
            long startTime = System.currentTimeMillis();
            PushResult result;
            try {
                mailSender.send(sysProject.getSysProjectSmtpFrom(), Email.builder()
                        .contentType("text/html")
                        .htmlMailCallbackUrl(emailTemplate.getSysEmailTemplateCallbackUrl())
                        .content(emailTemplate.getSysEmailTemplateContent())
                        .templateId(emailTemplate.getSysEmailTemplateId() + "")
                        .traceId(traceId + "")
                        .title(emailTemplate.getSysEmailTemplateName())
                        .maps(request.getParams())
                        .to(s)
                        .build());

                result = PushResult.builder().code(MessageCode.SUCCESS).target(s).build();
            } catch (Exception e) {
                result = PushResult.builder().code(MessageCode.FAILURE).target(s).message(e.getMessage()).build();
            }
            long cost = System.currentTimeMillis() - startTime;
            
            // 保存发送日志
            saveEmailLog(sysProject, emailTemplate, request, s, result, cost);
            rs.add(result);
        }
        return ReturnResult.of(rs);
    }

    /**
     * 保存邮件发送日志
     *
     * @param sysProject    项目配置
     * @param emailTemplate 邮件模板
     * @param request       请求参数
     * @param target        接收人
     * @param result        发送结果
     * @param cost          耗时
     */
    private void saveEmailLog(SysProject sysProject, SysEmailTemplate emailTemplate,
                              EmailRequest request, String target, PushResult result, long cost) {
        try {
            SysEmailLog emailLog = new SysEmailLog();
            emailLog.setSysProjectId(sysProject.getSysProjectId());
            emailLog.setSysEmailLogTarget(target);
            emailLog.setSysEmailLogTemplateId(String.valueOf(emailTemplate.getSysEmailTemplateId()));
            emailLog.setSysEmailLogParam(Json.toJson(request.getParams()));
            emailLog.setSysEmailLogContent(emailTemplate.getSysEmailTemplateContent());
            emailLog.setSysEmailCost(BigDecimal.valueOf(cost));
            emailLog.setSysEmailCode(result.getCode() != null ? result.getCode().name() : null);
            emailLog.setSysEmailMessage(result.getMessage());
            emailLog.setSysEmailRead(0);
            emailLogService.save(emailLog);
        } catch (Exception e) {
            log.error("保存邮件发送日志失败: {}", e.getMessage(), e);
        }
    }
}
