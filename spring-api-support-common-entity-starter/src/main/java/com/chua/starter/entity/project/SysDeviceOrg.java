package com.chua.starter.entity.project;

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
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备组织")
@Data
@TableName(value = "sys_device_org")
public class SysDeviceOrg extends SysBase {
    @TableId(value = "sys_device_org_id", type = IdType.INPUT)
    @Schema(description = "")
    private String sysDeviceOrgId;
    /**
     * 产品id
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "产品id")
    private Integer sysProjectId;


    /**
     * 组织名称
     */
    @TableField(value = "sys_device_org_name")
    @Schema(description = "组织名称")
    private String sysDeviceOrgName;

    /**
     * 上级组织
     */
    @TableField(value = "sys_device_org_pid")
    @Schema(description = "上级组织")
    private String sysDeviceOrgPid;

    /**
     * 路径
     */
    @TableField(value = "sys_device_org_tree_path")
    @Schema(description = "路径")
    private String sysDeviceOrgTreePath;
}
