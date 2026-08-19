package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
import lombok.Data;

/**
 * 用户协议配置（userAgreement 分组）
 * <p>
 * 字段对应 sys_setting 表的 sys_setting_name，通过 GlobalSettingFactory 反射映射。
 * 更新 sys_setting 后，工厂自动写入此 Bean。
 *
 * @since 2026/4
 * @author CH
 */
@Data
@GlobalSettingGroup("userAgreement")
public class UserAgreementSetting implements Upgrade<UserAgreementSetting> {

    /**
     * 协议版本号（对应 sys_setting_name = "version"）
     */
    private String version;

    /**
     * 协议更新时间（对应 sys_setting_name = "updatedAt"）
     */
    private String updatedAt;

    @Override
    public void upgrade(UserAgreementSetting setting) {
        this.version = setting.version;
        this.updatedAt = setting.updatedAt;
    }
}
