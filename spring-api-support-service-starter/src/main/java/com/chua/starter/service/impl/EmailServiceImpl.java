package com.chua.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.mail.Mail;
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
    public ReturnResult<Boolean> send(Mail mail) {
        SmtpSetting smtpSetting = GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
        MailSender mailSender = smtpSetting.getMailSender();
        if(StringUtils.isEmpty(smtpSetting.getFrom())) {
            return ReturnResult.fail("邮件设置 - 发送账号未设置");
        }

        if(null == mailSender) {
            return ReturnResult.fail("暂不支持邮件发送/邮件设置未设置");
        }
        try {
            mailSender.send(smtpSetting.getFrom(), mail);
            return ReturnResult.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResult.fail("发送失败!");
        }
    }
}
