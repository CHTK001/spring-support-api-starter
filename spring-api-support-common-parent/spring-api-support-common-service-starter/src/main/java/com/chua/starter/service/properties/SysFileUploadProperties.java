package com.chua.starter.service.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本地文件上传配置
 *
 * @author CH
 * @since 4.0.0
 */
@Data
@ConfigurationProperties(prefix = SysFileUploadProperties.PRE, ignoreInvalidFields = true)
public class SysFileUploadProperties {

    /**
     * PRE
    */
    public static final String PRE = "plugin.filesystem.upload";

    /**
     * 本地存储根目录（相对进程工作目录或绝对路径）
     */
    private String rootPath = "./oss/system";

    /**
     * 单文件大小上限（MB）
     */
    private long maxSizeMb = 10;

    /**
     * 允许上传的扩展名（小写，不含点）
     * 仅开放图片类型：非白名单类型一律拒绝，避免存储并回显可执行/可脚本内容
     */
    private List<String> allowedExtensions = new ArrayList<>(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp"));
}
