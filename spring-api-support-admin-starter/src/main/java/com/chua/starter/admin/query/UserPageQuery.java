package com.chua.starter.admin.query;

import com.chua.starter.admin.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询对象
 *
 * @author haoxr
 * @since 2022/1/14
 */
@EqualsAndHashCode(callSuper = true)
@Schema
@Data
public class UserPageQuery extends BasePageQuery<SysUser> {

    @Schema(description = "关键字(用户名/昵称/手机号)")
    private String keywords;

    @Schema(description = "用户状态")
    private Integer status;

    @Schema(description = "部门ID")
    private Long deptId;

}
