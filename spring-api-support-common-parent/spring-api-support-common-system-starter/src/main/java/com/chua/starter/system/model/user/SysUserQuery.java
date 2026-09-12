package com.chua.starter.system.model.user;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询
 * @since 2024/8/7
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询")
public class SysUserQuery extends Query<SysUser> {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    /** 用户名 */
    private String username;

    /**
    * 昵称
     */
    @Schema(description = "昵称")
    /** Nickname */
    private String nickname;

    /**
    * 手机号
     */
    @Schema(description = "手机号")
    /** Phone */
    private String phone;

    /**
    * 性别; 0:女;1:男; 2:其他
     */
    @Schema(description = "性别; 0:女;1:男; 2:其他")
    /** SEX */
    private Integer sex;

    /**
    * 是否启用；0：禁用
     */
    @Schema(description = "是否启用；0：禁用")
    /** 状态 */
    private Integer status;
    /**
    * 部门标识
     */
    @Schema(description = "部门id")
    /** sysdeptid */
    private Integer sysDeptId;
}
