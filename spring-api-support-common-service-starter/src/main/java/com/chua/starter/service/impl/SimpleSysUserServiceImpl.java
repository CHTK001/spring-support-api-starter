package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.constant.Projects;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.qr.CodePointStyle;
import com.chua.common.support.lang.qr.QrCode;
import com.chua.common.support.lang.qr.QrCodeBuilder;
import com.chua.common.support.otp.TotpGenerator;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.redis.support.constant.RedisConstant;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import com.google.zxing.qrcode.encoder.QRCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * @author CH
 * @since 2024/8/7
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleSysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SimpleSysUserService {

    final StringRedisTemplate stringRedisTemplate;


    @Override
    public ReturnResult<Boolean> bind(Integer sysUserId) {
        SysUser sysUser = baseMapper.selectById(sysUserId);
        if (null == sysUser) {
            return ReturnResult.illegal("当前账号信息异常, 请重新登录或者刷新后重试!");
        }

        String seed = Projects.getMainBoardSerialNumber() + IdUtils.createTid() + sysUser.getSysUserUsername() + System.currentTimeMillis();
        String sha512Hex = DigestUtils.sha512Hex(seed);
        stringRedisTemplate.opsForHash().put(RedisConstant.TOTP_PREFIX, sysUser.getSysUserUsername(), sha512Hex);
        sysUser.setSysUserTotpCode(sha512Hex);
        return null;
    }

    @Override
    public ReturnResult<Boolean> unbind(Integer sysUserId) {
        SysUser sysUser = baseMapper.selectById(sysUserId);
        if (null == sysUser) {
            return ReturnResult.illegal("当前账号信息异常, 请重新登录或者刷新后重试!");
        }
        stringRedisTemplate.opsForHash().delete(RedisConstant.TOTP_PREFIX, sysUser.getSysUserUsername());
        sysUser.setSysUserTotpCode(EMPTY);
        return null;
    }

    @Override
    public ReturnResult<byte[]> generate(Integer sysUserId) {
        ReturnResult<String> stringReturnResult = generateUri(sysUserId);
        if (!stringReturnResult.isOk()) {
            return ReturnResult.of(stringReturnResult.getCode(), null, stringReturnResult.getMsg());
        }
        QrCode qrCode = QrCodeBuilder.builder()
                .pointStyle(CodePointStyle.RECTANGLE)
                .build();
        try (ByteArrayOutputStream outputStream1 = (ByteArrayOutputStream) qrCode.create(stringReturnResult.getData())) {
            return ReturnResult.ok(outputStream1.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReturnResult<Boolean> verify(String code, @NotNull String username) {
        Object key = stringRedisTemplate.opsForHash().get(RedisConstant.TOTP_PREFIX, username);
        if (null == key) {
            return Result.failed("请先绑定fa验证码");
        }
        String secretString = key.toString();
        byte[] bytes = secretString.getBytes(StandardCharsets.UTF_8);

        TotpGenerator otpGenerator = ServiceProvider.of(TotpGenerator.class).getNewExtension("default", bytes, ZoneOffset.UTC);
        return ReturnResult.ok(otpGenerator.verify(code));
    }

    @Override
    public ReturnResult<String> generateUri(Integer sysUserId) {
        SysUser sysUser = baseMapper.selectById(sysUserId);
        if (null == sysUser) {
            return ReturnResult.illegal("当前账号信息异常, 请重新登录或者刷新后重试!");
        }
        Object key = stringRedisTemplate.opsForHash().get(RedisConstant.TOTP_PREFIX, sysUser.getSysUserUsername());
        if (null == key) {
            return ReturnResult.illegal("请先绑定fa验证码");
        }
        String secretString = key.toString();
        byte[] bytes = secretString.getBytes(StandardCharsets.UTF_8);

        TotpGenerator otpGenerator = ServiceProvider.of(TotpGenerator.class).getNewExtension("default", bytes, ZoneOffset.UTC);

        try {
            return ReturnResult.ok(otpGenerator.getUri(sysUser.getSysUserUsername(), "totp").toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
