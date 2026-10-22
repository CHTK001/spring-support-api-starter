package com.chua.starter.system.job;

import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.task.message.MessageEnvironment;
import com.chua.common.support.task.message.MessagePush;
import com.chua.common.support.task.message.MessageRequest;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.starter.job.support.notify.JobAlarmMessage;
import com.chua.starter.job.support.notify.JobAlarmNotifier;
import com.chua.starter.service.setting.SmtpSetting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统管理应用的调度任务邮件告警通道（类型 EMAIL）。
 *
 * <p>凭据取自<b>系统配置</b>（{@code sys_setting} 的 {@code smtp} 分组），
 * 经 {@link SmtpSetting} 转成 {@link MessageEnvironment} 后取邮件 SPI
 * （{@code @Spi("email")} 的 {@code com.chua.email.support.EmailPush}）投递。</p>
 *
 * <p>为什么不直接用 {@code spring.mail.*} 的 {@code JavaMailSender}：
 * 本仓库的邮件账号是<b>运行时系统配置</b>，运维在「系统设置」里改分组即生效，
 * 不需要重启、也不需要往配置文件里塞明文密码。{@code JavaMailSender} 读的是
 * {@code spring.mail.*}，系统应用根本没配，结果会永远是「未配置邮件账号」——
 * 这正是采集侧那份实现被迫放弃 MailService 的原因（见 {@code CollectJobEmailAlarmNotifier}）。
 * 系统应用扫到了 {@code com.chua.starter.service}，能直接复用同一套
 * {@link GlobalSettingFactory} + SPI 装配（与 {@code SysMessagePushServiceImpl}
 * 的 EMAIL 分支保持同一份口径）。</p>
 *
 * <p>告警通道是 SPI：job-starter 自身不绑定通道，宿主注册 {@link JobAlarmNotifier}
 * Bean 即接通。系统应用不依赖采集模块，所以要在自己这里补上通道实现。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
@ConditionalOnClass(JobAlarmNotifier.class)
public class SystemJobEmailAlarmNotifier implements JobAlarmNotifier {

    /**
     * 系统配置里的邮件分组名（{@code sys_setting} 的 group）。
     */
    private static final String SETTING_GROUP = "smtp";

    /**
     * 邮件 SPI 渠道名，与 {@code @Spi("email")} 一致。
     */
    private static final String SPI_CHANNEL = "EMAIL";

    /**
     * 时间展示格式，与任务日志一致。
     */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    public String type() {
        return JobAlarmMessage.TYPE_EMAIL;
    }

    @Override
    public void notify(JobAlarmMessage message) throws Exception {
        String[] targets = recipients(message == null ? null : message.target());
        if (targets.length == 0) {
            throw new IllegalStateException("告警目标里没有有效的收件邮箱");
        }
        SmtpSetting smtp = GlobalSettingFactory.getInstance().get(SETTING_GROUP, SmtpSetting.class);
        if (smtp == null || !smtp.isValid()) {
            throw new IllegalStateException("邮件发信未配置，请先在系统设置里配置 smtp 分组");
        }
        MessagePush push = buildEmailPush(smtp);
        if (push == null) {
            throw new IllegalStateException("未找到邮件渠道 SPI（email），请确认 utils-support-email-starter 在依赖中");
        }
        MessageResponse response = push.send(MessageRequest.builder()
                .to(String.join(",", targets))
                .subject(subject(message))
                .content(content(message))
                .build());
        if (response == null || !response.isSuccess()) {
            String reason = response == null ? "邮件渠道无返回" : response.getErrorMessage();
            throw new IllegalStateException("邮件告警发送失败: " + reason);
        }
        log.info("[system] 调度任务邮件告警已发送 job={} target={}", message.jobName(), message.target());
    }

    /**
     * 用系统配置装配邮件 SPI 实例。
     *
     * <p>与 {@code SysMessagePushServiceImpl#buildEmailPush} 保持同一份参数口径：
     * 字段名一旦不一致，SPI 内部会拿不到配置而静默按空值发信。</p>
     *
     * @param smtp 系统配置里的邮件设置
     * @return 邮件渠道实例；SPI 未注册时返回 null
     */
    private MessagePush buildEmailPush(SmtpSetting smtp) {
        MessageEnvironment environment = new MessageEnvironment();
        set(environment, "smtp.host", smtp.getHost());
        set(environment, "smtp.port", String.valueOf(smtp.getPort()));
        set(environment, "smtp.username", smtp.resolveUsername());
        set(environment, "smtp.password", smtp.getPassword());
        set(environment, "smtp.from",
                StringUtils.isNotBlank(smtp.getFrom()) ? smtp.getFrom() : smtp.resolveUsername());
        set(environment, "smtp.auth", String.valueOf(smtp.isAuth()));
        set(environment, "smtp.starttls", String.valueOf(smtp.isStarttls()));
        set(environment, "smtp.ssl.enable", String.valueOf(smtp.isSslEnable()));
        ServiceProvider<MessagePush> provider = ServiceProvider.of(MessagePush.class);
        if (!provider.getExtensions().contains(SPI_CHANNEL)) {
            return null;
        }
        return provider.getNewExtension(SPI_CHANNEL, environment);
    }

    /**
     * 写一个环境参数，空值不写（让 SPI 用自身默认值而不是空串）。
     *
     * @param environment 环境参数
     * @param key         参数名
     * @param value       参数值
     */
    private void set(MessageEnvironment environment, String key, String value) {
        if (value != null) {
            environment.set(key, value);
        }
    }

    /**
     * 拆收件人：逗号（中英文）或分号分隔，去空白。
     *
     * @param target 告警目标
     * @return 收件人数组
     */
    private String[] recipients(String target) {
        if (target == null || target.isBlank()) {
            return new String[0];
        }
        List<String> list = new ArrayList<>();
        for (String item : target.split("[,，;；]")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list.toArray(String[]::new);
    }

    /**
     * 告警邮件标题。
     *
     * @param message 告警消息
     * @return 标题
     */
    private String subject(JobAlarmMessage message) {
        return "【调度任务失败】" + safe(message.jobName());
    }

    /**
     * 告警邮件正文：任务、节点、耗时、时刻与失败原因。
     *
     * @param message 告警消息
     * @return 正文
     */
    private String content(JobAlarmMessage message) {
        long occurredAt = message.occurredAtMs() > 0 ? message.occurredAtMs() : System.currentTimeMillis();
        return "任务: " + safe(message.jobName()) + "\n"
                + "节点: " + safe(message.nodeId()) + "\n"
                + "耗时: " + message.costMs() + "ms\n"
                + "时刻: " + TIME_FORMAT.format(Instant.ofEpochMilli(occurredAt)) + "\n"
                + "原因:\n" + safe(message.exception()) + "\n";
    }

    /**
     * 空值兜底。
     *
     * @param text 文本
     * @return 非空文本
     */
    private String safe(String text) {
        return text == null || text.isBlank() ? "—" : text;
    }
}
