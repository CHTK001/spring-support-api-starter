package com.chua.starter.system.service.impl;

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
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.service.model.SysUser;
import com.chua.starter.system.service.SimpleSysUserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;


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
            return ReturnResult.illegal("褰撳墠璐﹀彿淇℃伅寮傚父, 璇烽噸鏂扮櫥褰曟垨鑰呭埛鏂板悗閲嶈瘯!");
        }

        String seed = Projects.getMainBoardSerialNumber() + IdUtils.createTid() + sysUser.getSysUserUsername() + System.currentTimeMillis();
        String sha512Hex = DigestUtils.sha512Hex(seed);
        stringRedisTemplate.opsForHash().put(RedisConstant.TOTP_PREFIX, sysUser.getSysUserUsername(), sha512Hex);
        sysUser.setSysUserTotpCode(sha512Hex);
        baseMapper.updateById(sysUser);
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Boolean> unbind(Integer sysUserId) {
        SysUser sysUser = baseMapper.selectById(sysUserId);
        if (null == sysUser) {
            return ReturnResult.illegal("褰撳墠璐﹀彿淇℃伅寮傚父, 璇烽噸鏂扮櫥褰曟垨鑰呭埛鏂板悗閲嶈瘯!");
        }
        stringRedisTemplate.opsForHash().delete(RedisConstant.TOTP_PREFIX, sysUser.getSysUserUsername());
        sysUser.setSysUserTotpCode("");
        baseMapper.updateById(sysUser);
        return ReturnResult.ok(true);
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
            return Result.failed("璇峰厛缁戝畾fa楠岃瘉鐮?);
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
            return ReturnResult.illegal("褰撳墠璐﹀彿淇℃伅寮傚父, 璇烽噸鏂扮櫥褰曟垨鑰呭埛鏂板悗閲嶈瘯!");
        }
        Object key = stringRedisTemplate.opsForHash().get(RedisConstant.TOTP_PREFIX, sysUser.getSysUserUsername());
        if (null == key) {
            return ReturnResult.illegal("璇峰厛缁戝畾fa楠岃瘉鐮?);
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
