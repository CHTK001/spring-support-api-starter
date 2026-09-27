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
     * 账号，模糊匹配 sys_user_username
     */
    @Schema(description = "账号")
    private String sysUserName;

    /**
     * 昵称，模糊匹配 sys_user_nickname
     */
    @Schema(description = "昵称")
    private String sysUserNickname;

    /**
     * 手机号，模糊匹配 sys_user_phone
     */
    @Schema(description = "手机号")
    private String sysUserPhone;

    /**
     * 性别; 0:女;1:男; 2:其他
     */
    @Schema(description = "性别; 0:女;1:男; 2:其他")
    private Integer sysUserSex;

    /**
     * 是否启用；0：禁用
     */
    @Schema(description = "是否启用；0：禁用")
    private Integer sysUserStatus;

    /**
     * 部门标识
     */
    @Schema(description = "部门id")
    private Integer sysDeptId;

    /**
     * 角色标识，只返回拥有该角色的用户
     */
    @Schema(description = "角色id")
    private Integer sysRoleId;

    /* ================== 数据权限过滤参数（由Service层注入，前端不传） ================== */

    /**
     * 当前用户ID（数据权限=仅本人时使用）
     */
    @Schema(hidden = true)
    private Integer currentUserId;

    /**
     * 当前用户部门ID（数据权限=本部门时使用）
     */
    @Schema(hidden = true)
    private Integer currentDeptId;

    /**
     * 当前用户部门树路径（数据权限=本部门及以下时使用）
     */
    @Schema(hidden = true)
    private String currentDeptTreeId;

    /**
     * 自定义数据权限部门ID列表（数据权限=自定义时使用）
     */
    @Schema(hidden = true)
    private java.util.List<Integer> dataScopeDeptIds;

    /**
     * 数据权限类型: 1-全部 2-本部门 3-本部门及以下 4-仅本人 5-自定义
     */
    @Schema(hidden = true)
    private Integer dataScope;

    /**
     * 是否排除 sa 角色用户（非 sa 用户看不到 sa 用户）
     */
    @Schema(hidden = true)
    private Boolean excludeSaUser;
}
