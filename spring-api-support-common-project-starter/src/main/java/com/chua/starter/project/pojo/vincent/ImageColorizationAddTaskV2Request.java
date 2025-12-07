package com.chua.starter.project.pojo.vincent;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 图像上色提交任务
 *
 * @author CH
 */
@Data
@Schema(description = "图像上色提交任务")
public class ImageColorizationAddTaskV2Request {

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
     * 文件地址
     */
    @Schema(description = "文件地址;base64/url")
    private String url;

}
