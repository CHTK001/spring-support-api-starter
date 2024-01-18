package com.chua.starter.admin.resolver;

import com.chua.starter.admin.entity.SysSetting;

import java.util.List;

/**
 * 数据解析器
 * @author Administrator
 * @date 2024/01/18
 */
public interface SettingValueResolver {

    /**
     * 处理
     * @param result 结果
     */
    void resolve(List<SysSetting> result);
}
