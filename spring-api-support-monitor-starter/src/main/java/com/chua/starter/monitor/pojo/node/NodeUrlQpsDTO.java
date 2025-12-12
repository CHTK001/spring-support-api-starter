package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 节点 URL QPS 统计信息 DTO
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Schema(description = "节点 URL QPS 统计信息")
public class NodeUrlQpsDTO {

    /**
     * URL 路径
     */
    @Schema(description = "URL 路径")
    private String url;

    /**
     * HTTP 方法
     */
    @Schema(description = "HTTP 方法")
    private String method;

    /**
     * 当前周期请求总数
     */
    @Schema(description = "当前周期请求总数")
    private Long count;

    /**
     * 成功次数
     */
    @Schema(description = "成功次数")
    private Long successCount;

    /**
     * 失败次数
     */
    @Schema(description = "失败次数")
    private Long failCount;

    /**
     * 平均响应时间（毫秒）
     */
    @Schema(description = "平均响应时间（毫秒）")
    private Long avgDuration;

    /**
     * 最小响应时间（毫秒）
     */
    @Schema(description = "最小响应时间（毫秒）")
    private Long minDuration;

    /**
     * 最大响应时间（毫秒）
     */
    @Schema(description = "最大响应时间（毫秒）")
    private Long maxDuration;

    /**
     * QPS（每秒查询数）
     */
    @Schema(description = "QPS（每秒查询数）")
    private Double qps;

    /**
     * 状态码统计
     */
    @Schema(description = "状态码统计")
    private Map<String, Long> statusCodes;

    /**
     * 历史总请求数
     */
    @Schema(description = "历史总请求数")
    private Long historyTotalCount;

    /**
     * 历史成功次数
     */
    @Schema(description = "历史成功次数")
    private Long historySuccessCount;

    /**
     * 历史失败次数
     */
    @Schema(description = "历史失败次数")
    private Long historyFailCount;
}
