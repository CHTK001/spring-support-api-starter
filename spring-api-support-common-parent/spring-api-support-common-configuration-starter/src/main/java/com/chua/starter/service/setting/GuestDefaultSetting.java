package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录页默认配置（default 分组）
 * <p>
 * 所有字段对应 sys_setting 表的 sys_setting_name，通过 GlobalSettingFactory 反射映射。
 * 更新 sys_setting 后，工厂会自动将值写入此 Bean，消费者通过
 * {@code GlobalSettingFactory.getInstance().get("default", GuestDefaultSetting.class)} 读取。
 *
 * @author CH
 * @since 2024/8/13
 */
@Data
@GlobalSettingGroup("default")
public class GuestDefaultSetting implements Upgrade<GuestDefaultSetting> {

    /**
     * 是否开启图形验证码（对应 sys_setting_name = "CheckCodeOpen"）
     */
    private boolean checkCodeOpen;

    /**
     * 是否开启 OTP 动态口令（对应 sys_setting_name = "CheckTotpCodeOpen"）
     */
    private boolean checkTotpCodeOpen;

    /**
     * 是否开启滑块验证（对应 sys_setting_name = "SlidingBlockOpen"）
     */
    private boolean slidingBlockOpen;

    /**
     * 系统标题（对应 sys_setting_name = "SystemName"）
     */
    private String systemName;

    /**
     * 是否开启短信登录（对应 sys_setting_name = "OpenSmsLogin"）
     */
    private boolean openSmsLogin;

    /**
     * 品牌名称（对应 sys_setting_name = "BrandName"）
     */
    private String brandName;

    /**
     * 欢迎语（对应 sys_setting_name = "Welcome"）
     */
    private String welcome;

    /**
     * 系统描述（对应 sys_setting_name = "Description"）
     */
    private String description;

    /**
     * 品牌 Logo 地址（对应 sys_setting_name = "LogoUrl"）
     */
    private String logoUrl;

    /**
     * 登录页主视觉图片（对应 sys_setting_name = "ImageUrl"）
     */
    private String imageUrl;

    /**
     * 默认用户头像（对应 sys_setting_name = "AvatarUrl"）
     */
    private String avatarUrl;

    /**
     * 备案号（对应 sys_setting_name = "BeiAnHao"）
     */
    private String beiAnHao;

    /**
     * 登录页功能特性标签（JSON 字符串，对应 sys_setting_name = "FeatureTags"）
     * 格式示例：[{"icon":"DataAnalysis","text":"高效管理"}]
     */
    private String featureTags;

    @Override
    public void upgrade(GuestDefaultSetting guestDefaultSetting) {
        this.checkCodeOpen = guestDefaultSetting.checkCodeOpen;
        this.checkTotpCodeOpen = guestDefaultSetting.checkTotpCodeOpen;
        this.slidingBlockOpen = guestDefaultSetting.slidingBlockOpen;
        this.systemName = guestDefaultSetting.systemName;
        this.openSmsLogin = guestDefaultSetting.openSmsLogin;
        this.brandName = guestDefaultSetting.brandName;
        this.welcome = guestDefaultSetting.welcome;
        this.description = guestDefaultSetting.description;
        this.logoUrl = guestDefaultSetting.logoUrl;
        this.imageUrl = guestDefaultSetting.imageUrl;
        this.avatarUrl = guestDefaultSetting.avatarUrl;
        this.beiAnHao = guestDefaultSetting.beiAnHao;
        this.featureTags = guestDefaultSetting.featureTags;
    }
}
