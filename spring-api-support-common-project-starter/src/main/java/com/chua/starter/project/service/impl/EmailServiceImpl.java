package com.chua.starter.project.service.impl;

import com.chua.advanced.support.lang.mail.Email;
import com.chua.advanced.support.lang.mail.MailConfiguration;
import com.chua.advanced.support.lang.mail.MailSender;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessageCode;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.project.service.EmailService;
import com.chua.starter.service.setting.SmtpSetting;
import com.chua.starter.service.utils.SettingGroupUtils;
import com.chua.starter.system.entity.SysEmailTemplate;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.email.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * email接口实现
 * @author CH
 * @since 2024/9/4
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SettingGroupUtils settingGroupUtils;


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

                rs.add(PushResult.builder().code(
                        MessageCode.SUCCESS
                ).build());
            } catch (Exception e) {
                rs.add(PushResult.builder().code(
                        MessageCode.FAILURE
                ).message(e.getMessage()).build());
            }
        }
        return ReturnResult.of(rs);
    }
}
