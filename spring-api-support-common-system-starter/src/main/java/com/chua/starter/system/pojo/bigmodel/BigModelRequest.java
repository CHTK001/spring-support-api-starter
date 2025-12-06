package com.chua.starter.system.pojo.bigmodel;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    /**
     * 最大tokens
     */
    @Builder.Default
    @NotNull(message = "最大tokens不能为空", groups = SelectGroup.class)
    private Integer tokens = 2048;
    /**
     * topK
     */
    @Builder.Default
    @NotNull(message = "topK不能为空", groups = SelectGroup.class)
    private Integer topK = 4;
    /**
     * 温度
     */
    @Builder.Default
    @NotNull(message = "温度不能为空", groups = SelectGroup.class)
    private Double temperature = 0.3;

    /**
     * 随机种子
     */
    @Builder.Default
    private Long seed = -1L;

    /**
     * 文件分组
     */
    private String groupId;

    /**
     * 文件base64
     */
    private com.chua.deeplearning.support.ml.bigmodel.BigModelRequest.FileInfo[] files;
}
