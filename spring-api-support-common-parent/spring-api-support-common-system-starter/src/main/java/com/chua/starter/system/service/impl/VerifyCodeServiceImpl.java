// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.pojo.verify.VerifyCodeSentVO;
import com.chua.starter.system.service.VerifyCodeService;
import com.chua.starter.system.support.verify.VerifyCodeChannel;
import com.chua.starter.system.support.verify.VerifyCodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 通用验证码服务实现
 *
 * <p>Redis 键形如 {@code verify:code:EMAIL:profile:a@b.com}，值为验证码，TTL 即有效期；
 * 校验只做取值比对，命中即删除，避免同一码复用。连错次数独立计数，防六位数字被穷举。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyCodeServiceImpl implements VerifyCodeService {

    /**
     * 验证码键前缀
     */
    private static final String CODE_KEY_PREFIX = "verify:code:";

    /**
     * 重发间隔键前缀
     */
    private static final String LIMIT_KEY_PREFIX = "verify:limit:";

    /**
     * 校验失败次数键前缀
     */
    private static final String FAIL_KEY_PREFIX = "verify:fail:";

    /**
     * 验证码有效期(分钟)
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 重发间隔(秒)
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    /**
     * 同一验证码允许的连错次数
     */
    private static final int MAX_FAIL_TIMES = 5;

    /**
     * 随机码发生器
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 字符串Redis模板
     */
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    /**
     * 配置好的
    */
    public boolean configured(String channel) {
        VerifyCodeChannel verifyCodeChannel = resolve(channel);
        return null != verifyCodeChannel && verifyCodeChannel.configured();
    }

    @Override
    /**
     * 发送编码
    */
    public ReturnResult<VerifyCodeSentVO> send(String channel, String bizType, String target) {
        if (StringUtils.isBlank(target)) {
            return ReturnResult.illegal("验证目标不能为空");
        }
        VerifyCodeChannel verifyCodeChannel = resolve(channel);
        if (null == verifyCodeChannel) {
            return ReturnResult.error("验证码渠道未安装: " + channel);
        }
        if (!verifyCodeChannel.configured()) {
            return ReturnResult.error("服务器未配置" + channel + "通道，请联系管理员");
        }

        String normalized = target.trim();
        String limitKey = limitKey(channel, bizType, normalized);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(limitKey))) {
            Long remain = stringRedisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
            return ReturnResult.error("发送过于频繁，请 " + (null == remain || remain < 0 ? SEND_INTERVAL_SECONDS : remain) + " 秒后重试");
        }

        String code = randomCode();
        try {
            verifyCodeChannel.send(new VerifyCodeMessage(normalized, bizType, code, CODE_EXPIRE_MINUTES));
        } catch (Exception e) {
            log.error("验证码下发失败: channel={}, bizType={}", channel, bizType, e);
            return ReturnResult.error("验证码发送失败: " + e.getMessage());
        }

        stringRedisTemplate.opsForValue().set(codeKey(channel, bizType, normalized), code,
                CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        stringRedisTemplate.delete(failKey(channel, bizType, normalized));
        stringRedisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        return ReturnResult.ok(new VerifyCodeSentVO(mask(normalized), CODE_EXPIRE_MINUTES * 60, SEND_INTERVAL_SECONDS));
    }

    @Override
    /**
     * 验证编码
    */
    public boolean verify(String channel, String bizType, String target, String code) {
        if (StringUtils.isBlank(target) || StringUtils.isBlank(code)) {
            return false;
        }
        String normalized = target.trim();
        String codeKey = codeKey(channel, bizType, normalized);
        String stored = stringRedisTemplate.opsForValue().get(codeKey);
        if (StringUtils.isEmpty(stored)) {
            log.warn("验证码不存在或已过期: channel={}, bizType={}", channel, bizType);
            return false;
        }
        if (!stored.equalsIgnoreCase(code.trim())) {
            accumulateFailure(channel, bizType, normalized);
            log.warn("验证码不匹配: channel={}, bizType={}", channel, bizType);
            return false;
        }
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(failKey(channel, bizType, normalized));
        return true;
    }

    /**
     * 累计校验失败次数，超限则作废验证码
     *
     * @param channel 渠道
     * @param bizType 业务场景
     * @param target  目标
     */
    private void accumulateFailure(String channel, String bizType, String target) {
        String failKey = failKey(channel, bizType, target);
        Long times = stringRedisTemplate.opsForValue().increment(failKey);
        if (null != times && 1L == times) {
            stringRedisTemplate.expire(failKey, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        if (null != times && times >= MAX_FAIL_TIMES) {
            stringRedisTemplate.delete(codeKey(channel, bizType, target));
            stringRedisTemplate.delete(failKey);
        }
    }

    /**
     * 按标识取渠道实现
     *
     * @param channel 渠道标识，大小写不敏感
     * @return 渠道实例，未安装时为空
     */
    private VerifyCodeChannel resolve(String channel) {
        if (StringUtils.isBlank(channel)) {
            return null;
        }
        String key = channel.trim().toUpperCase();
        ServiceProvider<VerifyCodeChannel> serviceProvider = ServiceProvider.of(VerifyCodeChannel.class);
        if (!serviceProvider.getExtensions().contains(key)) {
            return null;
        }
        return serviceProvider.getExtension(key);
    }

    /**
     * 生成数字验证码
     *
     * @return 验证码
     */
    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    /**
     * 验证码键
     *
     * @param channel 渠道
     * @param bizType 业务场景
     * @param target  目标
     * @return Redis 键
     */
    private String codeKey(String channel, String bizType, String target) {
        return CODE_KEY_PREFIX + channel.trim().toUpperCase() + ":" + bizType + ":" + target;
    }

    /**
     * 重发间隔键
     *
     * @param channel 渠道
     * @param bizType 业务场景
     * @param target  目标
     * @return Redis 键
     */
    private String limitKey(String channel, String bizType, String target) {
        return LIMIT_KEY_PREFIX + channel.trim().toUpperCase() + ":" + bizType + ":" + target;
    }

    /**
     * 失败次数键
     *
     * @param channel 渠道
     * @param bizType 业务场景
     * @param target  目标
     * @return Redis 键
     */
    private String failKey(String channel, String bizType, String target) {
        return FAIL_KEY_PREFIX + channel.trim().toUpperCase() + ":" + bizType + ":" + target;
    }

    /**
     * 投递目标脱敏：邮箱保留首字符与域名，手机号保留后四位
     *
     * @param target 原目标
     * @return 脱敏结果
     */
    private String mask(String target) {
        int at = target.indexOf('@');
        if (at > 0) {
            String name = target.substring(0, at);
            String prefix = name.length() <= 1 ? name : name.substring(0, 1);
            return prefix + "***" + target.substring(at);
        }
        if (target.length() <= 4) {
            return "****";
        }
        return "****" + target.substring(target.length() - 4);
    }
}