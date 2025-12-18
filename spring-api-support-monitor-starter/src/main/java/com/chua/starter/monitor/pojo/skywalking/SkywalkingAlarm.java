package com.chua.starter.monitor.pojo.skywalking;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SkyWalking 告警信息
 *
 * @author CH
 * @since 2024-12-18
 */
@Data
@ApiModel(description = "SkyWalking 告警信息")
@Schema(description = "SkyWalking 告警信息")
public class SkywalkingAlarm {

    @ApiModelProperty("告警消息列表")
    @Schema(description = "告警消息列表")
    private List<AlarmMessage> msgs;

    @ApiModelProperty("总数")
    @Schema(description = "总数")
    private Integer total;

    /**
     * 告警消息
     */
    @Data
    @ApiModel(description = "告警消息")
    @Schema(description = "告警消息")
    public static class AlarmMessage {
        @ApiModelProperty("告警ID")
        @Schema(description = "告警ID")
        private String id;

        @ApiModelProperty("告警消息内容")
        @Schema(description = "告警消息内容")
        private String message;

        @ApiModelProperty("开始时间")
        @Schema(description = "开始时间")
        private Long startTime;

        @ApiModelProperty("作用域")
        @Schema(description = "作用域")
        private String scope;

        @ApiModelProperty("作用域ID")
        @Schema(description = "作用域ID")
        private String scopeId;

        @ApiModelProperty("标签")
        @Schema(description = "标签")
        private List<Tag> tags;

        @ApiModelProperty("事件列表")
        @Schema(description = "事件列表")
        private List<Event> events;
    }

    /**
     * 标签
     */
    @Data
    @ApiModel(description = "标签")
    @Schema(description = "标签")
    public static class Tag {
        @ApiModelProperty("键")
        @Schema(description = "键")
        private String key;

        @ApiModelProperty("值")
        @Schema(description = "值")
        private String value;
    }

    /**
     * 事件
     */
    @Data
    @ApiModel(description = "事件")
    @Schema(description = "事件")
    public static class Event {
        @ApiModelProperty("事件UUID")
        @Schema(description = "事件UUID")
        private String uuid;

        @ApiModelProperty("来源")
        @Schema(description = "来源")
        private Source source;

        @ApiModelProperty("事件名称")
        @Schema(description = "事件名称")
        private String name;

        @ApiModelProperty("事件类型")
        @Schema(description = "事件类型")
        private String type;

        @ApiModelProperty("事件消息")
        @Schema(description = "事件消息")
        private String message;

        @ApiModelProperty("开始时间")
        @Schema(description = "开始时间")
        private Long startTime;

        @ApiModelProperty("结束时间")
        @Schema(description = "结束时间")
        private Long endTime;
    }

    /**
     * 来源
     */
    @Data
    @ApiModel(description = "来源")
    @Schema(description = "来源")
    public static class Source {
        @ApiModelProperty("服务名称")
        @Schema(description = "服务名称")
        private String service;

        @ApiModelProperty("服务实例名称")
        @Schema(description = "服务实例名称")
        private String serviceInstance;

        @ApiModelProperty("端点名称")
        @Schema(description = "端点名称")
        private String endpoint;
    }
}
