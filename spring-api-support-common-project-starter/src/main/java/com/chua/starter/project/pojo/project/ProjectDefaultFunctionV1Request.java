package com.chua.starter.project.pojo.project;

import com.chua.common.support.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 项目默认功能
 *
 * @author CH
 */
@Data
@Schema(description = "项目默认功能")
public class ProjectDefaultFunctionV1Request {

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    @NotNull(message = "项目id不能为空", groups = UpdateGroup.class)
    private Integer sysProjectId;

    /**
     * 类型�?字典�?;
     */
    @Schema(description = "类型�?字典�?;")
    private Integer sysProjectDefaultType;
    /**
     * 类型�?字典�?;
     */
    @Schema(description = "类型�?字典�?;")
    private String sysProjectDefaultTypeName;

    /**
     * 设置默认�? 0: false
     */
    @Schema(description = "设置默认�? 0: false")
    @NotNull(message = "默认值不能为�?, groups = UpdateGroup.class)
    private Integer sysSetDefault;
}
