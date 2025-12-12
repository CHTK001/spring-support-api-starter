package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 节点 URL 映射信息 DTO
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Schema(description = "节点 URL 映射信息")
public class NodeUrlMappingDTO {

    /**
     * URL 模式
     */
    @Schema(description = "URL 模式")
    private String pattern;

    /**
     * HTTP 方法列表
     */
    @Schema(description = "HTTP 方法列表")
    private List<String> methods;

    /**
     * 处理器信息
     */
    @Schema(description = "处理器信息")
    private String handler;

    /**
     * 所属 Controller
     */
    @Schema(description = "所属 Controller")
    private String controller;

    /**
     * 生产的 Content-Type
     */
    @Schema(description = "生产的 Content-Type")
    private List<String> produces;

    /**
     * 消费的 Content-Type
     */
    @Schema(description = "消费的 Content-Type")
    private List<String> consumes;
}
