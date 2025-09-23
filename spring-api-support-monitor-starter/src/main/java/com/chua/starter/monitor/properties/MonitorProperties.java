package com.chua.starter.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 监控配置属性
 *
 * @author CH
 * @since 2025/09/22
 */
@Data
@ConfigurationProperties(prefix = MonitorProperties.PRE)
public class MonitorProperties {

    public static final String PRE = "plugin.monitor.server";

    /**
     * 是否启用后端定时检测推送任务
     */
    private boolean enableScheduledTasks = true;
    /**
     * 是否启用文件合并任务
     */
    private boolean enableFileMergeTasks = true;

}