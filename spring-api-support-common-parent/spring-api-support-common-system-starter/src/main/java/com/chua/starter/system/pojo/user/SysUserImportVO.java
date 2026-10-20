package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户导入VO
 *
 * @param sysUserName     用户名
 * @param sysUserNickName 昵称
 * @param sysUserEmail    邮箱
 * @param sysUserPhone    手机号
 * @param sysUserGender   性别(0:男,1:女)
 * @param sysUserDeptId   部门ID
 * @param roleNames       角色编码(多个用逗号分隔)
 * @param sysUserStatus   状态(0:禁用,1:启用)
 * @param sysUserRemark   备注
 * @since 2024/12/15
 * @author CH
 */
@Schema(description = "用户导入VO")
public record SysUserImportVO(
        @Schema(description = "用户名", example = "zhangsan") String sysUserName,
        @Schema(description = "昵称", example = "张三") String sysUserNickName,
        @Schema(description = "邮箱", example = "zhangsan@example.com") String sysUserEmail,
        @Schema(description = "手机号", example = "13800138000") String sysUserPhone,
        @Schema(description = "性别(0:男,1:女)", example = "0") Integer sysUserGender,
        @Schema(description = "部门ID", example = "1") Long sysUserDeptId,
        @Schema(description = "角色编码(多个用逗号分隔)", example = "admin,user") String roleNames,
        @Schema(description = "状态(0:禁用,1:启用)", example = "1") Integer sysUserStatus,
        @Schema(description = "备注", example = "备注信息") String sysUserRemark) {
}
