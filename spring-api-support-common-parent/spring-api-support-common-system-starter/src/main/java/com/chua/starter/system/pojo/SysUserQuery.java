package com.chua.starter.system.pojo;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询对象
 *
 * @since 2024/8/7
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询对象")
public class SysUserQuery extends Query<SysUser> {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    /**
     * SYS用户名称
    */
    private String sysUserName;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    /**
     * SYS用户phone
    */
    private String sysUserPhone;

    /**
     * 状态
     */
    @Schema(description = "状态")
    /**
     * SYS用户状态
    */
    private Integer sysUserStatus;

    /**
     * 部门标识
     */
    @Schema(description = "部门ID")
    /**
     * sysdeptid
    */
    private Long sysDeptId;

    /**
     * 是否排除sa用户（非sa角色的用户看不到sa）
     */
    @Schema(description = "是否排除sa用户")
    private Boolean excludeSaUser = false;
}

