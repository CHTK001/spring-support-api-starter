package com.chua.starter.admin.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.starter.common.support.serialize.JsonArrayToLongDeserialize;
import com.chua.starter.common.support.serialize.JsonArrayToStringDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 账号更新
 *
 * @author CH
 */
@Data
@Schema(title = "账号更新")
public class SysUserUpdate {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    @Schema(title = "主键")
    @NotNull(message = "主键不能为空")
    private Long userId;

    /**
     * 用户真实姓名
     */
    @Schema(title = "用户真实姓名")
    private String userRealName;
    /**
     * 住址
     */
    @Schema(title = "住址")
    private String userAddress;

    /**
     * 旧密码
     */
    @Schema(title = "旧密码")
    private String userOldPassword;

    /**
     * 昵称
     */
    @Schema(title = "昵称")
    private String userNickname;

    /**
     * 性别((1:男;2:女))
     */
    @Schema(title = "性别(1:男;2:女)")
    @Column(comment = "性别(1:男;2:女)")
    private Integer userGender;

    /**
     * 部门ID
     */
    @Schema(title = "部门ID")
    @JsonDeserialize(using = JsonArrayToLongDeserialize.class)
    private Long deptId;
    /**
     * 密钥
     */
    @TableField(exist = false)
    @Schema(title = "盐")
    private String userSeRan;
    /**
     * 密码
     */
    @Schema(title = "密码")
    private String userPassword;
    /**
     * 备注
     */
    @Schema(title = "备注")
    private String userMarker;
    /**
     * 用户头像
     */
    @Schema(title = "用户头像")
    private String userAvatar;

    /**
     * 联系方式
     */
    @Schema(title = "联系方式")
    private String userMobile;

    /**
     * 用户状态((1:正常;0:禁用))
     */
    @Schema(title = "用户状态(1:正常;0:禁用)")
    @Column(defaultValue = "1", comment = "用户状态(1:正常;0:禁用)")
    private Integer userStatus;

    /**
     * 用户邮箱
     */
    @Schema(title = "用户邮箱")
    private String userEmail;
    /**
     * 角色ids
     */
    @Schema(title = "角色ids")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String roleId;

}
