package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户导入VO
 *
 * @since 2024/12/15
 * @author CH
 */
@Data
@Schema(description = "用户导入VO")
public class SysUserImportVO {

    @Schema(description = "用户名", example = "zhangsan")
    /** SYS用户名称 */
    private String sysUserName;

    @Schema(description = "昵称", example = "张三")
    /** SYS用户nick名称 */
    private String sysUserNickName;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    /** SYS用户email */
    private String sysUserEmail;

    @Schema(description = "手机号", example = "13800138000")
    /** SYS用户phone */
    private String sysUserPhone;

    @Schema(description = "性别(0:男,1:女)", example = "0")
    /** SYS用户gender */
    private Integer sysUserGender;

    @Schema(description = "部门ID", example = "1")
    /** SYS用户deptid */
    private Long sysUserDeptId;

    @Schema(description = "角色编码(多个用逗号分隔)", example = "admin,user")
    /** Rolenames */
    private String roleNames;

    @Schema(description = "状态(0:禁用,1:启用)", example = "1")
    /** SYS用户状态 */
    private Integer sysUserStatus;

    @Schema(description = "备注", example = "备注信息")
    /** SYS用户评论 */
    private String sysUserRemark;
}
