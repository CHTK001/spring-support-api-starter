package com.chua.starter.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 *
 * @since 2024/10/24
 * @author CH    
 */
/**
 * 用户安装的组件
 */
@ApiModel(description="用户安装的组件")
@Schema(description="用户安装的组件")
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_sfc_install")
public class SysSfcInstall extends SysBase implements Serializable {
    @TableId(value = "sys_sfc_install_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer sysSfcInstallId;

    /**
     * 安裝的用户ID
     */
    @TableField(value = "user_id")
    @ApiModelProperty(value="安裝的用户ID")
    @Schema(description="安裝的用户ID")
    @Size(max = 255,message = "安裝的用户ID最大长度要小于 255")
    private Integer userId;

    /**
     * 组件ID
     */
    @TableField(value = "sys_sfc_id")
    @ApiModelProperty(value="组件ID")
    @Schema(description="组件ID")
    @Size(max = 255,message = "组件ID最大长度要小于 255")
    private Integer sysSfcId;
}