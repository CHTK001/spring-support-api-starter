package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 运行脚本响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "运行脚本响应")
public class RunScriptResponse {

    /**
     * 执行结果，键为主机地址，值为执行结果
     */
    @Schema(description = "执行结果，键为主机地址，值为执行结果")
    private Map<String, String> results;

    /**
     * 是否全部成功
     */
    @Schema(description = "是否全部成功")
    private Boolean allSuccess;
} 