package com.chua.starter.monitor.configuration;

import java.util.List;

/**
 * 运行时全局设置提供者，向全局设置中心提供「运行时配置」分组及其配置项。
 *
 * @since 4.0.0.42
 * @author CH
 */
public class RuntimeGlobalSettingProvider {

    /**
     * 配置分组编码，用于标识「运行时配置」分组
     */
    private static final String GROUP_CODE = "runtime";

    /**
     * 获取配置分组编码。
     *
     * @return 分组编码
     */
    public String group() {
        return GROUP_CODE;
    }

    /**
     * 获取配置分组的中文名称。
     *
     * @return 分组名称
     */
    public String groupName() {
        return "运行时配置";
    }

    /**
     * 获取该分组下的全部配置项。
     *
     * @return 配置项列表，当前为空
     */
    public List<Object> getItems() {
        return List.of();
    }
}