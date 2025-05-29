package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 维护操作日志实体类
 * 用于记录所有维护操作的日志
 * 
 * @author CH
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("maintenance_log")
@ApiModel(value = "MaintenanceLog", description = "维护操作日志实体")
public class MaintenanceLog {
    
    /**
     * 日志ID
     */
    @ApiModelProperty(value = "日志ID", example = "1")
    @TableId(value = "maintenance_log_id", type = IdType.AUTO)
    private Integer maintenanceLogId;
    
    /**
     * 维护组ID
     */
    @ApiModelProperty(value = "维护组ID", required = true, example = "1")
    @TableField("maintenance_group_id")
    private Long maintenanceGroupId;
    
    /**
     * 维护主机ID（可选）
     */
    @ApiModelProperty(value = "维护主机ID", example = "1")
    @TableField("maintenance_host_id")
    private Long maintenanceHostId;
    
    /**
     * 维护脚本ID（可选）
     */
    @ApiModelProperty(value = "维护脚本ID", example = "1")
    @TableField("maintenance_script_id")
    private Long maintenanceScriptId;
    
    /**
     * 维护文件ID（可选）
     */
    @ApiModelProperty(value = "维护文件ID", example = "1")
    @TableField("maintenance_file_id")
    private Long maintenanceFileId;
    
    /**
     * 操作类型
     * 1: 创建, 2: 更新, 3: 删除, 4: 启用/禁用, 5: 连接测试, 6: 执行脚本, 7: 上传文件, 8: 部署文件
     */
    @ApiModelProperty(value = "操作类型: 1创建, 2更新, 3删除, 4启用/禁用, 5连接测试, 6执行脚本, 7上传文件, 8部署文件", required = true, example = "1")
    @TableField("operation_type")
    private Integer operationType;
    
    /**
     * 操作对象类型
     * 1: 维护组, 2: 维护主机, 3: 维护脚本, 4: 维护文件
     */
    @ApiModelProperty(value = "操作对象类型: 1维护组, 2维护主机, 3维护脚本, 4维护文件", required = true, example = "1")
    @TableField("target_type")
    private Integer targetType;
    
    /**
     * 操作用户
     */
    @ApiModelProperty(value = "操作用户", example = "admin")
    @TableField("operation_user")
    private String operationUser;
    
    /**
     * 操作详情（JSON格式记录详细信息）
     */
    @ApiModelProperty(value = "操作详情（JSON格式记录详细信息）", example = "{\"key\":\"value\"}")
    @TableField("operation_details")
    private String operationDetails;
    
    /**
     * 操作结果
     * true: 成功，false: 失败
     */
    @ApiModelProperty(value = "操作结果: true成功, false失败", example = "true")
    @TableField("operation_result")
    private Boolean operationResult;
    
    /**
     * 操作结果消息
     */
    @ApiModelProperty(value = "操作结果消息", example = "操作成功")
    @TableField("result_message")
    private String resultMessage;
    
    /**
     * IP地址
     */
    @ApiModelProperty(value = "IP地址", example = "192.168.1.1")
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间", example = "2023-01-01 12:00:00")
    @TableField("operation_time")
    private LocalDateTime operationTime;
} 