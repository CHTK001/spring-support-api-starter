package com.chua.starter.system.pojo.dept;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 部门VO
 *
 * @param sysDeptId                      部门标识
 * @param sysDeptName                    部门名称
 * @param sysDeptCode                    部门编码
 * @param sysDeptPid                     父部门ID
 * @param sysDeptTreeId                  部门链路
 * @param sysDeptStatus                  状态
 * @param sysDeptPrincipal               部门负责人
 * @param principalUserIds               负责人用户ID集合
 * @param principalUserNames             负责人名称集合
 * @param memberCount                    部门人数
 * @param sysDeptContact                 联系方式
 * @param sysDeptSort                    排序
 * @param sysDeptRemark                  备注
 * @param sysDeptDataPermission          数据权限类型
 * @param sysDeptDataPermissionDeptId    数据权限可见部门ID
 * @param sysDeptDataPermissionDeptIds   自定义数据权限可见部门ID列表
 * @param children                       子部门
 * @since 2024/8/20
 * @author CH
 */
@Schema(description = "部门VO")
public record SysDeptVO(
        @Schema(description = "部门ID") Integer sysDeptId,
        @Schema(description = "部门名称") String sysDeptName,
        @Schema(description = "部门编码") String sysDeptCode,
        @Schema(description = "父部门ID") Integer sysDeptPid,
        @Schema(description = "部门链路") String sysDeptTreeId,
        @Schema(description = "状态") Integer sysDeptStatus,
        @Schema(description = "部门负责人") String sysDeptPrincipal,
        @Schema(description = "负责人用户ID集合") List<Integer> principalUserIds,
        @Schema(description = "负责人名称集合") List<String> principalUserNames,
        @Schema(description = "部门人数") Integer memberCount,
        @Schema(description = "联系方式") String sysDeptContact,
        @Schema(description = "排序") Integer sysDeptSort,
        @Schema(description = "备注") String sysDeptRemark,
        @Schema(description = "数据权限类型") Integer sysDeptDataPermission,
        @Schema(description = "数据权限可见部门ID") String sysDeptDataPermissionDeptId,
        @Schema(description = "自定义数据权限可见部门ID列表") String sysDeptDataPermissionDeptIds,
        @Schema(description = "子部门") List<SysDeptVO> children) {

    /**
     * 返回挂载子部门后的新实例。
     *
     * @param children 子部门
     * @return 新实例
     */
    public SysDeptVO withChildren(List<SysDeptVO> children) {
        return new SysDeptVO(sysDeptId, sysDeptName, sysDeptCode, sysDeptPid, sysDeptTreeId, sysDeptStatus,
                sysDeptPrincipal, principalUserIds, principalUserNames, memberCount, sysDeptContact,
                sysDeptSort, sysDeptRemark, sysDeptDataPermission, sysDeptDataPermissionDeptId,
                sysDeptDataPermissionDeptIds, children);
    }
}
