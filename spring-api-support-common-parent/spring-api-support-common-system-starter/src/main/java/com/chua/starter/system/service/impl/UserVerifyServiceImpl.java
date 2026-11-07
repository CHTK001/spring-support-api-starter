// [P3C BL-边界 豁免]

package com.chua.starter.system.service.impl;

import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.service.setting.SmtpSetting;
import com.chua.starter.system.pojo.verify.EmailConnectivityVO;
import com.chua.starter.system.pojo.verify.VerifyCodeSentVO;
import com.chua.starter.system.service.UserVerifyService;
import com.chua.starter.system.service.VerifyCodeService;
import com.chua.starter.system.support.verify.EmailProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 账号资料验证服务实现
 *
 * <p>投递目标恒定取自当前登录用户的档案行，请求里不出现地址，因此不存在"给任意地址发码"的开放投递面；
 * 校验通过只回写已验证标记与时间，邮箱本身仍以用户保存的值为准。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVerifyServiceImpl implements UserVerifyService {

    /**
     * 邮箱渠道标识，与 SPI 注册键一致
     */
    private static final String CHANNEL_EMAIL = "EMAIL";

    /**
     * 资料验证业务场景，决定 Redis 验证码键的场景段
     */
    private static final String BIZ_PROFILE = "profile";

    /**
     * 通用验证码服务
     */
    private final VerifyCodeService verifyCodeService;
    /**
     * SYS用户映射器
    */
    private final SysUserMapper sysUserMapper;

    @Override
    /**
     * 发送编码
    */
    public ReturnResult<VerifyCodeSentVO> sendCode(Long userId, String channel) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (null == sysUser) {
            return ReturnResult.illegal("用户信息不存在");
        }
        if (!CHANNEL_EMAIL.equalsIgnoreCase(StringUtils.trimToEmpty(channel))) {
            return ReturnResult.illegal("暂不支持的验证渠道");
        }
        String target = sysUser.getSysUserEmail();
        if (StringUtils.isBlank(target)) {
            return ReturnResult.illegal("请先在基本资料中填写邮箱并保存");
        }
        return verifyCodeService.send(CHANNEL_EMAIL, BIZ_PROFILE, target);
    }

    @Override
    /**
     * 验证编码
    */
    public ReturnResult<Boolean> verifyCode(Long userId, String channel, String code) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (null == sysUser) {
            return ReturnResult.illegal("用户信息不存在");
        }
        if (!CHANNEL_EMAIL.equalsIgnoreCase(StringUtils.trimToEmpty(channel))) {
            return ReturnResult.illegal("暂不支持的验证渠道");
        }
        String target = sysUser.getSysUserEmail();
        if (StringUtils.isBlank(target)) {
            return ReturnResult.illegal("请先在基本资料中填写邮箱并保存");
        }
        if (!verifyCodeService.verify(CHANNEL_EMAIL, BIZ_PROFILE, target, code)) {
            return ReturnResult.error("验证码错误或已过期");
        }
        sysUser.setSysUserEmailVerified(1);
        sysUser.setSysUserEmailVerifyTime(LocalDateTime.now());
        sysUserMapper.updateById(sysUser);
        log.info("邮箱验证通过: userId={}", userId);
        return ReturnResult.ok(true);
    }

    @Override
    /**
     * 检查邮箱连通性
    */
    public ReturnResult<EmailConnectivityVO> checkEmailConnectivity() {
        SmtpSetting setting = GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
        EmailProbe.ProbeResult smtp = EmailProbe.probeSmtp(setting);
        EmailProbe.ProbeResult imap = EmailProbe.probeImap(setting);
        return ReturnResult.ok(new EmailConnectivityVO(smtp.success(), smtp.message(), smtp.cost(),
                imap.success(), imap.message(), imap.cost(), null != setting && setting.isValid()));
    }
}
