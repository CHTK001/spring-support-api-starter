package com.chua.starter.system.entity;

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
 * 鐢ㄦ埛瀹夎鐨勭粍浠? *
 * @author CH
 * @since 2024/10/24
 */
@ApiModel(description = "鐢ㄦ埛瀹夎鐨勭粍浠")
@Schema(description = "鐢ㄦ埛瀹夎鐨勭粍浠")
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sfc_install")
public class SysSfcInstall extends SysBase implements Serializable {

    @TableId(value = "sys_sfc_install_id", type = IdType.AUTO)
    @ApiModelProperty(value = "涓婚敭ID")
    @Schema(description = "涓婚敭ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysSfcInstallId;

    /**
     * 瀹夎鐨勭敤鎴稩D
     */
    @TableField(value = "user_id")
    @ApiModelProperty(value = "瀹夎鐨勭敤鎴稩D")
    @Schema(description = "瀹夎鐨勭敤鎴稩D")
    @Size(max = 255, message = "瀹夎鐨勭敤鎴稩D鏈€澶ч暱搴﹁灏忎簬 255")
    private Integer userId;

    /**
     * 缁勪欢ID
     */
    @TableField(value = "sys_sfc_id")
    @ApiModelProperty(value = "缁勪欢ID")
    @Schema(description = "缁勪欢ID")
    @Size(max = 255, message = "缁勪欢ID鏈€澶ч暱搴﹁灏忎簬 255")
    private Integer sysSfcId;
}