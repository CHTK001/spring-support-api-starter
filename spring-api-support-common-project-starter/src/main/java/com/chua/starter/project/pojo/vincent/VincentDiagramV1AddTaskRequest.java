package com.chua.starter.project.pojo.vincent;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文生图提交任�?
 *
 * @author CH
 */
@Data
@Schema(description = "文生图提交任�?)
public class VincentDiagramV1AddTaskRequest {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = {SelectGroup.class})
    private Integer sysProjectId;
    /**
     * 模型名称
     */
    @Schema(description = "模型名称")
    @NotBlank(message = "模型名称不能为空", groups = {SelectGroup.class})
    private String model;

    /**
     * 输入参数
     */
    @Schema(description = "输入参数")
    private Input input;


    @Schema(description = "参数")
    private Parameters parameters;

    /**
     * 参数
     */
    @Data
    public static class Parameters {


        /**
         * 输出图像的风�?
         */
        @Schema(description = "输出图像的风�?)
        private String style;

        /**
         * 输出图像尺寸
         */
        @Schema(description = "输出图像尺寸")
        private String size;
        /**
         * 输出图像质量
         */
        @Schema(description = "输出图像质量")
        private String quality;
        /**
         * 随机种子
         */
        @Schema(description = "随机种子")
        private Integer seed;
        /**
         * 输出图像帧率
         */
        @Schema(description = "输出图像帧率")
        private Integer fps;
        /**
         * 是否支持音效
         */
        @Schema(description = "是否支持音效")
        private Integer withAudio;
        /**
         * 输出图像数量
         */
        @Schema(description = "输出图像数量")
        @NotNull(message = "输出图像数量不能为空", groups = {SelectGroup.class})
        private Integer number;
        /**
         * 控制输出图像与垫图（参考图）的相似�?
         */
        @Schema(description = "控制输出图像与垫图（参考图）的相似�?)
        private Float refStrength;
    }

    /**
     * 输入参数
     */
    @Data
    public static class Input {

        /**
         * 提示�?
         */
        @Schema(description = "提示�?)
        @NotBlank(message = "提示词不能为�?, groups = {SelectGroup.class})
        private String prompt;
        /**
         * 提示�?
         */
        @Schema(description = "提示�?)
        private String negativePrompt;
        /**
         * 参考图�?
         */
        @Schema(description = "参考图�?)
        private String refImg;

    }
}
