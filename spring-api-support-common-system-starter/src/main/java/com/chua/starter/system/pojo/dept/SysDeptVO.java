package com.chua.starter.system.pojo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 部门VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "部门VO")
public class SysDeptVO {

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String sysDeptName;

    /**
     * 部门编码
     */
    @Schema(description = "部门编码")
    private String sysDeptCode;

    /**
     * 鐖堕儴闂↖D
     */
    @Schema(description = "鐖堕儴闂↖D")
    private Integer sysDeptPid;

    /**
     * 部门閾捐矾
     */
    @Schema(description = "部门閾捐矾")
    private String sysDeptTreeId;

    /**
     * 状态?     */
    @Schema(description = "状态")
    private Integer sysDeptStatus;

    /**
     * 瀛愰儴闂"     */
    @Schema(description = "瀛愰儴闂")
    private List<SysDeptVO> children;
}