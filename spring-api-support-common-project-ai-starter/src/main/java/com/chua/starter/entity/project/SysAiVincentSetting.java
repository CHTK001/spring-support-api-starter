package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;

import java.util.List;

@Data
@TableName("sys_ai_vincent_setting")
public class SysAiVincentSetting extends SysBase {

    @TableId(value = "sys_ai_vincent_id", type = IdType.AUTO)
    private Integer sysAiVincentId;
    private Integer sysAiModuleId;
    private String sysAiVincentSupportedSize;
    private Integer sysAiVincentSupportedMaxSize;
    private String sysAiVincentSupportedStyle;
    private Integer sysAiVincentSupportedNumber;
    private Integer sysAiVincentSupportAsync;
    private Integer sysAiVincentSupportAudio;
    private String sysAiVincentSupportedQuality;
    private String sysAiVincentSupportedFps;
    private Integer sysAiVincentSupportRefImage;
    @TableField(exist = false)
    private List<SysAiVincentStyle> styles;
}
