package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户批量操作入参
 * <p>批量启用/禁用与批量删除共用的请求体，避免以 Map 承载业务数据</p>
 *
 * @since 2026/9/19
 * @author CH
 */
@Data
@Schema(description = "用户批量操作入参")
public class SysUserBatchUpdate {

    /**
     * 目标用户主键集合
     */
    @Schema(description = "目标用户主键集合")
    private List<Long> sysUserIds;

    /**
     * 目标状态：1-启用，0-禁用，仅批量更新状态时使用
     */
    @Schema(description = "目标状态：1-启用，0-禁用")
    private Integer sysUserStatus;
}
