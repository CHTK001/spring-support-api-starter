package com.chua.starter.monitor.pojo.skywalking;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SkyWalking 链路追踪信息
 *
 * @author CH
 * @since 2024-12-18
 */
@Data
@ApiModel(description = "SkyWalking 链路追踪信息")
@Schema(description = "SkyWalking 链路追踪信息")
public class SkywalkingTrace {

    @ApiModelProperty("Trace ID")
    @Schema(description = "Trace ID")
    private String traceId;

    @ApiModelProperty("Span 列表")
    @Schema(description = "Span 列表")
    private List<Span> spans;

    /**
     * Trace 简要信息
     */
    @Data
    @ApiModel(description = "Trace 简要信息")
    @Schema(description = "Trace 简要信息")
    public static class Brief {
        @ApiModelProperty("Trace ID 列表")
        @Schema(description = "Trace ID 列表")
        private List<BasicTrace> traces;

        @ApiModelProperty("总数")
        @Schema(description = "总数")
        private Integer total;
    }

    /**
     * 基础 Trace 信息
     */
    @Data
    @ApiModel(description = "基础 Trace 信息")
    @Schema(description = "基础 Trace 信息")
    public static class BasicTrace {
        @ApiModelProperty("Segment ID")
        @Schema(description = "Segment ID")
        private String segmentId;

        @ApiModelProperty("端点名称列表")
        @Schema(description = "端点名称列表")
        private List<String> endpointNames;

        @ApiModelProperty("持续时间(毫秒)")
        @Schema(description = "持续时间(毫秒)")
        private Integer duration;

        @ApiModelProperty("开始时间")
        @Schema(description = "开始时间")
        private String start;

        @ApiModelProperty("是否错误")
        @Schema(description = "是否错误")
        private Boolean isError;

        @ApiModelProperty("Trace ID 列表")
        @Schema(description = "Trace ID 列表")
        private List<String> traceIds;
    }

    /**
     * Span 信息
     */
    @Data
    @ApiModel(description = "Span 信息")
    @Schema(description = "Span 信息")
    public static class Span {
        @ApiModelProperty("Trace ID")
        @Schema(description = "Trace ID")
        private String traceId;

        @ApiModelProperty("Segment ID")
        @Schema(description = "Segment ID")
        private String segmentId;

        @ApiModelProperty("Span ID")
        @Schema(description = "Span ID")
        private Integer spanId;

        @ApiModelProperty("父 Span ID")
        @Schema(description = "父 Span ID")
        private Integer parentSpanId;

        @ApiModelProperty("引用")
        @Schema(description = "引用")
        private List<Ref> refs;

        @ApiModelProperty("服务代码")
        @Schema(description = "服务代码")
        private String serviceCode;

        @ApiModelProperty("服务实例名称")
        @Schema(description = "服务实例名称")
        private String serviceInstanceName;

        @ApiModelProperty("开始时间")
        @Schema(description = "开始时间")
        private Long startTime;

        @ApiModelProperty("结束时间")
        @Schema(description = "结束时间")
        private Long endTime;

        @ApiModelProperty("端点名称")
        @Schema(description = "端点名称")
        private String endpointName;

        @ApiModelProperty("类型")
        @Schema(description = "类型")
        private String type;

        @ApiModelProperty("对端地址")
        @Schema(description = "对端地址")
        private String peer;

        @ApiModelProperty("组件")
        @Schema(description = "组件")
        private String component;

        @ApiModelProperty("是否错误")
        @Schema(description = "是否错误")
        private Boolean isError;

        @ApiModelProperty("层")
        @Schema(description = "层")
        private String layer;

        @ApiModelProperty("标签")
        @Schema(description = "标签")
        private List<KeyValue> tags;

        @ApiModelProperty("日志")
        @Schema(description = "日志")
        private List<LogEntity> logs;
    }

    /**
     * 引用信息
     */
    @Data
    @ApiModel(description = "引用信息")
    @Schema(description = "引用信息")
    public static class Ref {
        @ApiModelProperty("Trace ID")
        @Schema(description = "Trace ID")
        private String traceId;

        @ApiModelProperty("父 Segment ID")
        @Schema(description = "父 Segment ID")
        private String parentSegmentId;

        @ApiModelProperty("父 Span ID")
        @Schema(description = "父 Span ID")
        private Integer parentSpanId;

        @ApiModelProperty("类型")
        @Schema(description = "类型")
        private String type;
    }

    /**
     * 键值对
     */
    @Data
    @ApiModel(description = "键值对")
    @Schema(description = "键值对")
    public static class KeyValue {
        @ApiModelProperty("键")
        @Schema(description = "键")
        private String key;

        @ApiModelProperty("值")
        @Schema(description = "值")
        private String value;
    }

    /**
     * 日志实体
     */
    @Data
    @ApiModel(description = "日志实体")
    @Schema(description = "日志实体")
    public static class LogEntity {
        @ApiModelProperty("时间")
        @Schema(description = "时间")
        private Long time;

        @ApiModelProperty("数据")
        @Schema(description = "数据")
        private List<KeyValue> data;
    }
}
