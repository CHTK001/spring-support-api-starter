package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;

@Data
@TableName("sys_ai_vincent_task")
public class SysAiVincentTask extends SysBase {

    @TableId(value = "sys_ai_vincent_task_id", type = IdType.AUTO)
    private Integer sysAiVincentTaskId;
    private String sysAiVincentTaskUid;
    private Integer sysAiVincentTaskStatus;
    private Integer sysProjectId;
    private Integer sysAiModuleId;
    private String sysAiVincentTaskCondition;
    private String sysAiVincentTaskType;
    private String sysAiVincentTaskUrl;
    private String sysAiVincentTaskLocalUrl;
    private String sysAiVincentTaskRequestId;
}
