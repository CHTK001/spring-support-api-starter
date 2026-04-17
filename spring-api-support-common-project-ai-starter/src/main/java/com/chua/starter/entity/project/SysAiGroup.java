package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;

@Data
@TableName("sys_ai_group")
public class SysAiGroup extends SysBase {

    @TableId(value = "sys_ai_group_id", type = IdType.AUTO)
    private Integer sysAiGroupId;
    private String sysAiGroupName;
    private Integer sysProjectId;
    private String sysUserId;
    private String sysAiGroupColor;
    private String sysAiGroupConfig;
}
