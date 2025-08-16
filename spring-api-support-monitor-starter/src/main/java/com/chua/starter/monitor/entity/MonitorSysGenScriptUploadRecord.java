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
 *
 * @author CH
 * @since 2023-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_script_upload_record")
@ApiModel("脚本上传记录")
@Schema(name = "MonitorSysGenScriptUploadRecord")
public class MonitorSysGenScriptUploadRecord extends SysBase {

    /**
     * 主键ID
     * <p>
     * 示例: 1
     */
    @TableId(value = "monitor_sys_gen_script_upload_record_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Integer monitorSysGenScriptUploadRecordId;

    /**
     * 脚本ID（可选）
     *
     * 示例: 1001
     */
    @TableField("monitor_sys_gen_script_id")
    @ApiModelProperty("脚本ID")
    private Integer monitorSysGenScriptId;

    /**
     * 上传类型：REMOTE/NODE
     *
     * 示例: "REMOTE"
     */
    @TableField("monitor_sys_gen_script_upload_type")
    @ApiModelProperty("上传类型：REMOTE/NODE")
    private String monitorSysGenScriptUploadType;

    /**
     * 服务器ID（当类型为 REMOTE 时）
     *
     * 示例: 2001
     */
    @TableField("monitor_sys_gen_script_upload_server_id")
    @ApiModelProperty("服务器ID")
    private Integer monitorSysGenScriptUploadServerId;

    /**
     * 节点ID（当类型为 NODE 时）
     *
     * 示例: "node-001"
     */
    @TableField("monitor_sys_gen_script_upload_node_id")
    @ApiModelProperty("节点ID")
    private String monitorSysGenScriptUploadNodeId;

    /**
     * 目标路径
     *
     * 示例: "/home/user/scripts/"
     */
    @TableField("monitor_sys_gen_script_upload_target_path")
    @ApiModelProperty("目标路径")
    private String monitorSysGenScriptUploadTargetPath;

    /**
     * 文件名
     *
     * 示例: "script.sh"
     */
    @TableField("monitor_sys_gen_script_upload_file_name")
    @ApiModelProperty("文件名")
    private String monitorSysGenScriptUploadFileName;

    /**
     * 文件大小（字节）
     *
     * 示例: 1024L
     */
    @TableField("monitor_sys_gen_script_upload_file_size")
    @ApiModelProperty("文件大小（字节）")
    private Long monitorSysGenScriptUploadFileSize;

    /**
     * 上传状态：SUCCESS/FAILED
     *
     * 示例: "SUCCESS"
     */
    @TableField("monitor_sys_gen_script_upload_status")
    @ApiModelProperty("上传状态：SUCCESS/FAILED")
    private String monitorSysGenScriptUploadStatus;

    /**
     * 错误信息
     *
     * 示例: "连接超时"
     */
    @TableField("monitor_sys_gen_script_upload_error_message")
    @ApiModelProperty("错误信息")
    private String monitorSysGenScriptUploadErrorMessage;

    /**
     * 脚本名称（持久化字段）
     */
    @TableField("monitor_sys_gen_script_upload_script_name")
    @ApiModelProperty("脚本名称")
    @Schema(description = "脚本名称")
    private String monitorSysGenScriptUploadScriptName;

    /**
     * 脚本描述（持久化字段）
     */
    @TableField("monitor_sys_gen_script_upload_script_description")
    @ApiModelProperty("脚本描述")
    @Schema(description = "脚本描述")
    private String monitorSysGenScriptUploadScriptDescription;

    /**
     * 实际IP（持久化字段；REMOTE 为服务器IP，NODE 为节点IP）
     */
    @TableField("monitor_sys_gen_script_upload_ip")
    @ApiModelProperty("实际IP")
    @Schema(description = "实际IP（REMOTE 为服务器IP，NODE 为节点IP）")
    private String monitorSysGenScriptUploadIp;


    /**
     * 非数据库字段：脚本名称
     */
    @TableField(exist = false)
    @ApiModelProperty("脚本名称")
    @Schema(description = "脚本名称")
    private String scriptName;

    /**
     * 非数据库字段：脚本描述
     */
    @TableField(exist = false)
    @ApiModelProperty("脚本描述")
    @Schema(description = "脚本描述")
    private String scriptDescription;

    /**
     * 非数据库字段：服务器IP
     */
    @TableField(exist = false)
    @ApiModelProperty("服务器IP")
    @Schema(description = "服务器IP")
    private String serverIp;

}

