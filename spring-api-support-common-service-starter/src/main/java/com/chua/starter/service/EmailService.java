package com.chua.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.mail.Email;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.starter.system.entity.SysEmailTemplate;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.email.EmailRequest;

import java.util.List;

/**
 * 邮件服务
 * @author CH
 * @since 2024/9/4
 */
public interface EmailService {


    /**
     * 发送邮件
     * @param email      邮件
     * @return 是否成功
     */
    ReturnResult<Boolean> send(Email email);

    /**
     * 发送邮件
     *
     * @param sysTemplateCode 模板编码
     * @param sysProject      项目
     * @param emailTemplate  配置
     * @param request         请求
     * @param traceId 链路ID
     * @return 是否成功
     */
    ReturnResult<List<PushResult>> send(String sysTemplateCode, SysProject sysProject, SysEmailTemplate emailTemplate, EmailRequest request, Integer traceId);
}
