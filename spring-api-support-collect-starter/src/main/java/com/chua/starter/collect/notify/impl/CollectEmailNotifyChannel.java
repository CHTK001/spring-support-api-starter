package com.chua.starter.collect.notify.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.starter.collect.dto.CollectNotifyMessage;
import com.chua.starter.collect.notify.CollectNotifyChannel;
import com.chua.starter.common.support.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 邮件通知渠道。
 *
 * <p>复用平台的 {@link MailService}（{@code spring.mail.*} 自动配置出的 JavaMailSender）。
 * 没配邮件账号时该渠道仍然在列表里，但发送会直接回一句可读的失败原因，
 * 让配置缺失在"最近结果"里可见而不是静默跳过。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.collect.notify", name = "enable", havingValue = "true", matchIfMissing = true)
public class CollectEmailNotifyChannel implements CollectNotifyChannel {

    /**
     * 邮件服务，未配置 spring.mail.host 时容器内没有该 Bean。
     */
    private final ObjectProvider<MailService> mailServiceProvider;

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public String label() {
        return "邮件";
    }

    @Override
    public String targetHint() {
        return "收件邮箱，多个用逗号分隔";
    }

    @Override
    public MessageResponse send(CollectNotifyMessage message) {
        MailService mailService = mailServiceProvider.getIfAvailable();
        if (mailService == null) {
            return MessageResponse.failure("未配置邮件账号（spring.mail.host），邮件渠道不可用");
        }
        long start = System.currentTimeMillis();
        ReturnResult<Void> result = mailService.sendNoticeMail(message.getTarget(),
                message.getSubject(), message.getContent());
        long cost = System.currentTimeMillis() - start;
        if (result != null && result.isSuccess()) {
            return MessageResponse.builder()
                    .success(true)
                    .messageId(message.getTarget())
                    .durationMillis(cost)
                    .build();
        }
        return MessageResponse.builder()
                .success(false)
                .errorMessage("邮件发送失败: " + (result == null ? "无响应" : result.getMsg()))
                .durationMillis(cost)
                .build();
    }
}
