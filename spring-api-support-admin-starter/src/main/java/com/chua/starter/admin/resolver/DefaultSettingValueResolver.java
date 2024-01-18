package com.chua.starter.admin.resolver;

import com.chua.common.support.annotations.SpiDefault;
import com.chua.starter.admin.entity.SysSetting;

import java.util.List;

/**
 * 数据解析器
 * @author CH
 * @version  1.0.0
 * @since 2024/01/18
 */
@SpiDefault
public class DefaultSettingValueResolver implements SettingValueResolver{
    @Override
    public void resolve(List<SysSetting> result) {

    }
}
