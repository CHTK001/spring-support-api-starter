package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessagePush;
import com.chua.common.support.lang.message.option.TemplateSetting;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.setting.SmsSetting;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.SmsCodeRequest;
import com.chua.starter.system.pojo.SmsLoginRequest;
import com.chua.starter.system.service.SmsCodeService;
import com.chua.starter.system.service.SysLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现
 * <p>使用 Redis 存储验证码，通过 SmsSetting 配置的 MessagePush 发送短信</p>
 *
 * @author CH
 * @since 2024/12/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    /**
     * Redis 验证码 Key 前缀
     */
    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 发送间隔时间（秒）
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;
    private final SysUserMapper sysUserMapper;
    private final SysLoginService sysLoginService;

    @Override
    public ReturnResult<Boolean> sendCode(SmsCodeRequest request) {
        String phone = request.getPhone();
        String bizType = request.getBizType();
        log.debug("发送短信验证码: phone={}, bizType={}", phone, bizType);

        // 检查发送频率
        String intervalKey = SMS_CODE_KEY_PREFIX + "interval:" + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(intervalKey))) {
            log.warn("短信验证码发送过于频繁: phone={}", phone);
            return ReturnResult.fail("验证码发送过于频繁，请稍后再试");
        }

        // 获取短信配置
        SmsSetting smsSetting = GlobalSettingFactory.getInstance().get("sms", SmsSetting.class);
        if (smsSetting == null || !smsSetting.isValid()) {
            log.error("短信配置未设置或无效");
            return ReturnResult.fail("短信服务未配置，请联系管理员");
        }

        // 登录业务需要验证手机号是否已注册
        if ("login".equals(bizType)) {
            SysUser user = sysUserMapper.selectOne(
                    Wrappers.<SysUser>lambdaQuery()
                            .eq(SysUser::getSysUserPhone, phone)
                            .eq(SysUser::getSysUserStatus, 1)
            );
            if (user == null) {
                log.warn("手机号未注册: phone={}", phone);
                return ReturnResult.fail("该手机号未注册");
            }
        }

        // 生成验证码(6位数字)
        String code = String.format("%0" + CODE_LENGTH + "d", (int) (Math.random() * Math.pow(10, CODE_LENGTH)));
        log.info("生成短信验证码: phone={}, code={}", phone, code);

        // 发送短信
        try {
            MessagePush messagePush = smsSetting.getMessagePush();
            TemplateSetting setting = TemplateSetting.builder()
                    .templateParam(JsonObject.create().fluentPut("code", code))
                    .signCode(smsSetting.getAppSign())
                    .build();

            List<PushResult> results = messagePush.publish(new String[]{phone}, setting);
            if (CollectionUtils.isEmpty(results)) {
                log.error("短信发送失败: phone={}, 无返回结果", phone);
                return ReturnResult.fail("短信发送失败");
            }

            PushResult result = results.getFirst();
            if (!result.isSuccess()) {
                log.error("短信发送失败: phone={}, msg={}", phone, result.getMessage());
                return ReturnResult.fail("短信发送失败: " + result.getMessage());
            }

            log.info("短信发送成功: phone={}", phone);
        } catch (Exception e) {
            log.error("短信发送异常: phone={}", phone, e);
            return ReturnResult.fail("短信发送失败: " + e.getMessage());
        }

        // 存储验证码到 Redis
        String codeKey = SMS_CODE_KEY_PREFIX + bizType + ":" + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送间隔
        stringRedisTemplate.opsForValue().set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        return ReturnResult.ok(true);
    }

    @Override
    public boolean verifyCode(String phone, String code, String bizType) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            return false;
        }

        String codeKey = SMS_CODE_KEY_PREFIX + bizType + ":" + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (StringUtils.isEmpty(storedCode)) {
            log.warn("验证码不存在或已过期: phone={}", phone);
            return false;
        }

        boolean valid = code.equals(storedCode);
        if (valid) {
            // 验证成功后删除验证码
            stringRedisTemplate.delete(codeKey);
            log.info("验证码验证成功: phone={}", phone);
        } else {
            log.warn("验证码验证失败: phone={}, inputCode={}", phone, code);
        }

        return valid;
    }

    @Override
    public ReturnResult<LoginResult> login(SmsLoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String phone = request.getPhone();
        String code = request.getCode();
        log.debug("短信验证码登录: phone={}", phone);

        // 验证验证码
        if (!verifyCode(phone, code, "login")) {
            return ReturnResult.fail("验证码错误或已过期");
        }

        // 根据手机号查询用户
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getSysUserPhone, phone)
                        .eq(SysUser::getSysUserStatus, 1)
        );

        if (user == null) {
            log.warn("手机号未注册: phone={}", phone);
            return ReturnResult.fail("该手机号未注册");
        }

        // 获取 AccessToken
        Map<String, Object> ext = ImmutableBuilder.<String, Object>builderOfMap()
                .put("loginType", "sms")
                .put("phone", phone)
                .put("address", RequestUtils.getIpAddress())
                .build();

        LoginAuthResult accessToken = AuthClientExecute.getInstance()
                .getAccessToken(String.valueOf(user.getSysUserId()), null, AuthType.SMS, ext);

        if (accessToken == null || StringUtils.isEmpty(accessToken.getToken())) {
            log.error("获取 Token 失败: phone={}", phone);
            return ReturnResult.fail("登录失败，请重试");
        }

        // 构建登录结果
        LoginResult loginResult = LoginResult.builder()
                .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();

        // 设置 Cookie
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);

        log.info("短信验证码登录成功: phone={}, userId={}", phone, user.getSysUserId());
        return ReturnResult.ok(loginResult);
    }
}
