package com.chua.starter.system.pojo.vincent;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 超分辨率提交任务
 *
 * @author CH
 */
@Data
@Schema(description = "超分辨率提交任务")
public class ImageResolutionAddTaskV2Request {

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
    @Schema(description = "文件地址;base64/url")
    private String url;
    /**
     * 输出图像尺寸
     */
    @Schema(description = "输出图像尺寸")
    private int scaleFactor;

}
