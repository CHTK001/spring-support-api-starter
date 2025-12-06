package com.chua.starter.system.pojo.project;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 同步请求
 *
 * @author CH
 */
@Data
@Schema(description = "模板查询请求")
public class TemplateV1Request {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = SelectGroup.class)
    private Integer sysProjectId;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容(支持模糊查询)")
    private String sysTemplateContent;
}
