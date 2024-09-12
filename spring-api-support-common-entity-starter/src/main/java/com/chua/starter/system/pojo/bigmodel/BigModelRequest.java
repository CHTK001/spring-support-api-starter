package com.chua.starter.system.pojo.bigmodel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 大模型请求
 * @author CH
 * @since 2024/9/10
 */
@Data
@Schema(description = "大模型请求")
@Builder
public class BigModelRequest {

    /**
     * 请求id
     */
    private String requestId;
    /**
     * 系统预设值
     */
    private String system;

    /**
     * 用户输入
     */
    private String user;

    /**
     * 模型
     */
    private String model;
}
