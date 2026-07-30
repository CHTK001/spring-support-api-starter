package com.chua.starter.device.pojo.request;

import com.chua.common.support.utils.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备模块通用项目请求。
 */
@Data
@Schema(description = "设备模块通用项目请求")
public class SyncV1Request {

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = SelectGroup.class)
    private Integer sysProjectId;
}
