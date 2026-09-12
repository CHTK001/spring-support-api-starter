package com.chua.starter.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* 设备组织
*
* @since 2026/08/03
* @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备组织")
@Data
@TableName(value = "sys_device_org")
public class SysDeviceOrg extends SysBase {
    /**
    * 组织主键标识
     */
    @TableId(value = "sys_device_org_id", type = IdType.INPUT)
    @Schema(description = "")
    /** sysdeviceorgid */
    private String sysDeviceOrgId;

    /**
    * 产品标识
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "产品id")
    /** sysprojectid */
    private Integer sysProjectId;

    /**
    * 组织名称
     */
    @TableField(value = "sys_device_org_name")
    @Schema(description = "组织名称")
    /** sysdeviceorg名称 */
    private String sysDeviceOrgName;

    /**
    * 上级组织
     */
    @TableField(value = "sys_device_org_pid")
    @Schema(description = "上级组织")
    /** sysdeviceorgpid */
    private String sysDeviceOrgPid;

    /**
    * 路径
     */
    @TableField(value = "sys_device_org_tree_path")
    @Schema(description = "路径")
    /** sysdevice或gtree路径 */
    private String sysDeviceOrgTreePath;
}
