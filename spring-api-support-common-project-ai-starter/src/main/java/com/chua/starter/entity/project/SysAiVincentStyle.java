package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_ai_vincent_style")
public class SysAiVincentStyle {

    @TableId(value = "sys_ai_vincent_style_id", type = IdType.AUTO)
    private Integer sysAiVincentStyleId;
    private Integer sysAiModuleId;
    private String sysAiVincentStyleName;
    private String sysAiVincentStyleCode;
    private String sysAiVincentStyleImage;
}
