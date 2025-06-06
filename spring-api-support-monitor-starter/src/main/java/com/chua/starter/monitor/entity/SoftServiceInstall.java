package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 软件服务安装记录
 *
 * @author CH
 * @since 2024/7/18
 */
@ApiModel(description = "软件服务安装记录")
@Schema(description = "软件服务安装记录")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "soft_service_install")
public class SoftServiceInstall extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "install_id", type = IdType.AUTO)
    @ApiModelProperty(value = "安装记录ID")
    @Schema(description = "安装记录ID")
    @NotNull(message = "安装记录ID不能为空", groups = {UpdateGroup.class})
    private Integer installId;

    /**
     * SSH ID，关联MachineSshModel的ID
     */
    @TableField(value = "ssh_id")
    @ApiModelProperty(value = "SSH ID，关联MachineSshModel的ID")
    @Schema(description = "SSH ID，关联MachineSshModel的ID")
    @NotBlank(message = "SSH ID不能为空", groups = {AddGroup.class})
    private String sshId;

    /**
     * 软件服务ID
     */
    @TableField(value = "soft_service_id")
    @ApiModelProperty(value = "软件服务ID")
    @Schema(description = "软件服务ID")
    @NotNull(message = "软件服务ID不能为空", groups = {AddGroup.class})
    private Integer softServiceId;

    /**
     * 安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败
     */
    @TableField(value = "install_status")
    @ApiModelProperty(value = "安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败")
    @Schema(description = "安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败")
    private Integer installStatus;

    /**
     * 安装路径
     */
    @TableField(value = "install_path")
    @ApiModelProperty(value = "安装路径")
    @Schema(description = "安装路径")
    private String installPath;

    /**
     * 安装日志
     */
    @TableField(value = "install_log")
    @ApiModelProperty(value = "安装日志")
    @Schema(description = "安装日志")
    private String installLog;

    /**
     * 安装版本
     */
    @TableField(value = "install_version")
    @ApiModelProperty(value = "安装版本")
    @Schema(description = "安装版本")
    private String installVersion;

    /**
     * 安装时间
     */
    @TableField(value = "install_time")
    @ApiModelProperty(value = "安装时间")
    @Schema(description = "安装时间")
    private Long installTime;

    /**
     * 最后一次启动时间
     */
    @TableField(value = "last_start_time")
    @ApiModelProperty(value = "最后一次启动时间")
    @Schema(description = "最后一次启动时间")
    private Long lastStartTime;

    /**
     * 最后一次停止时间
     */
    @TableField(value = "last_stop_time")
    @ApiModelProperty(value = "最后一次停止时间")
    @Schema(description = "最后一次停止时间")
    private Long lastStopTime;

    /**
     * 运行状态：0-未运行，1-运行中
     */
    @TableField(value = "run_status")
    @ApiModelProperty(value = "运行状态：0-未运行，1-运行中")
    @Schema(description = "运行状态：0-未运行，1-运行中")
    private Integer runStatus;
} 