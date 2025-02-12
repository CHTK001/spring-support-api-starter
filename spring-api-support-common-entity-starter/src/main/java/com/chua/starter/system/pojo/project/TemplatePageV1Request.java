package com.chua.starter.system.pojo.project;

import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysSmsTemplate;
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
public class TemplatePageV1Request extends Query<SysSmsTemplate> {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = SelectGroup.class)
    private Integer sysProjectId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称(支持模糊查询)")
    private String sysDeviceName;

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号(支持模糊查询)")
    private String sysDeviceSerialNumber;

    /**
     * 设备状态
     */
    @Schema(description = "设备是否禁用;0:启用")
    private Integer sysCameraTemplateStatus;


    /**
     * 设备是否在线;0:离线
     */
    @Schema(description = "设备是否在线;0:离线")
    private Integer sysCameraTemplateOnline;

}
