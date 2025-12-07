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
 * 鐢ㄦ埛涓夋柟缁戝畾琛? *
 * @author CH
 * @since 2024/9/6
 */
@ApiModel(description = "鐢ㄦ埛涓夋柟缁戝畾琛")
@Schema(description = "鐢ㄦ埛涓夋柟缁戝畾琛")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_third_party")
public class SysUserThirdParty extends SysBase implements Serializable {

    @TableId(value = "sys_user_third_party_id", type = IdType.AUTO)
    @ApiModelProperty(value = "涓婚敭ID")
    @Schema(description = "涓婚敭ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysUserThirdPartyId;

    /**
     * 涓夋柟绫诲瀷. gitee
     */
    @TableField(value = "sys_user_third_party_type")
    @ApiModelProperty(value = "涓夋柟绫诲瀷. gitee")
    @Schema(description = "涓夋柟绫诲瀷. gitee")
    @Size(max = 255, message = "涓夋柟绫诲瀷鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserThirdPartyType;

    /**
     * 涓夋柟淇℃伅鍚嶇О
     */
    @TableField(value = "sys_user_third_party_name")
    @ApiModelProperty(value = "涓夋柟淇℃伅鍚嶇О")
    @Schema(description = "涓夋柟淇℃伅鍚嶇О")
    @Size(max = 255, message = "涓夋柟淇℃伅鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserThirdPartyName;

    /**
     * 涓夋柟淇℃伅
     */
    @TableField(value = "sys_user_third_party_value")
    @ApiModelProperty(value = "涓夋柟淇℃伅")
    @Schema(description = "涓夋柟淇℃伅")
    @Size(max = 255, message = "涓夋柟淇℃伅鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserThirdPartyValue;

    /**
     * 褰撳墠绯荤粺鐢ㄦ埛ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "褰撳墠绯荤粺鐢ㄦ埛ID")
    @Schema(description = "褰撳墠绯荤粺鐢ㄦ埛ID")
    private Integer sysUserId;

    /**
     * 涓夋柟鐢ㄦ埛ID
     */
    @TableField(value = "sys_user_third_party_uid")
    @ApiModelProperty(value = "涓夋柟鐢ㄦ埛ID")
    @Schema(description = "涓夋柟鐢ㄦ埛ID")
    @Size(max = 255, message = "涓夋柟鐢ㄦ埛ID鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserThirdPartyUid;

    /**
     * 涓夋柟绫诲瀷鏋氫妇
     */
    public static enum Type {
        /**
         * Gitee
         */
        GITEE,
        /**
         * 寰俊灏忕▼搴"         */
        WX_MINI_APP
    }
}