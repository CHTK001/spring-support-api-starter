package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 脚本上传记录
 *
 * 记录脚本在远程服务器或节点上的文件上传操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_script_upload_record")
@ApiModel("脚本上传记录")
@Schema(name = "MonitorSysGenScriptUploadRecord")
public class MonitorSysGenScriptUploadRecord extends SysBase {

    @TableId(value = "monitor_sys_gen_script_upload_record_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Integer monitorSysGenScriptUploadRecordId;

    /** 脚本ID（可选） */
    @TableField("monitor_sys_gen_script_id")
    @ApiModelProperty("脚本ID")
    private Integer monitorSysGenScriptId;

    /** 上传类型：REMOTE/NODE */
    @TableField("monitor_sys_gen_script_upload_type")
    @ApiModelProperty("上传类型：REMOTE/NODE")
    private String monitorSysGenScriptUploadType;

    /** 服务器ID（当类型为 REMOTE 时） */
    @TableField("monitor_sys_gen_script_upload_server_id")
    @ApiModelProperty("服务器ID")
    private Integer monitorSysGenScriptUploadServerId;

    /** 节点ID（当类型为 NODE 时） */
    @TableField("monitor_sys_gen_script_upload_node_id")
    @ApiModelProperty("节点ID")
    private String monitorSysGenScriptUploadNodeId;

    /** 目标路径 */
    @TableField("monitor_sys_gen_script_upload_target_path")
    @ApiModelProperty("目标路径")
    private String monitorSysGenScriptUploadTargetPath;

    /** 文件名 */
    @TableField("monitor_sys_gen_script_upload_file_name")
    @ApiModelProperty("文件名")
    private String monitorSysGenScriptUploadFileName;

    /** 文件大小（字节） */
    @TableField("monitor_sys_gen_script_upload_file_size")
    @ApiModelProperty("文件大小（字节）")
    private Long monitorSysGenScriptUploadFileSize;

    /** 上传状态：SUCCESS/FAILED */
    @TableField("monitor_sys_gen_script_upload_status")
    @ApiModelProperty("上传状态：SUCCESS/FAILED")
    private String monitorSysGenScriptUploadStatus;

    /** 错误信息 */
    @TableField("monitor_sys_gen_script_upload_error_message")
    @ApiModelProperty("错误信息")
    private String monitorSysGenScriptUploadErrorMessage;
}

