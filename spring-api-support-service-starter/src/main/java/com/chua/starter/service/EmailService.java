package com.chua.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.mail.Mail;

/**
 * 邮件服务
 * @author CH
 * @since 2024/9/4
 */
public interface EmailService {


    /**
     * 发送邮件
     * @param mail      邮件
     * @return 是否成功
     */
    ReturnResult<Boolean> send(Mail mail);
}
