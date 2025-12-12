package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 脚本执行记录查询请求
 *
 * @author CH
 * @since 2024-12-12
 */
@Schema(description = "脚本执行记录查询请求")
@Data
public class ScriptExecuteRecordQueryRequest {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    /**
     * 脚本ID
     */
    @Schema(description = "脚本ID", example = "1")
    private Integer scriptId;

    /**
     * 脚本名称（模糊搜索）
     */
    @Schema(description = "脚本名称", example = "监控脚本")
    private String scriptName;

    /**
     * 执行方式
     */
    @Schema(description = "执行方式: SSH, NODE", example = "SSH")
    private String executeMethod;

    /**
     * 执行结果
     */
    @Schema(description = "执行结果: SUCCESS, FAILED, RUNNING, TIMEOUT", example = "SUCCESS")
    private String executeResult;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2024-01-01")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2024-12-31")
    private String endTime;
}
