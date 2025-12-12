package com.chua.starter.monitor.pojo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent 链路追踪 DTO
 * <p>
 * 用于存储和查询 Agent 上报的链路追踪数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 链路追踪信息")
public class AgentTraceDTO {

    /**
     * 链路ID
     */
    @Schema(description = "链路ID")
    private String linkId;

    /**
     * Span ID
     */
    @Schema(description = "Span ID")
    private String spanId;

    /**
     * 父 Span ID
     */
    @Schema(description = "父 Span ID")
    private String parentSpanId;

    /**
     * 线程名称
     */
    @Schema(description = "线程名称")
    private String threadName;

    /**
     * 类名
     */
    @Schema(description = "类名")
    private String typeName;

    /**
     * 方法名
     */
    @Schema(description = "方法名")
    private String method;

    /**
     * 分类
     */
    @Schema(description = "分类: HTTP, JDBC, REDIS 等")
    private String category;

    /**
     * 协议
     */
    @Schema(description = "协议")
    private String protocol;

    /**
     * 地址
     */
    @Schema(description = "地址")
    private String address;

    /**
     * 数据库
     */
    @Schema(description = "数据库")
    private String database;

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private String statusCode;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String error;

    /**
     * 进入时间（毫秒时间戳）
     */
    @Schema(description = "进入时间（毫秒时间戳）")
    private Long enterTime;

    /**
     * 退出时间（毫秒时间戳）
     */
    @Schema(description = "退出时间（毫秒时间戳）")
    private Long exitTime;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private Long costTime;

    /**
     * 来源 IP
     */
    @Schema(description = "来源 IP")
    private String sourceIp;

    /**
     * 来源端口
     */
    @Schema(description = "来源端口")
    private Integer sourcePort;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String applicationName;

    /**
     * 请求头
     */
    @Schema(description = "请求头")
    private List<String> headers;

    /**
     * 参数
     */
    @Schema(description = "参数")
    private List<String> params;

    /**
     * 子 Span 列表
     */
    @Schema(description = "子 Span 列表")
    private List<AgentTraceDTO> children;

    /**
     * 服务实例列表
     */
    @Schema(description = "服务实例列表")
    private List<AgentServiceInstanceDTO> instances;
}
