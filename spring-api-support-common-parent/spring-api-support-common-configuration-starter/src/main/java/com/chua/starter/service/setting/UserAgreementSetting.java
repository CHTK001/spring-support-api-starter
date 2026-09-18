package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
import lombok.Data;

/**
 * 用户协议配置（用户agreement 分组）
 * <p>
 * 字段对应 sys_setting 表的 sys_setting_名称，通过 全局setting工厂 反射映射。
 * 更新 sys_setting 后，工厂自动写入此 Bean。
 *
 * @since 2026/4
 * @author CH
 */
@Data
@GlobalSettingGroup("userAgreement")
public class UserAgreementSetting implements Upgrade<UserAgreementSetting> {

    /**
     * 协议版本号（对应 sys_setting_名称 = "版本"）
     */
    private String version;

    /**
     * 协议更新时间（对应 sys_setting_名称 = "更新at"）
     */
    private String updatedAt;

    @Override
    /** Upgrade */
    public void upgrade(UserAgreementSetting setting) {
        this.version = setting.version;
        this.updatedAt = setting.updatedAt;
    }
}
