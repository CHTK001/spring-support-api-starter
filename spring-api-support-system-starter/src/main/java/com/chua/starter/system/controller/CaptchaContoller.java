package com.chua.starter.system.controller;

import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.constant.Constants;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.starter.common.support.constant.CaptchaTypeEnum;
import com.chua.starter.common.support.properties.CaptchaProperties;
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
 * 校验码
 *
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "校验码")
@Tag(name = "校验码")
@Ignore
@RequestMapping("/v2/captcha")
@SuppressWarnings("ALL")
public class CaptchaContoller {

    @Autowired
    private CaptchaProperties captchaProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    public Captcha producer() {
        return createCaptcha(captchaProperties.getType(), captchaProperties.getLength());
    }

    public Captcha producer(CaptchaTypeEnum typeEnum) {
        return createCaptcha(typeEnum, captchaProperties.getLength());
    }

    public Captcha producer(CaptchaTypeEnum typeEnum, int length) {
        return createCaptcha(typeEnum, length);
    }

    private Captcha createCaptcha(CaptchaTypeEnum type, int length) {
        Captcha captcha;
        int width = captchaProperties.getWidth();
        int height = captchaProperties.getHeight();
        String fontName = captchaProperties.getFontName();

        switch (type) {
            case ARITHMETIC:
                captcha = new ArithmeticCaptcha(width, height);
                //固定设置为两位，图片为算数运算表达式
                captcha.setLen(length);
                break;
            case CHINESE:
                captcha = new ChineseCaptcha(width, height);
                captcha.setLen(length);
                break;
            case CHINESE_GIF:
                captcha = new ChineseGifCaptcha(width, height);
                captcha.setLen(length);
                break;
            case GIF:
                captcha = new GifCaptcha(width, height);
                captcha.setLen(length);
                break;
            case SPEC:
                captcha = new SpecCaptcha(width, height);
                captcha.setCharType(Captcha.TYPE_DEFAULT);
                captcha.setLen(length);
                break;
            default:
                throw new RuntimeException("验证码配置信息错误！正确配置查看 CaptchaTypeEnum ");
        }
        try {
            //captcha.setFont(Captcha.FONT_1, captchaProperties.getFontSize());
        } catch (Exception ignored) {
        }

        return captcha;
    }


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
     */
    @ApiOperation("获取验证码")
    @Operation(summary = "获取验证码")
    @GetMapping
    public CaptchaResult captchaBase64(HttpServletRequest request, HttpServletResponse response) {
        Captcha captcha = createCaptcha();
        String captchaText = captcha.text();
        log.info("当前校验码: {}", captchaText);
        String captchaBase64 = captcha.toBase64();
        try {
            HttpSession session = request.getSession();
            session.setAttribute(Constants.CAPTCHA_SESSION_KEY, captchaText);
            String ulid = IdUtils.createUlid();
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(ulid, captchaText, 30, TimeUnit.SECONDS);
            return CaptchaResult.builder()
                    .verifyCodeBase64(captchaBase64)
                    .verifyCodeUlid(ulid)
                    .verifyCodeKey(captchaText)
                    .build();
        } catch (Exception e) {
            throw e;
        }
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

        private String verifyCodeKey;

        private String verifyCodeBase64;

        private String verifyCodeUlid;

    }
}
