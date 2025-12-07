package com.chua.starter.service.query;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询
 * @author CH
 * @since 2024/8/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询")
public class SysUserQuery extends Query<SysUser> {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别; 0:女;1:男; 2:其他
     */
    @Schema(description = "性别; 0:女;1:男; 2:其他")
    private Integer sex;

    /**
     * 是否启用；0：禁用
     */
    @Schema(description = "是否启用；0：禁用")
    private Integer status;
    /**
     * 部门id
     */
    @Schema(description = "部门id")
    private Integer sysDeptId;
}
