// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.task.message.MessageEnvironment;
import com.chua.common.support.task.message.MessagePush;
import com.chua.common.support.task.message.MessageRequest;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.common.support.task.message.TemplateInfo;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.entity.system.SysMessageLog;
import com.chua.starter.entity.system.SysMessageRecipient;
import com.chua.starter.entity.system.SysMessageTemplate;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.service.setting.SmsSetting;
import com.chua.starter.service.setting.SmtpSetting;
import com.chua.starter.system.mapper.SysMessageLogMapper;
import com.chua.starter.system.mapper.SysMessageRecipientMapper;
import com.chua.starter.system.mapper.SysMessageTemplateMapper;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.system.pojo.message.MessageContentSendRequest;
import com.chua.starter.system.pojo.message.MessageSendRequest;
import com.chua.starter.system.pojo.message.SendByCodeRequest;
import com.chua.starter.system.service.SysMessagePushService;
import com.chua.starter.system.service.SysMessageService;
import com.chua.starter.system.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息推送服务实现
 *
 * <p>模板自带渠道(EMAIL/SMS)，账号凭据统一从 sys_setting 读取：
 * EMAIL 取 {@code smtp} 分组，SMS 取 {@code sms} 分组并按 appType 选择运营商 SPI。
 * 发送结果落到 {@code sys_message_log}，并在站内消息中心 {@code sys_message} 留痕。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessagePushServiceImpl implements SysMessagePushService {

    /**
     * 日志返回码 - 成功
     */
    private static final String CODE_SUCCESS = "SUCCESS";

    /**
     * 日志返回码 - 失败
     */
    private static final String CODE_FAILURE = "FAILURE";

    /**
     * 默认内容类型
     */
    private static final String CONTENT_TYPE_TEXT = "text";

    /**
     * 渠道：邮件
     */
    private static final String CHANNEL_EMAIL = "EMAIL";

    /**
     * 渠道：短信
     */
    private static final String CHANNEL_SMS = "SMS";

    /**
     * 站内消息渠道：站内(默认)
     */
    private static final String SYS_CHANNEL_PLATFORM = "PLATFORM";

    /**
     * 日志接收人长度上限，对齐 sys_message_log.sys_message_log_target
     */
    private static final int TARGET_MAX_LENGTH = 255;

    /**
     * 日志渠道长度上限，对齐 sys_message_log.sys_message_log_channel
     */
    private static final int CHANNEL_MAX_LENGTH = 64;

    /**
     * 日志主题长度上限，对齐 sys_message_log.sys_message_log_subject
     */
    private static final int SUBJECT_MAX_LENGTH = 512;

    /**
     * 日志返回消息长度上限，对齐 sys_message_log.sys_message_message
     */
    private static final int MESSAGE_MAX_LENGTH = 1000;

    /**
     * 服务商消息标识长度上限，对齐 sys_message_log.sys_message_log_message_id
     */
    private static final int MESSAGE_ID_MAX_LENGTH = 255;

    /**
     * 模板变量占位符：${key} 与 {key}
     */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$?\\{([\\w.]+)}");

    /**
     * SYS消息日志映射器
     */
    private final SysMessageLogMapper sysMessageLogMapper;

    /**
     * SYS消息模板映射器
     */
    private final SysMessageTemplateMapper sysMessageTemplateMapper;

    /**
     * 消息模板服务，占位符渲染口径由它统一持有
     */
    private final SysMessageTemplateService sysMessageTemplateService;

    /**
     * 站内消息服务，发送邮件/短信后在消息中心留痕
     */
    private final SysMessageService sysMessageService;

    /**
     * 鉴权服务(可选)：取当前登录用户作为站内消息发送者
     */
    private final ObjectProvider<AuthService> authServiceProvider;

    /**
     * 角色 Mapper：按角色编码解析接收人
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 用户-角色 Mapper
     */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 用户 Mapper：取邮箱/手机号
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 消息接收人送达记录 Mapper
     */
    private final SysMessageRecipientMapper sysMessageRecipientMapper;

    /**
     * 内置模板变量清单(供前端 tip 展示)
     */
    public static final List<String> BUILT_IN_VARS = List.of("date", "time");

    @Override
    /**
     * 按模板ID发送
     */
    public ReturnResult<List<MessageResponse>> send(MessageSendRequest request) {
        if (null == request || null == request.sysMessageTemplateId()) {
            return ReturnResult.illegal("模板ID不能为空");
        }
        if (CollectionUtils_isEmpty(request.target())) {
            return ReturnResult.illegal("接收人不能为空");
        }
        SysMessageTemplate template = sysMessageTemplateMapper.selectById(request.sysMessageTemplateId());
        if (null == template) {
            return ReturnResult.error("模板不存在");
        }
        if (isDisabled(template.getSysMessageTemplateStatus())) {
            return ReturnResult.error("模板已停用");
        }
        return doSendByTemplate(template, request.target(), request.subject(), request.params());
    }

    @Override
    /**
     * 按模板编码发送
     */
    public ReturnResult<List<MessageResponse>> sendByCode(SendByCodeRequest request) {
        if (null == request || StringUtils.isBlank(request.templateCode())) {
            return ReturnResult.illegal("模板编码不能为空");
        }
        SysMessageTemplate template = sysMessageTemplateMapper.selectOne(Wrappers.<SysMessageTemplate>lambdaQuery()
                .eq(SysMessageTemplate::getSysMessageTemplateCode, request.templateCode().trim())
                .last("limit 1"));
        if (null == template) {
            return ReturnResult.error("模板不存在");
        }
        if (isDisabled(template.getSysMessageTemplateStatus())) {
            return ReturnResult.error("模板已停用");
        }
        // 解析接收人：角色解析 + 手工兜底合并去重
        String channel = trimToNull(template.getSysMessageChannel());
        List<String> targets = resolveRecipients(channel, request.roleKeys(), request.targets(), request.target());
        if (CollectionUtils_isEmpty(targets)) {
            return ReturnResult.illegal("未解析到有效接收人(请选择角色或填写接收人)");
        }
        Map<String, Object> params = null == request.params()
                ? new LinkedHashMap<>() : new LinkedHashMap<>(request.params());
        return doSendByTemplate(template, targets, null, params);
    }

    /**
     * 按角色编码解析接收人邮箱/手机号，并与手工接收人合并去重
     *
     * @param channel   模板渠道(EMAIL/SMS)
     * @param roleKeys  角色编码列表
     * @param extraTargets 手工接收人列表
     * @param singleTarget 旧版单接收人字段
     * @return 去重后的接收人列表
     */
    private List<String> resolveRecipients(String channel, List<String> roleKeys,
                                           List<String> extraTargets, String singleTarget) {
        Set<String> result = new LinkedHashSet<>();
        // 手工兜底
        if (null != extraTargets) {
            for (String t : extraTargets) {
                if (StringUtils.isNotBlank(t)) {
                    result.add(t.trim());
                }
            }
        }
        if (StringUtils.isNotBlank(singleTarget)) {
            result.add(singleTarget.trim());
        }
        // 按角色解析
        if (null != roleKeys && !roleKeys.isEmpty() && null != channel) {
            List<SysRole> roles = sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                    .in(SysRole::getSysRoleCode, roleKeys));
            if (!roles.isEmpty()) {
                List<Integer> roleIds = roles.stream().map(SysRole::getSysRoleId).toList();
                List<SysUserRole> urList = sysUserRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .in(SysUserRole::getSysRoleId, roleIds));
                if (!urList.isEmpty()) {
                    List<Integer> userIds = urList.stream().map(SysUserRole::getSysUserId).distinct().toList();
                    List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                    boolean wantEmail = CHANNEL_EMAIL.equalsIgnoreCase(channel);
                    for (SysUser u : users) {
                        String addr = wantEmail ? u.getSysUserEmail() : u.getSysUserPhone();
                        if (StringUtils.isNotBlank(addr)) {
                            result.add(addr.trim());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    @Override
    /**
     * 按内容发送
     */
    public ReturnResult<List<MessageResponse>> sendContent(MessageContentSendRequest request) {
        if (null == request || StringUtils.isBlank(request.channel())) {
            return ReturnResult.illegal("消息渠道不能为空");
        }
        if (CollectionUtils_isEmpty(request.target())) {
            return ReturnResult.illegal("接收人不能为空");
        }
        if (StringUtils.isBlank(request.content())) {
            return ReturnResult.illegal("消息内容不能为空");
        }
        // 邮件与短信必须走模板下发（运营商侧模板/签名需登记），不允许自由内容直连
        if (CHANNEL_EMAIL.equalsIgnoreCase(request.channel()) || CHANNEL_SMS.equalsIgnoreCase(request.channel())) {
            return ReturnResult.error("邮件和短信必须通过模板发送，不支持自由内容");
        }
        // 其余渠道(机器人/自定义)在移除供应商后不再支持自由内容直发
        return ReturnResult.error("自由内容发送已停用，请通过模板发送");
    }

    @Override
    /**
     * 按渠道同步模板到本地模板表
     */
    public ReturnResult<Integer> syncTemplates(String channel) {
        String ch = StringUtils.isBlank(channel) ? CHANNEL_SMS : channel.trim();
        if (CHANNEL_EMAIL.equalsIgnoreCase(ch)) {
            return syncEmailTemplates();
        }
        if (CHANNEL_SMS.equalsIgnoreCase(ch)) {
            return syncSmsTemplates();
        }
        return ReturnResult.error("不支持的同步渠道: " + ch + "(仅支持 EMAIL/SMS)");
    }

    /**
     * 同步短信模板：读 SmsSetting -> 选 SPI -> listTemplates -> upsert(channel=SMS)
     */
    private ReturnResult<Integer> syncSmsTemplates() {
        SmsSetting sms = GlobalSettingFactory.getInstance().get("sms", SmsSetting.class);
        if (null == sms || !sms.isValid()) {
            return ReturnResult.error("短信服务未配置，请先在系统设置中配置短信分组");
        }
        String spiChannel = resolveSmsSpiChannel(sms.getAppType());
        if (null == spiChannel) {
            return ReturnResult.error("不支持的短信服务商类型: " + sms.getAppType());
        }
        MessagePush push = buildSmsPush(sms, spiChannel);
        if (null == push) {
            return ReturnResult.error("短信渠道初始化失败: " + spiChannel);
        }
        List<TemplateInfo> templates;
        try {
            templates = push.listTemplates();
        } catch (Exception e) {
            log.warn("拉取运营商短信模板失败: {}", e.getMessage());
            return ReturnResult.error("拉取运营商短信模板失败: " + e.getMessage());
        }
        return upsertTemplates(templates, CHANNEL_SMS);
    }

    /**
     * 同步邮件模板：读 SmtpSetting -> 构建 email SPI -> listTemplates -> upsert(channel=EMAIL)。
     * 当前邮件 SPI 返回空列表即返回 0，未来支持模板列表时自动生效。
     */
    private ReturnResult<Integer> syncEmailTemplates() {
        SmtpSetting smtp = GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
        if (null == smtp || !smtp.isValid()) {
            return ReturnResult.error("邮件发信未配置，请先在系统设置中配置 smtp 分组");
        }
        MessagePush push = buildEmailPush(smtp);
        if (null == push) {
            return ReturnResult.error("邮件渠道初始化失败: " + CHANNEL_EMAIL);
        }
        List<TemplateInfo> templates;
        try {
            templates = push.listTemplates();
        } catch (Exception e) {
            log.warn("拉取邮件模板列表失败: {}", e.getMessage());
            return ReturnResult.error("拉取邮件模板列表失败: " + e.getMessage());
        }
        return upsertTemplates(templates, CHANNEL_EMAIL);
    }

    /**
     * 把渠道返回的模板列表 upsert 到 sys_message_template
     */
    private ReturnResult<Integer> upsertTemplates(List<TemplateInfo> templates, String channel) {
        if (null == templates || templates.isEmpty()) {
            return ReturnResult.ok(0);
        }
        int synced = 0;
        for (TemplateInfo item : templates) {
            if (null == item || StringUtils.isBlank(item.id())) {
                continue;
            }
            SysMessageTemplate exist = sysMessageTemplateMapper.selectOne(Wrappers.<SysMessageTemplate>lambdaQuery()
                    .eq(SysMessageTemplate::getSysMessageTemplateCode, item.id())
                    .last("limit 1"));
            String paramSchema = extractParams(item.content());
            if (null == exist) {
                SysMessageTemplate template = new SysMessageTemplate();
                template.setSysMessageTemplateCode(item.id());
                template.setSysMessageTemplateName(item.name());
                template.setSysMessageTemplateContent(item.content());
                template.setSysMessageTemplateType(CONTENT_TYPE_TEXT);
                template.setSysMessageChannel(channel);
                template.setSysMessageExternalCode(item.id());
                template.setSysMessageParamSchema(paramSchema);
                template.setSysMessageTemplateStatus(1);
                synced += sysMessageTemplateMapper.insert(template) > 0 ? 1 : 0;
            } else {
                exist.setSysMessageTemplateName(item.name());
                exist.setSysMessageTemplateContent(item.content());
                exist.setSysMessageChannel(channel);
                exist.setSysMessageExternalCode(item.id());
                exist.setSysMessageParamSchema(paramSchema);
                synced += sysMessageTemplateMapper.updateById(exist) > 0 ? 1 : 0;
            }
        }
        return ReturnResult.ok(synced);
    }

    @Override
    /**
     * 送达/已读统计
     */
    public ReturnResult<Map<String, Object>> recipientStats(Long sysMessageId) {
        if (null == sysMessageId) {
            return ReturnResult.illegal("消息ID不能为空");
        }
        List<SysMessageRecipient> list = sysMessageRecipientMapper.selectList(
                Wrappers.<SysMessageRecipient>lambdaQuery()
                        .eq(SysMessageRecipient::getSysMessageId, sysMessageId));
        long sent = list.stream().filter(r -> SysMessageRecipient.DELIVERY_SENT.equals(r.getDeliveryStatus())).count();
        long failed = list.stream().filter(r -> SysMessageRecipient.DELIVERY_FAILED.equals(r.getDeliveryStatus())).count();
        long read = list.stream().filter(r -> SysMessageRecipient.READ_READ.equals(r.getReadStatus())).count();
        List<String> delivered = list.stream()
                .filter(r -> SysMessageRecipient.DELIVERY_SENT.equals(r.getDeliveryStatus()))
                .map(SysMessageRecipient::getRecipientAddress).toList();
        List<String> readList = list.stream()
                .filter(r -> SysMessageRecipient.READ_READ.equals(r.getReadStatus()))
                .map(SysMessageRecipient::getRecipientAddress).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", list.size());
        result.put("sent", sent);
        result.put("failed", failed);
        result.put("read", read);
        result.put("unread", list.size() - read);
        result.put("deliveredRecipients", delivered);
        result.put("readRecipients", readList);
        return ReturnResult.ok(result);
    }

    /**
     * 按模板渠道加载凭据、构造 SPI 实例并逐接收人发送，同时落日志与站内消息
     *
     * @param template       模板
     * @param targets        接收人
     * @param overrideSubject 覆盖主题(可为空，默认模板标题)
     * @param params         模板变量
     * @return 每个接收人的发送结果
     */
    private ReturnResult<List<MessageResponse>> doSendByTemplate(SysMessageTemplate template,
                                                                  List<String> targets,
                                                                  String overrideSubject,
                                                                  Map<String, Object> params) {
        String channel = trimToNull(template.getSysMessageChannel());
        if (null == channel) {
            return ReturnResult.error("模板未配置渠道");
        }
        MessageEnvironment environment = new MessageEnvironment();
        String spiChannel;
        String externalTemplateId;
        if (CHANNEL_EMAIL.equalsIgnoreCase(channel)) {
            spiChannel = CHANNEL_EMAIL;
            SmtpSetting smtp = GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
            if (null == smtp || !smtp.isValid()) {
                return ReturnResult.error("邮件发信未配置，请先在系统设置中配置 smtp 分组");
            }
            set(environment, "smtp.host", smtp.getHost());
            set(environment, "smtp.port", String.valueOf(smtp.getPort()));
            set(environment, "smtp.username", smtp.resolveUsername());
            set(environment, "smtp.password", smtp.getPassword());
            set(environment, "smtp.from", StringUtils.isNotBlank(smtp.getFrom()) ? smtp.getFrom() : smtp.resolveUsername());
            set(environment, "smtp.auth", String.valueOf(smtp.isAuth()));
            set(environment, "smtp.starttls", String.valueOf(smtp.isStarttls()));
            set(environment, "smtp.ssl.enable", String.valueOf(smtp.isSslEnable()));
            externalTemplateId = null;
        } else if (CHANNEL_SMS.equalsIgnoreCase(channel)) {
            SmsSetting sms = GlobalSettingFactory.getInstance().get("sms", SmsSetting.class);
            if (null == sms || !sms.isValid()) {
                return ReturnResult.error("短信服务未配置，请先在系统设置中配置 sms 分组");
            }
            spiChannel = resolveSmsSpiChannel(sms.getAppType());
            if (null == spiChannel) {
                return ReturnResult.error("不支持的短信服务商类型: " + sms.getAppType());
            }
            externalTemplateId = trimToNull(template.getSysMessageExternalCode());
            if (null == externalTemplateId) {
                return ReturnResult.error("短信模板必须配置外部运营商模板码");
            }
            fillSmsEnvironment(environment, sms, spiChannel);
        } else {
            return ReturnResult.error("不支持的消息渠道: " + channel);
        }

        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        if (!serviceProvider.getExtensions().contains(spiChannel)) {
            return ReturnResult.error("暂不支持的消息渠道: " + spiChannel);
        }
        MessagePush messagePush = serviceProvider.getNewExtension(spiChannel, environment);
        if (null == messagePush) {
            return ReturnResult.error("消息渠道初始化失败: " + spiChannel);
        }

        String subject = StringUtils.isNotBlank(overrideSubject) ? overrideSubject : template.getSysMessageTemplateTitle();
        // 注入内置变量(日期/时间)
        Map<String, Object> renderParams = injectBuiltInVars(params);
        // 先在站内消息中心留痕并取得消息ID，再逐接收人发送并写送达记录
        SysMessage platformMessage = recordPlatformMessage(template, channel, subject,
                template.getSysMessageTemplateContent(), renderParams);
        Long sysMessageId = null;
        if (null != platformMessage && null != platformMessage.getSysMessageId()) {
            sysMessageId = platformMessage.getSysMessageId().longValue();
        }
        String recipientType = CHANNEL_SMS.equalsIgnoreCase(channel)
                ? SysMessageRecipient.TYPE_PHONE : SysMessageRecipient.TYPE_EMAIL;
        return doSend(messagePush, template.getSysMessageTemplateId(), sysMessageId, recipientType,
                externalTemplateId, subject, template.getSysMessageTemplateContent(),
                template.getSysMessageTemplateType(), renderParams, targets);
    }

    /**
     * 构造短信 SPI 实例(同步模板用)
     *
     * @param sms        短信配置
     * @param spiChannel SPI 渠道名(大写)
     * @return SPI 实例，初始化失败返回空
     */
    private MessagePush buildSmsPush(SmsSetting sms, String spiChannel) {
        MessageEnvironment environment = new MessageEnvironment();
        fillSmsEnvironment(environment, sms, spiChannel);
        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        if (!serviceProvider.getExtensions().contains(spiChannel)) {
            return null;
        }
        return serviceProvider.getNewExtension(spiChannel, environment);
    }

    /**
     * 构造邮件 SPI 实例(同步模板用)
     *
     * @param smtp 邮件配置
     * @return SPI 实例，初始化失败返回空
     */
    private MessagePush buildEmailPush(SmtpSetting smtp) {
        MessageEnvironment environment = new MessageEnvironment();
        set(environment, "smtp.host", smtp.getHost());
        set(environment, "smtp.port", String.valueOf(smtp.getPort()));
        set(environment, "smtp.username", smtp.resolveUsername());
        set(environment, "smtp.password", smtp.getPassword());
        set(environment, "smtp.from", StringUtils.isNotBlank(smtp.getFrom()) ? smtp.getFrom() : smtp.resolveUsername());
        set(environment, "smtp.auth", String.valueOf(smtp.isAuth()));
        set(environment, "smtp.starttls", String.valueOf(smtp.isStarttls()));
        set(environment, "smtp.ssl.enable", String.valueOf(smtp.isSslEnable()));
        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        if (!serviceProvider.getExtensions().contains(CHANNEL_EMAIL)) {
            return null;
        }
        return serviceProvider.getNewExtension(CHANNEL_EMAIL, environment);
    }

    /**
     * 把短信凭据翻译成各运营商 SPI 读取的环境参数
     *
     * @param environment 环境参数
     * @param sms         短信配置
     * @param spiChannel  SPI 渠道名(大写)
     */
    private void fillSmsEnvironment(MessageEnvironment environment, SmsSetting sms, String spiChannel) {
        if ("TENCENT-SMS".equals(spiChannel)) {
            // 腾讯云：SecretId/SecretKey/SdkAppId/签名
            set(environment, "sms.secretId", trimToNull(sms.getAppId()));
            set(environment, "sms.secretKey", trimToNull(sms.getAppSecret()));
            set(environment, "sms.appId", trimToNull(sms.getAppId()));
            set(environment, "sms.signName", trimToNull(sms.getAppSign()));
        } else {
            // 阿里云 / 百度云：AccessKey/SecretKey/签名
            set(environment, "sms.accessKey", trimToNull(sms.getAppId()));
            set(environment, "sms.secretKey", trimToNull(sms.getAppSecret()));
            set(environment, "sms.signName", trimToNull(sms.getAppSign()));
        }
    }

    /**
     * 按 appType 解析短信 SPI 渠道名(大写，对齐 SPI 注册键)
     *
     * @param appType 短信服务商类型
     * @return SPI 渠道名，不支持返回空
     */
    private String resolveSmsSpiChannel(String appType) {
        String type = trimToNull(appType);
        if (null == type) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "aliyun", "alibaba" -> "ALIBABA-SMS";
            case "tencent", "tencentcloud" -> "TENCENT-SMS";
            case "baidu", "bce" -> "BAIDU-SMS";
            default -> null;
        };
    }

    /**
     * 逐接收人发送并落日志
     *
     * @param messagePush       已注入配置的 SPI 实例
     * @param templateId        模板ID，按内容发送时为空（仅用于日志）
     * @param externalTemplateId 传给 SPI 的模板标识：短信为运营商模板码，邮件为空
     * @param subject          主题模板(可为空)
     * @param content           内容模板
     * @param type              内容类型来源(模板类型)
     * @param params            模板变量
     * @param targets           接收人
     * @return 每个接收人的发送结果
     */
    private ReturnResult<List<MessageResponse>> doSend(MessagePush messagePush, Long templateId,
                                                       Long sysMessageId, String recipientType,
                                                       String externalTemplateId, String subject, String content,
                                                       String type, Map<String, Object> params,
                                                       List<String> targets) {
        String contentType = resolveContentType(type);
        String batchId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> safeParams = null == params ? new LinkedHashMap<>() : params;
        List<MessageResponse> results = new ArrayList<>(targets.size());
        for (String target : targets) {
            if (StringUtils.isBlank(target)) {
                continue;
            }
            String to = target.trim();
            String renderedContent = sysMessageTemplateService.render(content, safeParams);
            String renderedSubject = sysMessageTemplateService.render(subject, safeParams);
            MessageResponse response;
            try {
                response = messagePush.send(MessageRequest.builder()
                        .to(to)
                        .subject(renderedSubject)
                        .content(renderedContent)
                        .contentType(contentType)
                        .templateId(externalTemplateId)
                        .templateParams(toParamMap(safeParams))
                        .build());
            } catch (Exception e) {
                log.error("消息发送失败, 渠道: {}, 接收人: {}", messagePush.getProvider(), to, e);
                response = MessageResponse.failure(e.getMessage());
            }
            results.add(response);
            saveLog(templateId, messagePush.getProvider(), to, renderedSubject,
                    renderedContent, safeParams, response, batchId);
            saveRecipient(sysMessageId, recipientType, to, response);
        }
        return ReturnResult.ok(results);
    }

    /**
     * 在站内消息中心留一条 EMAIL/SMS 记录
     *
     * @param template   模板
     * @param channel    渠道(EMAIL/SMS)
     * @param subjectTpl 主题模板
     * @param contentTpl 内容模板
     * @param params     模板变量
     */
    private SysMessage recordPlatformMessage(SysMessageTemplate template, String channel,
                                             String subjectTpl, String contentTpl, Map<String, Object> params) {
        try {
            String title = sysMessageTemplateService.render(subjectTpl, params);
            String content = sysMessageTemplateService.render(contentTpl, params);
            SysMessage message = new SysMessage();
            message.setSysMessageTitle(truncate(title, 255));
            message.setSysMessageContent(content);
            message.setSysMessageType(SysMessage.Type.NOTICE);
            message.setSysMessageLevel(SysMessage.Level.NORMAL);
            message.setSysMessageChannel(channel);
            message.setSysMessageSenderId(currentUserId());
            message.setSysMessageSenderName("系统");
            message.setSysMessageReceiverId(0);
            message.setSysMessageRead(0);
            message.setSysMessageSendTime(LocalDateTime.now());
            message.setSysMessageStatus(SysMessage.Status.SENT);
            message.setSysMessageBizType("message:" + channel.toLowerCase());
            message.setSysMessageBizId(String.valueOf(template.getSysMessageTemplateId()));
            sysMessageService.save(message);
            return message;
        } catch (Exception e) {
            log.warn("写站内消息记录失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 写一条接收人送达记录，失败不影响发送结果
     *
     * @param sysMessageId 关联站内消息ID，可为空
     * @param recipientType 接收人类型(EMAIL/PHONE)
     * @param address      接收人地址
     * @param response     发送结果
     */
    private void saveRecipient(Long sysMessageId, String recipientType, String address, MessageResponse response) {
        try {
            SysMessageRecipient r = new SysMessageRecipient();
            r.setSysMessageId(sysMessageId);
            r.setRecipientType(recipientType);
            r.setRecipientAddress(truncate(address, TARGET_MAX_LENGTH));
            r.setDeliveryStatus(null != response && response.isSuccess()
                    ? SysMessageRecipient.DELIVERY_SENT : SysMessageRecipient.DELIVERY_FAILED);
            r.setReadStatus(SysMessageRecipient.READ_UNREAD);
            sysMessageRecipientMapper.insert(r);
        } catch (Exception e) {
            log.warn("写接收人送达记录失败: {}", e.getMessage());
        }
    }

    /**
     * 注入内置模板变量(date/time)到参数副本
     *
     * @param params 原始参数，可为空
     * @return 合并内置变量后的参数副本
     */
    private Map<String, Object> injectBuiltInVars(Map<String, Object> params) {
        Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        merged.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        if (null != params) {
            merged.putAll(params);
        }
        return merged;
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
     * 判定内容类型，仅放行 SPI 实现识别的取值
     *
     * @param type 模板类型
     * @return 内容类型
     */
    private String resolveContentType(String type) {
        if (StringUtils.isBlank(type)) {
            return CONTENT_TYPE_TEXT;
        }
        return switch (type.trim().toLowerCase()) {
            case "html", "richtext" -> "html";
            case "text", "json", "raw" -> type.trim().toLowerCase();
            default -> CONTENT_TYPE_TEXT;
        };
    }

    /**
     * 落一条发送日志，失败不影响发送结果返回
     *
     * @param templateId 模板ID，可为空
     * @param channel    渠道标识
     * @param target     接收人
     * @param subject    渲染后主题
     * @param content    渲染后内容
     * @param params     模板变量
     * @param response   发送结果
     * @param batchId    批次号
     */
    private void saveLog(Long templateId, String channel, String target,
                         String subject, String content, Map<String, Object> params,
                         MessageResponse response, String batchId) {
        try {
            SysMessageLog entity = new SysMessageLog();
            entity.setSysMessageLogTarget(truncate(target, TARGET_MAX_LENGTH));
            entity.setSysMessageTemplateId(templateId);
            entity.setSysMessageProviderId(null);
            entity.setSysMessageLogChannel(truncate(channel, CHANNEL_MAX_LENGTH));
            entity.setSysMessageLogContent(content);
            entity.setSysMessageLogParam(Json.toJson(params));
            entity.setSysMessageLogSubject(truncate(subject, SUBJECT_MAX_LENGTH));
            entity.setSysMessageReadStatus(0);
            entity.setSysMessageBatchId(batchId);
            if (null != response) {
                entity.setSysMessageCode(response.isSuccess() ? CODE_SUCCESS : CODE_FAILURE);
                entity.setSysMessageCost(response.getDurationMillis());
                entity.setSysMessageMessage(truncate(response.isSuccess()
                        ? StringUtils.defaultIfBlank(response.getErrorMessage(), "发送成功")
                        : response.getErrorMessage(), MESSAGE_MAX_LENGTH));
                entity.setSysMessageLogMessageId(truncate(response.getMessageId(), MESSAGE_ID_MAX_LENGTH));
            } else {
                entity.setSysMessageCode(CODE_FAILURE);
                entity.setSysMessageMessage("渠道无返回");
            }
            sysMessageLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("保存消息发送日志失败, 接收人: {}, 原因: {}", target, e.getMessage());
        }
    }

    /**
     * 模板变量转 SPI 需要的字符串键值对
     *
     * @param params 模板变量
     * @return 字符串键值对，空值转为空串
     */
    private Map<String, String> toParamMap(Map<String, Object> params) {
        Map<String, String> result = new LinkedHashMap<>();
        params.forEach((key, value) -> result.put(key, null == value ? "" : String.valueOf(value)));
        return result;
    }

    /**
     * 从模板内容中提取 ${var}/{var} 变量名，逗号拼接为 param_schema
     *
     * @param content 模板内容
     * @return 逗号分隔的变量名，无变量返回空
     */
    private String extractParams(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(content);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names.isEmpty() ? null : String.join(",", names);
    }

    /**
     * 当前登录用户ID，无鉴权上下文时返回 0(系统)
     *
     * @return 用户ID
     */
    private Integer currentUserId() {
        try {
            AuthService auth = null == authServiceProvider ? null : authServiceProvider.getIfAvailable();
            if (null == auth) {
                return 0;
            }
            CurrentUser user = auth.getCurrentUser();
            if (null == user || StringUtils.isBlank(user.getUserId())) {
                return 0;
            }
            return Integer.parseInt(user.getUserId().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 状态是否为停用
     *
     * @param status 状态
     * @return 是否停用
     */
    private boolean isDisabled(Integer status) {
        return null != status && status == 0;
    }

    /**
     * 去除首尾空白，全空白返回空
     *
     * @param value 原值
     * @return 有效值或空
     */
    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    /**
     * 集合是否为空(避免引入 mybatis-plus CollectionUtils 的重载差异)
     *
     * @param collection 集合
     * @return 是否为空
     */
    private boolean CollectionUtils_isEmpty(List<String> collection) {
        return null == collection || collection.isEmpty();
    }

    /**
     * 截断超长字符串，避免写库被截断报错
     *
     * @param value     原值
     * @param maxLength 最大长度
     * @return 截断后的值
     */
    private String truncate(String value, int maxLength) {
        if (StringUtils.isBlank(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}