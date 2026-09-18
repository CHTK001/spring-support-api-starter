package com.chua.starter.system.pojo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 部门VO
 *
 * @since 2024/8/20
 * @author CH
 */
@Data
@Schema(description = "部门VO")
public class SysDeptVO {

    /**
     * 部门标识
     */
    @Schema(description = "部门ID")
    /** sysdeptid */
    private Integer sysDeptId;

    /**
    * 部门名称
     */
    @Schema(description = "部门名称")
    /** sysdept名称 */
    private String sysDeptName;

    /**
    * 部门编码
     */
    @Schema(description = "部门编码")
    /** sysdept代码 */
    private String sysDeptCode;

    /**
    * 鐖堕儴闂↖D
     */
    @Schema(description = "鐖堕儴闂↖D")
    /** sysdeptpid */
    private Integer sysDeptPid;

    /**
    * 部门閾捐矾
     */
    @Schema(description = "部门閾捐矾")
    /** sysdepttreeid */
    private String sysDeptTreeId;

    /**
    * 状态?     */
    @Schema(description = "状态")
    /** sysdept状态 */
    private Integer sysDeptStatus;

    @Schema(description = "部门负责人")
    /** sysdeptprincipal */
    private String sysDeptPrincipal;

    @Schema(description = "负责人用户ID集合")
    /** Principal用户标识 */
    private List<Integer> principalUserIds;

    @Schema(description = "负责人名称集合")
    /** Principal用户名称 */
    private List<String> principalUserNames;

    @Schema(description = "部门人数")
    /** Member数量 */
    private Integer memberCount;

    /**
    * 瀛愰儴闂"     */
    @Schema(description = "瀛愰儴闂")
    /** Children */
    private List<SysDeptVO> children;
}
