package com.chua.starter.monitor.controller.file;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.FileSystemGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件系统分组操作控制器
 * 负责分组的高级操作，如移动、迁移等
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@RestController
@RequestMapping("/v1/filesystem/group/operation")
@RequiredArgsConstructor
@Validated
@Api(tags = "文件系统分组操作管理")
@Tag(name = "文件系统分组操作管理", description = "文件系统分组的高级操作，如移动、迁移等")
public class FileSystemGroupOperationController {

    private final FileSystemGroupService fileSystemGroupService;

    /**
     * 移动分组
     *
     * @param groupId     分组ID
     * @param newParentId 新父分组ID
     * @return 移动结果
     */
    @PutMapping("/{groupId}/move")
    @ApiOperation("移动分组")
    @Operation(summary = "移动分组", description = "将分组移动到新的父分组下")
    public ReturnResult<Boolean> moveGroup(
            @PathVariable @NotNull @ApiParam("分组ID") @Parameter(description = "分组ID") Integer groupId,
            @RequestParam(required = false) @ApiParam("新父分组ID") @Parameter(description = "新父分组ID") Integer newParentId) {
        try {
            boolean moved = fileSystemGroupService.moveGroup(groupId, newParentId);
            return ReturnResult.ok(moved);
        } catch (Exception e) {
            log.error("移动分组失败: groupId={}, newParentId={}", groupId, newParentId, e);
            return ReturnResult.error("移动分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取分组及其子分组的ID列表
     *
     * @param groupId 分组ID
     * @return 分组ID列表
     */
    @GetMapping("/{groupId}/children-ids")
    @ApiOperation("获取分组及其子分组的ID列表")
    @Operation(summary = "获取分组及其子分组的ID列表", description = "获取指定分组及其所有子分组的ID列表")
    public ReturnResult<List<Integer>> getGroupAndChildrenIds(
            @PathVariable @NotNull @ApiParam("分组ID") @Parameter(description = "分组ID") Integer groupId) {
        try {
            List<Integer> ids = fileSystemGroupService.getGroupAndChildrenIds(groupId);
            return ReturnResult.ok(ids);
        } catch (Exception e) {
            log.error("获取分组及其子分组ID列表失败: groupId={}", groupId, e);
            return ReturnResult.error("获取分组及其子分组ID列表失败: " + e.getMessage());
        }
    }

    /**
     * 迁移文件到指定分组
     *
     * @param fileIds       文件ID列表
     * @param targetGroupId 目标分组ID
     * @return 迁移结果
     */
    @PutMapping("/migrate-files")
    @ApiOperation("迁移文件到指定分组")
    @Operation(summary = "迁移文件到指定分组", description = "将指定的文件迁移到目标分组")
    public ReturnResult<Integer> migrateFilesToGroup(
            @RequestParam @NotEmpty @ApiParam("文件ID列表") @Parameter(description = "文件ID列表") List<Integer> fileIds,
            @RequestParam @NotNull @ApiParam("目标分组ID") @Parameter(description = "目标分组ID") Integer targetGroupId) {
        try {
            int migratedCount = fileSystemGroupService.migrateFilesToGroup(fileIds, targetGroupId);
            return ReturnResult.ok(migratedCount);
        } catch (Exception e) {
            log.error("迁移文件到分组失败: fileIds={}, targetGroupId={}", fileIds, targetGroupId, e);
            return ReturnResult.error("迁移文件到分组失败: " + e.getMessage());
        }
    }

    /**
     * 批量迁移文件到指定分组（通过POST请求体传递大量文件ID）
     *
     * @param request 迁移请求
     * @return 迁移结果
     */
    @PostMapping("/migrate-files-batch")
    @ApiOperation("批量迁移文件到指定分组")
    @Operation(summary = "批量迁移文件到指定分组", description = "批量将文件迁移到目标分组，适用于大量文件的迁移")
    public ReturnResult<Integer> migrateFilesBatch(
            @RequestBody @ApiParam("迁移请求") @Parameter(description = "迁移请求") MigrateFilesRequest request) {
        try {
            if (request.getFileIds() == null || request.getFileIds().isEmpty()) {
                return ReturnResult.error("文件ID列表不能为空");
            }
            if (request.getTargetGroupId() == null) {
                return ReturnResult.error("目标分组ID不能为空");
            }

            int migratedCount = fileSystemGroupService.migrateFilesToGroup(request.getFileIds(), request.getTargetGroupId());
            return ReturnResult.ok(migratedCount);
        } catch (Exception e) {
            log.error("批量迁移文件到分组失败: request={}", request, e);
            return ReturnResult.error("批量迁移文件到分组失败: " + e.getMessage());
        }
    }

    /**
     * 更新分组的树链路路径
     *
     * @param groupId 分组ID
     * @return 更新结果
     */
    @PutMapping("/{groupId}/update-tree-path")
    @ApiOperation("更新分组的树链路路径")
    @Operation(summary = "更新分组的树链路路径", description = "重新计算并更新分组的树链路路径")
    public ReturnResult<String> updateTreePath(
            @PathVariable @NotNull @ApiParam("分组ID") @Parameter(description = "分组ID") Integer groupId) {
        try {
            fileSystemGroupService.updateTreePath(groupId);
            return ReturnResult.ok("更新树链路路径成功");
        } catch (Exception e) {
            log.error("更新分组树链路路径失败: groupId={}", groupId, e);
            return ReturnResult.error("更新分组树链路路径失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新子分组的树链路路径
     *
     * @param parentId 父分组ID
     * @return 更新结果
     */
    @PutMapping("/{parentId}/update-children-tree-path")
    @ApiOperation("批量更新子分组的树链路路径")
    @Operation(summary = "批量更新子分组的树链路路径", description = "重新计算并更新指定分组下所有子分组的树链路路径")
    public ReturnResult<String> updateChildrenTreePath(
            @PathVariable @NotNull @ApiParam("父分组ID") @Parameter(description = "父分组ID") Integer parentId) {
        try {
            fileSystemGroupService.updateChildrenTreePath(parentId);
            return ReturnResult.ok("更新子分组树链路路径成功");
        } catch (Exception e) {
            log.error("更新子分组树链路路径失败: parentId={}", parentId, e);
            return ReturnResult.error("更新子分组树链路路径失败: " + e.getMessage());
        }
    }

    /**
     * 迁移文件请求DTO
     */
    public static class MigrateFilesRequest {
        @NotEmpty(message = "文件ID列表不能为空")
        private List<Integer> fileIds;

        @NotNull(message = "目标分组ID不能为空")
        private Integer targetGroupId;

        public List<Integer> getFileIds() {
            return fileIds;
        }

        public void setFileIds(List<Integer> fileIds) {
            this.fileIds = fileIds;
        }

        public Integer getTargetGroupId() {
            return targetGroupId;
        }

        public void setTargetGroupId(Integer targetGroupId) {
            this.targetGroupId = targetGroupId;
        }

        @Override
        public String toString() {
            return "MigrateFilesRequest{" +
                    "fileIds=" + fileIds +
                    ", targetGroupId=" + targetGroupId +
                    '}';
        }
    }
}
