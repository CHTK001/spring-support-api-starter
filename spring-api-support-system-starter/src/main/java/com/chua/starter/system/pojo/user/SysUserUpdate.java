package com.chua.starter.system.pojo.user;

import com.chua.starter.system.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author CH
 * @since 2024/8/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "用户更新")
public class SysUserUpdate extends SysUser {


    /**
     * 角色ID
     */
    @Schema(title = "角色ID")
    private List<String> roleIds;
}
