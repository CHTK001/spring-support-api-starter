package com.chua.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.mail.Email;
import com.chua.common.support.lang.mail.MailSender;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.EmailService;
import com.chua.starter.service.setting.SmtpSetting;
import org.springframework.stereotype.Service;

/**
 * email接口实现
 * @author CH
 * @since 2024/9/4
 */
@Service
public class EmailServiceImpl implements EmailService {


    @Override
    public ReturnResult<Boolean> send(Email email) {
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
}
