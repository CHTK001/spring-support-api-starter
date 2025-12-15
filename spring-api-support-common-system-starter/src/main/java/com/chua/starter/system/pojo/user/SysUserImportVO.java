package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户导入VO
 *
 * @author CH
 * @since 2024/12/15
 */
@Data
@Schema(description = "用户导入VO")
public class SysUserImportVO {

    @Schema(description = "用户名", example = "zhangsan")
    private String sysUserName;

    @Schema(description = "昵称", example = "张三")
    private String sysUserNickName;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String sysUserEmail;

    @Schema(description = "手机号", example = "13800138000")
    private String sysUserPhone;

    @Schema(description = "性别(0:男,1:女)", example = "0")
    private Integer sysUserGender;

    @Schema(description = "部门ID", example = "1")
    private Long sysUserDeptId;

    @Schema(description = "角色编码(多个用逗号分隔)", example = "admin,user")
    private String roleNames;

    @Schema(description = "状态(0:禁用,1:启用)", example = "1")
    private Integer sysUserStatus;

    @Schema(description = "备注", example = "备注信息")
    private String sysUserRemark;
}
