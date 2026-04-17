package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;

@Data
@TableName("sys_ai_group_message")
public class SysAiGroupMessage extends SysBase {

    @TableId(value = "sys_ai_group_message_id", type = IdType.AUTO)
    private Integer sysAiGroupMessageId;
    private Integer sysAiGroupId;
    private String sysAiGroupMessageRole;
    private String sysAiGroupMessageContent;
    private String sysAiGroupMessageType;
}
