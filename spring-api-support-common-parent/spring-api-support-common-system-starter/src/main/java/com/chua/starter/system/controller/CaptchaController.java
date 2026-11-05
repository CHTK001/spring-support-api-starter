// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.starter.common.support.constant.CaptchaTypeEnum;
import com.chua.starter.common.support.properties.CaptchaProperties;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 图形校验码接口，提供滑动/点选/算术等多种验证码生成与校验能力。
 *
 * @author CH
 * @since 4.0.0.50
 */
@RestController
@Slf4j
@Api(tags = "校验码")
@Tag(name = "校验码")
@Ignore
@RequestMapping("/v2/captcha")
public class CaptchaController {

    @Autowired
    /**
     * Captcha属性
    */
    private CaptchaProperties captchaProperties;

    @Autowired
    private RedisTemplate<String, String> redisTemplate; // redisTemplate

    /**
     * Producer
     *
     * @return producer的结果
     */
    public Captcha producer() {
        return createCaptcha(captchaProperties.getType(), captchaProperties.getLength());
    }

    /**
     * Producer
     *
     * @param typeEnum 类型enum
     * @return producer的结果
     */
    public Captcha producer(CaptchaTypeEnum typeEnum) {
        return createCaptcha(typeEnum, captchaProperties.getLength());
    }

    /**
     * Producer
     *
     * @param typeEnum 类型enum
     * @param length 长度
     * @return producer的结果
     */
    public Captcha producer(CaptchaTypeEnum typeEnum, int length) {
        return createCaptcha(typeEnum, length);
    }

    /**
     * 创建Captcha
     *
     * @param type 类型
     * @param length 长度
     * @return 创建captcha的结果
     */
    private Captcha createCaptcha(CaptchaTypeEnum type, int length) {
        Captcha captcha;
        int width = captchaProperties.getWidth();
        int height = captchaProperties.getHeight();
        String fontName = captchaProperties.getFontName();

        switch (type) {
            case CaptchaTypeEnum.ARITHMETIC:
                captcha = new ArithmeticCaptcha(width, height);
                // 固定设置为两位，图片为算数运算表达式
                captcha.setLen(length);
                break;
            case CaptchaTypeEnum.CHINESE:
                captcha = new ChineseCaptcha(width, height);
                captcha.setLen(length);
                break;
            case CaptchaTypeEnum.CHINESE_GIF:
                captcha = new ChineseGifCaptcha(width, height);
                captcha.setLen(length);
                break;
            case CaptchaTypeEnum.GIF:
                captcha = new GifCaptcha(width, height);
                captcha.setLen(length);
                break;
            case CaptchaTypeEnum.SPEC:
                captcha = new SpecCaptcha(width, height);
                captcha.setCharType(Captcha.TYPE_DEFAULT);
                captcha.setLen(length);
                break;
            default:
                throw new RuntimeException("验证码配置信息错误！正确配置查看 CaptchaTypeEnum");
        }
        return captcha;
    }


    /**
     * 创建Captcha
     *
     * @return 创建captcha的结果
     */
    public Captcha createCaptcha() {
        int randomInt = RandomUtils.randomInt(2, 3);
        CaptchaTypeEnum typeEnum = captchaProperties.getType();
        CaptchaTypeEnum captchaTypeEnum = typeEnum;
        if(typeEnum == CaptchaTypeEnum.RANDOM) {
            typeEnum = RandomUtils.randomEnum(CaptchaTypeEnum.class);
        }
        if (captchaTypeEnum == CaptchaTypeEnum.CHINESE || captchaTypeEnum == CaptchaTypeEnum.CHINESE_GIF) {
            randomInt = RandomUtils.randomInt(4, 5);
        } else if (captchaTypeEnum != CaptchaTypeEnum.ARITHMETIC) {
            randomInt = RandomUtils.randomInt(6, 7);
        }
        return producer(captchaTypeEnum, randomInt);
    }

    /**
     * 校验码
     *
     * @param request 请求
     * @param response 响应
     * @return captchaBase64的结果
     */
    @ApiOperation("获取验证码")
    @Operation(summary = "获取验证码")
    @GetMapping
    @TokenForIgnore
    public CaptchaResult captchaBase64(HttpServletRequest request, HttpServletResponse response) {
        Captcha captcha = createCaptcha();
        String captchaText = captcha.text();
        log.debug("验证码已生成");
        String captchaBase64 = captcha.toBase64();
        HttpSession session = request.getSession();
        session.setAttribute(AuthConstant.CAPTCHA_SESSION_KEY, captchaText);
        String ulid = IdUtils.createSimpleUuid();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(ulid, captchaText, 30, TimeUnit.SECONDS);
        return CaptchaResult.builder()
                .verifyCodeBase64(captchaBase64)
                .verifyCodeUlid(ulid)
                .build();
    }


    /**
     * 验证码响应对象
     *
     * @author haoxr
     * @since 2023/03/24
     */
    @Builder
    @Data
    public static class CaptchaResult {

        /**
         * 验证码图片基础64编码
         */
        private String verifyCodeBase64;

        /**
         * 验证码唯一标识，用于后端校验
         */
        private String verifyCodeUlid;

    }}