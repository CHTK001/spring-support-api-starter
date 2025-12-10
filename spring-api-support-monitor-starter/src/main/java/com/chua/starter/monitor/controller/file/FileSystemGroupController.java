package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.FileSystemGroup;
import com.chua.starter.monitor.service.FileSystemGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 文件系统分组基础管理控制器
 * 负责分组的基本CRUD操作
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@RestController
@RequestMapping("/v1/filesystem/group")
@RequiredArgsConstructor
@Validated
@Api(tags = "文件系统分组基础管理")
@Tag(name = "文件系统分组基础管理", description = "文件系统分组的基本增删改查操作")
public class FileSystemGroupController {

    private final FileSystemGroupService fileSystemGroupService;

    /**
     * 获取分组树形结构
     *
     * @return 分组树形结构
     */
    @GetMapping("/tree")
    @ApiOperation("获取分组树形结构")
    @Operation(summary = "获取分组树形结构", description = "获取文件系统分组的树形结构，包含文件数量统计")
    public ReturnResult<List<FileSystemGroup>> getGroupTree() {
        try {
            List<FileSystemGroup> tree = fileSystemGroupService.getGroupTree();
            return ReturnResult.ok(tree);
        } catch (Exception e) {
            log.error("获取分组树形结构失败", e);
            return ReturnResult.error("获取分组树形结构失败: " + e.getMessage());
        }
    }

    /**
     * 创建分组
     *
     * @param group 分组信息
     * @return 创建结果
     */
    @PostMapping
    @ApiOperation("创建分组")
    @Operation(summary = "创建分组", description = "创建新的文件系统分组")
    public ReturnResult<FileSystemGroup> createGroup(
            @Valid @RequestBody @ApiParam("分组信息") @Parameter(description = "分组信息") FileSystemGroup group) {
        try {
            FileSystemGroup createdGroup = fileSystemGroupService.createGroup(group);
            return ReturnResult.ok(createdGroup);
        } catch (Exception e) {
            log.error("创建分组失败", e);
            return ReturnResult.error("创建分组失败: " + e.getMessage());
        }
    }

    /**
     * 更新分组
     *
     * @param group 分组信息
     * @return 更新结果
     */
    @PutMapping
    @ApiOperation("更新分组")
    @Operation(summary = "更新分组", description = "更新文件系统分组信息")
    public ReturnResult<FileSystemGroup> updateGroup(
            @Valid @RequestBody @ApiParam("分组信息") @Parameter(description = "分组信息") FileSystemGroup group) {
        try {
            FileSystemGroup updatedGroup = fileSystemGroupService.updateGroup(group);
            return ReturnResult.ok(updatedGroup);
        } catch (Exception e) {
            log.error("更新分组失败", e);
            return ReturnResult.error("更新分组失败: " + e.getMessage());
        }
    }

    /**
     * 删除分组
     *
     * @param groupId 分组ID
     * @return 删除结果
     */
    @DeleteMapping("/{groupId}")
    @ApiOperation("删除分组")
    @Operation(summary = "删除分组", description = "删除文件系统分组（级联删除子分组）")
    public ReturnResult<Boolean> deleteGroup(
            @PathVariable @NotNull @ApiParam("分组ID") @Parameter(description = "分组ID") Integer groupId) {
        try {
            boolean deleted = fileSystemGroupService.deleteGroup(groupId);
            return ReturnResult.ok(deleted);
        } catch (Exception e) {
            log.error("删除分组失败: groupId={}", groupId, e);
            return ReturnResult.error("删除分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取分组详情
     *
     * @param groupId 分组ID
     * @return 分组详情
     */
    @GetMapping("/{groupId}")
    @ApiOperation("获取分组详情")
    @Operation(summary = "获取分组详情", description = "根据ID获取分组详细信息")
    public ReturnResult<FileSystemGroup> getGroupDetail(
            @PathVariable @NotNull @ApiParam("分组ID") @Parameter(description = "分组ID") Integer groupId) {
        try {
            FileSystemGroup group = fileSystemGroupService.getById(groupId);
            if (group == null) {
                return ReturnResult.error("分组不存在");
            }
            return ReturnResult.ok(group);
        } catch (Exception e) {
            log.error("获取分组详情失败: groupId={}", groupId, e);
            return ReturnResult.error("获取分组详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认分组
     *
     * @return 默认分组
     */
    @GetMapping("/default")
    @ApiOperation("获取默认分组")
    @Operation(summary = "获取默认分组", description = "获取或创建默认分组")
    public ReturnResult<FileSystemGroup> getDefaultGroup() {
        try {
            FileSystemGroup defaultGroup = fileSystemGroupService.getOrCreateDefaultGroup();
            return ReturnResult.ok(defaultGroup);
        } catch (Exception e) {
            log.error("获取默认分组失败", e);
            return ReturnResult.error("获取默认分组失败: " + e.getMessage());
        }
    }

    /**
     * 初始化默认分组
     *
     * @return 初始化结果
     */
    @PostMapping("/init-default")
    @ApiOperation("初始化默认分组")
    @Operation(summary = "初始化默认分组", description = "初始化系统默认的文件分组（图片、视频、文档、默认）")
    public ReturnResult<String> initDefaultGroups() {
        try {
            fileSystemGroupService.initDefaultGroups();
            return ReturnResult.ok("初始化默认分组成功");
        } catch (Exception e) {
            log.error("初始化默认分组失败", e);
            return ReturnResult.error("初始化默认分组失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件类型获取推荐分组
     *
     * @param fileExtension 文件扩展名
     * @return 推荐分组ID
     */
    @GetMapping("/recommend")
    @ApiOperation("根据文件类型获取推荐分组")
    @Operation(summary = "根据文件类型获取推荐分组", description = "根据文件扩展名自动推荐合适的分组")
    public ReturnResult<Integer> getRecommendedGroup(
            @RequestParam @ApiParam("文件扩展名") @Parameter(description = "文件扩展名") String fileExtension) {
        try {
            Integer groupId = fileSystemGroupService.getGroupByFileType(fileExtension);
            return ReturnResult.ok(groupId);
        } catch (Exception e) {
            log.error("获取推荐分组失败: fileExtension={}", fileExtension, e);
            return ReturnResult.error("获取推荐分组失败: " + e.getMessage());
        }
    }
}
