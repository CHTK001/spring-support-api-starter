package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import com.chua.starter.mybatis.pojo.SysBase;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author CH
 */

/**
 * 系统日志信息表
 */
@Schema(description = "系统日志信息表")
@Data
@Table(comment = "系统日志信息表")
@TableName(value = "sys_logger")
public class SysLogger extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer logId;

    /**
     * 日志编号
     */
    @TableField(value = "log_code")
    @Schema(description = "日志编号")
    private String logCode;

    /**
     * 日志名称
     */
    @TableField(value = "log_name")
    @Schema(description = "日志名称")
    private String logName;

    /**
     * 耗时
     */
    @TableField(value = "log_cost")
    @Schema(description = "耗时")
    private Integer logCost;

    /**
     * 日志请求地址
     */
    @TableField(value = "log_mapping")
    @Schema(description = "日志请求地址")
    private String logMapping;

    /**
     * 日志内容
     */
    @TableField(value = "log_content")
    @Schema(description = "日志内容")
    private String logContent;

    /**
     * 请求地址
     */
    @TableField(value = "log_address")
    @Schema(description = "请求地址")
    private String logAddress;

    /**
     * 请求地址位置
     */
    @TableField(value = "log_address_position")
    @Schema(description = "请求地址位置")
    private String logAddressPosition;


    /**
     * 请求链路
     */
    @TableField(value = "log_watch")
    @Schema(description = "请求链路")
    @Column(columnDefinition = "longtext", comment = "请求链路")
    private String logWatch;
    /**
     * 查询参数
     */
    @TableField(value = "log_param")
    @Schema(description = "查询参数")
    @Column(columnDefinition = "longtext", comment = "查询参数")
    private String logParam;
    /**
     * 动作
     */
    @TableField(value = "log_action")
    @Schema(description = "动作")
    private String logAction;

    /**
     * 状态; 1:成功;
     */
    @TableField(value = "log_status")
    @Schema(description = "状态; 1:成功;")
    private String logStatus;


    /**
     * 动作
     */
    @TableField(value = "log_day")
    @Column(columnDefinition = "date", comment = "时间(天)")
    @Schema(description = "时间(天)")
    private Date logDay;

}