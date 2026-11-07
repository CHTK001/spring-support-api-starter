// [P3C BL-边界 豁免]

package com.chua.starter.system.support.verify;

import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.task.message.MessageEnvironment;
import com.chua.common.support.task.message.MessagePush;
import com.chua.common.support.task.message.MessageRequest;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.starter.service.setting.SmtpSetting;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮箱验证码渠道
 *
 * <p>发信参数取系统配置里的 smtp 分组（sys_setting 中 group=smtp 的行），
 * 投递复用邮件 SPI 实现 {@code EmailPush}，本类只负责把配置翻译成渠道环境并校验投递结果。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Spi("EMAIL")
public class EmailVerifyCodeChannel implements VerifyCodeChannel {

    /**
     * 渠道标识，与 SPI 注册键一致
     */
    public static final String PROVIDER = "EMAIL";

    /**
     * 邮件主题
     */
    private static final String SUBJECT = "邮箱验证码";

    @Override
    /**
     * 配置好的
    */
    public boolean configured() {
        SmtpSetting setting = setting();
        return null != setting && setting.isValid();
    }

    @Override
    /**
     * 发送
     * @param message 消息
     */
    public void send(VerifyCodeMessage message) throws Exception {
        SmtpSetting setting = setting();
        if (null == setting || !setting.isValid()) {
            throw new IllegalStateException("服务器未配置邮箱发信通道，请联系管理员在系统配置中填写 smtp 分组");
        }
        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        if (!serviceProvider.getExtensions().contains(PROVIDER)) {
            throw new IllegalStateException("邮件渠道未安装，当前可用渠道: "
                    + String.join(",", serviceProvider.getExtensions()));
        }
        MessagePush messagePush = serviceProvider.getNewExtension(PROVIDER, buildEnvironment(setting));
        if (null == messagePush) {
            throw new IllegalStateException("邮件渠道初始化失败");
        }
        MessageResponse response = messagePush.send(MessageRequest.builder()
                .to(message.target())
                .subject(SUBJECT)
                .content(buildContent(message))
                .build());
        if (null == response) {
            throw new IllegalStateException("邮件渠道无返回");
        }
        if (!response.isSuccess()) {
            throw new IllegalStateException(null == response.getErrorMessage() ? "邮件投递失败" : response.getErrorMessage());
        }
    }

    /**
     * 读取 smtp 分组配置
     *
     * @return 配置，未注册时为空
     */
    private SmtpSetting setting() {
        return GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
    }

    /**
     * 把系统配置翻译成邮件渠道环境参数
     *
     * @param setting smtp 配置
     * @return 渠道环境
     */
    private MessageEnvironment buildEnvironment(SmtpSetting setting) {
        MessageEnvironment environment = new MessageEnvironment();
        set(environment, "smtp.host", setting.getHost());
        set(environment, "smtp.port", String.valueOf(setting.getPort()));
        set(environment, "smtp.username", setting.resolveUsername());
        set(environment, "smtp.password", setting.getPassword());
        set(environment, "smtp.from", null == setting.getFrom() ? setting.resolveUsername() : setting.getFrom());
        set(environment, "smtp.auth", String.valueOf(setting.isAuth()));
        set(environment, "smtp.starttls", String.valueOf(setting.isStarttls()));
        set(environment, "smtp.ssl.enable", String.valueOf(setting.isSslEnable()));
        return environment;
    }

    /**
     * 写入环境参数，空值跳过
     *
     * @param environment 环境参数
     * @param key         配置键
     * @param value       配置值，可为空
     */
    private void set(MessageEnvironment environment, String key, String value) {
        if (null != value) {
            environment.set(key, value);
        }
    }

    /**
     * 验证码正文
     *
     * @param message 验证码消息
     * @return 正文
     */
    private String buildContent(VerifyCodeMessage message) {
        return "您的验证码是：" + message.code() + "，" + message.expireMinutes() + "分钟内有效。"
                + "若非本人操作，请忽略本邮件并尽快修改账号密码。";
    }
}
