package com.chua.starter.service.setting;

import com.chua.common.support.setting.GlobalSettingGroup;
import lombok.Data;

/**
 * 版本设置
 * @author CH
 * @since 2024/8/26
 */
@Data
@GlobalSettingGroup("config")
public class VersionSetting {

    /**
     * 版本
     */
    private String version;
}
