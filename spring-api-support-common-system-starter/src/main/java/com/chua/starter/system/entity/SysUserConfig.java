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

import java.io.Serializable;

/**
 * 鐢ㄦ埛淇℃伅 - 鐢ㄦ埛璁剧疆琛? *
 * @author CH
 * @since 2024/8/30
 */
@ApiModel(description = "鐢ㄦ埛淇℃伅 - 鐢ㄦ埛璁剧疆琛?)
@Schema(description = "鐢ㄦ埛淇℃伅 - 鐢ㄦ埛璁剧疆琛?)
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_config")
public class SysUserConfig extends SysBase implements Serializable {

    @TableId(value = "sys_user_config_id", type = IdType.AUTO)
    @ApiModelProperty(value = "閰嶇疆ID")
    @Schema(description = "閰嶇疆ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysUserConfigId;

    /**
     * 寰俊openid
     */
    @TableField(value = "sys_user_config_wx_openid")
    @ApiModelProperty(value = "寰俊openid")
    @Schema(description = "寰俊openid")
    @Size(max = 255, message = "寰俊openid鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserConfigWxOpenid;

    /**
     * 寰俊unionid
     */
    @TableField(value = "sys_user_config_wx_unionid")
    @ApiModelProperty(value = "寰俊unionid")
    @Schema(description = "寰俊unionid")
    @Size(max = 255, message = "寰俊unionid鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserConfigWxUnionid;
}