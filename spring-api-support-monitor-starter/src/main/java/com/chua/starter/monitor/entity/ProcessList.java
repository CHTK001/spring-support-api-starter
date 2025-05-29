package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 进程列表
 *
 * @author CH
 * @since 2025/5/29 16:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "进程列表")
@TableName("process_list")
public class ProcessList extends SysBase {

    /**
     * 进程列表ID
     */
    @Schema(description = "进程列表ID")
    @TableId(value = "process_list_id", type = IdType.AUTO)
    private Integer processListId;

    /**
     * 进程列表名称
     */
    @Schema(description = "进程列表名称")
    @TableField("process_list_name")
    private String processListName;

    /**
     * 进程列表描述
     */
    @Schema(description = "进程列表描述")
    @TableField("process_list_description")
    private String processListDescription;

    /**
     * 启动命令
     */
    @Schema(description = "启动命令")
    @TableField("process_list_start_command")
    private String processListStartCommand;

    /**
     * 停止命令
     */
    @Schema(description = "停止命令")
    @TableField("process_list_stop_command")
    private String processListStopCommand;

    /**
     * 重启命令
     */
    @Schema(description = "重启命令")
    @TableField("process_list_restart_command")
    private String processListRestartCommand;
    /**
     * 进程列表状态;0:停用;1:启用
     */
    @Schema(description = "进程列表状态;0:停用;1:启用")
    @TableField("process_list_status")
    private Boolean processListStatus;

}
