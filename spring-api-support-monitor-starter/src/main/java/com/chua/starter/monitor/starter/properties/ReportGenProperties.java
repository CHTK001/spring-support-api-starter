package com.chua.starter.monitor.starter.properties;

import com.chua.common.support.utils.FileUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * gen配置
 * @author CH
 * @since 2024/9/26
 */
@Data
@ConfigurationProperties(prefix = ReportGenProperties.PRE, ignoreInvalidFields = true)
public class ReportGenProperties {

    public static final String PRE = "plugin.report.server.gen";
    /**
     * 生成包路径
     */
    private String packageName;
    /**
     * 作者
     */
    private String author;

    /**
     * 临时路径
     */
    private String tempPath = "gen";
    /**
     * 模板路径
     */

    private String templatePath;
}
