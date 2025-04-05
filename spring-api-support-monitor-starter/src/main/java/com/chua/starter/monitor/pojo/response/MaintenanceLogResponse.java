package com.chua.starter.monitor.pojo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 维护日志响应对象
 *
 * @author CH
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "MaintenanceLogResponse", description = "维护日志响应对象")
public class MaintenanceLogResponse {
    
    /**
     * 日志ID
     */
    @ApiModelProperty(value = "日志ID", example = "1")
    private Long maintenanceLogId;
    
    /**
     * 维护组ID
     */
    @ApiModelProperty(value = "维护组ID", example = "1")
    private Long maintenanceGroupId;
    
    /**
     * 维护主机ID（可选）
     */
    @ApiModelProperty(value = "维护主机ID", example = "1")
    private Long maintenanceHostId;
    
    /**
     * 维护脚本ID（可选）
     */
    @ApiModelProperty(value = "维护脚本ID", example = "1")
    private Long maintenanceScriptId;
    
    /**
     * 维护文件ID（可选）
     */
    @ApiModelProperty(value = "维护文件ID", example = "1")
    private Long maintenanceFileId;
    
    /**
     * 操作类型
     * 1: 创建, 2: 更新, 3: 删除, 4: 启用/禁用, 5: 连接测试, 6: 执行脚本, 7: 上传文件, 8: 部署文件
     */
    @ApiModelProperty(value = "操作类型: 1创建, 2更新, 3删除, 4启用/禁用, 5连接测试, 6执行脚本, 7上传文件, 8部署文件", example = "1")
    private Integer operationType;
    
    /**
     * 操作类型描述
     */
    @ApiModelProperty(value = "操作类型描述", example = "创建")
    private String operationTypeDesc;
    
    /**
     * 操作对象类型
     * 1: 维护组, 2: 维护主机, 3: 维护脚本, 4: 维护文件
     */
    @ApiModelProperty(value = "操作对象类型: 1维护组, 2维护主机, 3维护脚本, 4维护文件", example = "1")
    private Integer targetType;
    
    /**
     * 操作对象类型描述
     */
    @ApiModelProperty(value = "操作对象类型描述", example = "维护组")
    private String targetTypeDesc;
    
    /**
     * 操作用户
     */
    @ApiModelProperty(value = "操作用户", example = "admin")
    private String operationUser;
    
    /**
     * 操作详情（JSON格式记录详细信息）
     */
    @ApiModelProperty(value = "操作详情（JSON格式记录详细信息）", example = "{\"key\":\"value\"}")
    private String operationDetails;
    
    /**
     * 操作结果
     * true: 成功，false: 失败
     */
    @ApiModelProperty(value = "操作结果: true成功, false失败", example = "true")
    private Boolean operationResult;
    
    /**
     * 操作结果消息
     */
    @ApiModelProperty(value = "操作结果消息", example = "操作成功")
    private String resultMessage;
    
    /**
     * IP地址
     */
    @ApiModelProperty(value = "IP地址", example = "192.168.1.1")
    private String ipAddress;
    
    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间", example = "2023-01-01 12:00:00")
    private LocalDateTime operationTime;
} 