package com.chua.starter.service.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
* @since 2024/12/4
* @author CH
 */
@Data
@ConfigurationProperties(prefix = FileSystemProperties.PRE, ignoreInvalidFields = true)
public class FileSystemProperties {

    /** PRE */
    public static final String PRE = "plugin.filesystem";

    /**
    * 合并任务线程池大小
    * 默认10
     */
    private int mergeTaskPoolSize = 10;


    /**
    * 临时文件路径
     */
    private String tempPath = "./temp/filesystem";
}
