package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;

@Data
@TableName("sys_ai_module")
public class SysAiModule extends SysBase {

    @TableId(value = "sys_ai_module_id", type = IdType.AUTO)
    private Integer sysAiModuleId;
    private String sysAiModuleName;
    private Integer sysAiModuleRoleSetting;
    private String sysAiModuleCode;
    private String sysAiModuleType;
    private String sysAiModuleUrl;
    private Integer sysProjectId;
    private Integer sysAiModuleVlm;
    private Boolean sysAiModuleLocation;
    @TableField(exist = false)
    private String sysProjectName;
    @TableField(exist = false)
    private String sysProjectIcon;
    private String sysAiModuleManufacturers;
    private Integer sysAiModuleStatus;
    private Integer sysAiModuleSort;
    private String sysAiModuleVersion;
    private String sysAiModuleRemark;
    @TableField(exist = false)
    private Integer sysAiVincentSupportAsync;
    @TableField(exist = false)
    private SysAiVincentSetting vincentSetting;
}
