package com.chua.starter.system.pojo;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询对象
 *
 * @author CH
 * @since 2024/8/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询对象")
public class SysUserQuery extends Query<SysUser> {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String sysUserName;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String sysUserPhone;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer sysUserStatus;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long sysDeptId;
}
