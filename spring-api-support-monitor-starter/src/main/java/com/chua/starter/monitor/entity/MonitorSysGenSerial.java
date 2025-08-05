package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 串口配置管理
 *
 * @author CH
 * @since 2024/7/23
 */
@ApiModel(description = "串口配置管理")
@Schema(description = "串口配置管理")
@Data
@TableName(value = "monitor_sys_gen_serial")
public class MonitorSysGenSerial implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "monitor_sys_gen_serial_id", type = IdType.AUTO)
    @ApiModelProperty(value = "串口配置ID")
    @Schema(description = "串口配置ID")
    @NotNull(message = "不能为null")
    private Integer monitorSysGenSerialId;
    /**
     * 串口名称
     */
    @TableField(value = "monitor_sys_gen_serial_name")
    @ApiModelProperty(value = "串口名称")
    @Schema(description = "串口名称")
    @Size(max = 255, message = "串口名称最大长度要小于 255")
    private String monitorSysGenSerialName;
    /**
     * 串口端口
     */
    @TableField(value = "monitor_sys_gen_serial_port")
    @ApiModelProperty(value = "串口端口")
    @Schema(description = "串口端口")
    @Size(max = 255, message = "串口端口最大长度要小于 255")
    private String monitorSysGenSerialPort;
    /**
     * 波特率
     */
    @TableField(value = "monitor_sys_gen_serial_baud_rate")
    @ApiModelProperty(value = "波特率")
    @Schema(description = "波特率")
    private Integer monitorSysGenSerialBaudRate;
    /**
     * 数据位
     */
    @TableField(value = "monitor_sys_gen_serial_data_bits")
    @ApiModelProperty(value = "数据位")
    @Schema(description = "数据位")
    private Integer monitorSysGenSerialDataBits;
    /**
     * 停止位
     */
    @TableField(value = "monitor_sys_gen_serial_stop_bits")
    @ApiModelProperty(value = "停止位")
    @Schema(description = "停止位")
    private Integer monitorSysGenSerialStopBits;
    /**
     * 校验位
     */
    @TableField(value = "monitor_sys_gen_serial_parity")
    @ApiModelProperty(value = "校验位")
    @Schema(description = "校验位")
    @Size(max = 50, message = "校验位最大长度要小于 50")
    private String monitorSysGenSerialParity;
    /**
     * 流控制
     */
    @TableField(value = "monitor_sys_gen_serial_flow_control")
    @ApiModelProperty(value = "流控制")
    @Schema(description = "流控制")
    @Size(max = 50, message = "流控制最大长度要小于 50")
    private String monitorSysGenSerialFlowControl;
    /**
     * 超时时间(毫秒)
     */
    @TableField(value = "monitor_sys_gen_serial_timeout")
    @ApiModelProperty(value = "超时时间(毫秒)")
    @Schema(description = "超时时间(毫秒)")
    private Integer monitorSysGenSerialTimeout;
    /**
     * 描述
     */
    @TableField(value = "monitor_sys_gen_serial_desc")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度要小于 255")
    private String monitorSysGenSerialDesc;
    /**
     * 状态;0:停用
     */
    @TableField(value = "monitor_sys_gen_serial_status")
    @ApiModelProperty(value = "状态;0:停用")
    @Schema(description = "状态;0:停用")
    private Integer monitorSysGenSerialStatus;
    /**
     * 生成器id
     */
    @TableField(value = "gen_id")
    @ApiModelProperty(value = "生成器id")
    @Schema(description = "生成器id")
    private Integer genId;
} 