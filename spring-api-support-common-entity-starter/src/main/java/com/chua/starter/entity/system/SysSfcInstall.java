package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 用户安装的组件表
 *
 * @author CH
 * @since 2024/10/24
 */
@Schema(description = "用户安装的组件表")
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sfc_install")
public class SysSfcInstall extends SysBase implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "sys_sfc_install_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Integer sysSfcInstallId;

    /**
     * 安装用户ID
     */
    @TableField(value = "user_id")
    @Schema(description = "安装用户ID")
    @Size(max = 255, message = "安装用户ID最大长度需小于255")
    private Integer userId;

    /**
     * 组件ID
     */
    @TableField(value = "sys_sfc_id")
    @Schema(description = "组件ID")
    @Size(max = 255, message = "组件ID最大长度需小于255")
    private Integer sysSfcId;
}
