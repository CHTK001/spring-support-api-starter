package com.chua.starter.monitor.configuration;

import java.util.List;

public class RuntimeGlobalSettingProvider {

    public String group() {
        return "runtime";
    }

    public String groupName() {
        return "运行时配置";
    }

    public List<Object> getItems() {
        return List.of();
    }
}