package com.chua.starter.device.pojo.request;

import com.chua.common.support.base.validator.group.SelectGroup;
import com.chua.starter.entity.project.SysSmsTemplate;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备模板分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备模板分页请求")
public class TemplatePageV1Request extends Query<SysSmsTemplate> {

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = SelectGroup.class)
    private Integer sysProjectId;

    @Schema(description = "设备名称(支持模糊查询)")
    private String sysDeviceName;

    @Schema(description = "设备序列号(支持模糊查询)")
    private String sysDeviceSerialNumber;

    @Schema(description = "设备是否禁用;0:启用")
    private Integer sysDeviceStatus;

    @Schema(description = "设备是否在线")
    private Integer sysDeviceOnline;
}
