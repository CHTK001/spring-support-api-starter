package com.chua.starter.service.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.storage.oss.result.PutObjectResult;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.common.support.base.validator.group.UploadGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemGroup;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemGroupService;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import com.chua.starter.service.pojo.sso.*;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件系统管理接口
 * 提供文件上传、分片上传、配置管理、分组管理等功能
 *
 * @author CH
 * @since 2024/12/4
 * @version 1.0.0
 */
@RestController
@Slf4j
@Api(tags = "文件系统管理接口")
@Tag(name = "文件系统管理接口")
@RequestMapping("/v2/file")
@RequiredArgsConstructor
public class SysFileSystemController {

    private final SysFileSystemService sysFileSystemService;
    private final SysFileSystemSettingService sysFileSystemSettingService;
    private final SysFileSystemGroupService sysFileSystemGroupService;

    /**
     * 上传文件
     * @param request 请求
     * @param file 文件
     * @return 结果
     */
    @Operation(summary = "上传文件")
    @PutMapping("/upload")
    @SysLog
    public ReturnResult<PutObjectResult> upload(@ParameterObject @Validated(UploadGroup.class) SysFileUploadRequest request,
                                                @RequestPart @RequestParam("file") MultipartFile file,
                                                BindingResult bindingResult
                                          ) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        if(null == file) {
            return ReturnResult.illegal("文件不能为空");
        }
        return sysFileSystemService.uploadForFileSystem(request, file);
    }


    /**
     * 秒传检查
     * 上传前检查文件是否已存在，如果存在则直接返回文件URL
     *
     * @param request 秒传检查请求
     * @return 秒传检查结果
     */
    @Operation(summary = "秒传检查")
    @PostMapping("/instant-check")
    public ReturnResult<SysFileInstantUploadResponse> checkInstantUpload(
            @Validated @RequestBody SysFileInstantUploadRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysFileSystemService.checkInstantUploadForFileSystem(request);
    }

    /**
     * 创建分片任务
     * @param request 请求
     * @return 结果
     */
    @Operation(summary = "创建分片任务")
    @PostMapping("/task/create")
    @SysLog
    public ReturnResult<SysFileUploadTaskResponse> createTask(@Validated(UploadGroup.class) @RequestBody SysFileUploadTaskRequest request,
                                                              BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysFileSystemService.createTaskForFileSystem(request);
    }
    /**
     * 上传分片
     * @param request 请求
     * @return 结果
     */
    @Operation(summary = "上传分片任务")
    @PostMapping("/task/upload")
    @SysLog
    public ReturnResult<SysFileUploadTaskPartResponse> uploadTask(@ParameterObject @Validated(UploadGroup.class) SysFileUploadTaskPartRequest request,
                                                                  @RequestPart@RequestParam("file") @Parameter(name= "file", description = "文件") MultipartFile multipartFile,
                                                                  BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        if(null == multipartFile) {
            return ReturnResult.illegal("文件不能为空");
        }
        return sysFileSystemService.uploadTaskForFileSystem(request, multipartFile);
    }

    /**
     * 获取上传任务信息（断点续传）
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    @Operation(summary = "获取上传任务信息（断点续传）")
    @GetMapping("/task/info/{taskId}")
    public ReturnResult<SysFileTaskInfoResponse> getTaskInfo(@PathVariable String taskId) {
        return sysFileSystemService.getTaskInfoForFileSystem(taskId);
    }

    /**
     * 根据MD5查找未完成的上传任务（断点续传）
     *
     * @param fileMd5  文件MD5
     * @param fileSize 文件大小
     * @return 任务信息
     */
    @Operation(summary = "根据MD5查找未完成的上传任务（断点续传）")
    @GetMapping("/task/resume")
    public ReturnResult<SysFileTaskInfoResponse> findTaskByMd5(
            @RequestParam String fileMd5,
            @RequestParam(required = false) Long fileSize) {
        return sysFileSystemService.findTaskByMd5ForFileSystem(fileMd5, fileSize);
    }

    // ==================== 配置管理接口 ====================

    /**
     * 获取文件系统配置
     *
     * @return 当前启用的配置
     */
    @Operation(summary = "获取文件系统配置")
    @GetMapping("/setting")
    public ReturnResult<SysFileSystemSetting> getSettingForFileSystem() {
        SysFileSystemSetting setting = sysFileSystemSettingService.getEnabledSettingForFileSystem();
        if (setting == null) {
            // 返回默认配置
            setting = SysFileSystemSetting.getDefaultSetting();
        }
        return ReturnResult.ok(setting);
    }

    /**
     * 更新文件系统配置
     *
     * @param setting 配置信息
     * @return 更新结果
     */
    @Operation(summary = "更新文件系统配置")
    @PutMapping("/setting")
    @Permission(role = {"admin", "superadmin"})
    @SysLog
    public ReturnResult<Boolean> updateSettingForFileSystem(@RequestBody SysFileSystemSetting setting) {
        boolean result;
        if (setting.getSysFileSystemSettingId() == null) {
            result = sysFileSystemSettingService.save(setting);
        } else {
            result = sysFileSystemSettingService.updateById(setting);
        }
        return result ? ReturnResult.ok(true) : ReturnResult.error("更新配置失败");
    }

    /**
     * 启动文件服务器
     *
     * @return 操作结果
     */
    @Operation(summary = "启动文件服务器")
    @PostMapping("/server/start")
    @Permission(role = {"admin", "superadmin"})
    @SysLog
    public ReturnResult<Boolean> startServerForFileSystem() {
        return sysFileSystemSettingService.startFileServerForFileSystem();
    }

    /**
     * 停止文件服务器
     *
     * @return 操作结果
     */
    @Operation(summary = "停止文件服务器")
    @PostMapping("/server/stop")
    @Permission(role = {"admin", "superadmin"})
    @SysLog
    public ReturnResult<Boolean> stopServerForFileSystem() {
        return sysFileSystemSettingService.stopFileServerForFileSystem();
    }

    /**
     * 获取文件服务器状态
     *
     * @return 服务器状态（running/stopped）
     */
    @Operation(summary = "获取文件服务器状态")
    @GetMapping("/server/status")
    public ReturnResult<String> getServerStatusForFileSystem() {
        return sysFileSystemSettingService.getFileServerStatusForFileSystem();
    }

    /**
     * 检查端口是否可用
     *
     * @param port 端口号
     * @return 是否可用
     */
    @Operation(summary = "检查端口是否可用")
    @GetMapping("/server/check-port")
    public ReturnResult<Boolean> checkPortForFileSystem(@RequestParam Integer port) {
        return sysFileSystemSettingService.checkPortAvailableForFileSystem(port);
    }

    // ==================== 统计接口 ====================

    /**
     * 获取文件统计信息
     *
     * @return 统计数据
     */
    @Operation(summary = "获取文件统计信息")
    @GetMapping("/stats")
    public ReturnResult<Map<String, Object>> getStatsForFileSystem() {
        Map<String, Object> stats = new HashMap<>();
        
        // 文件总数
        long totalCount = sysFileSystemService.count();
        stats.put("totalCount", totalCount);
        
        // 待合并数量
        long pendingMergeCount = sysFileSystemService.count(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.PENDING_MERGE));
        stats.put("pendingMergeCount", pendingMergeCount);
        
        // 上传失败数量
        long errorCount = sysFileSystemService.count(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.ERROR));
        stats.put("errorCount", errorCount);
        
        // 已完成数量
        long completedCount = sysFileSystemService.count(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.COMPLETED));
        stats.put("completedCount", completedCount);
        
        // 上传中数量
        long uploadingCount = sysFileSystemService.count(Wrappers.<SysFileSystem>lambdaQuery()
                .in(SysFileSystem::getSysFileSystemStatus, 
                    SysFileSystem.Status.PENDING, 
                    SysFileSystem.Status.UPLOADING,
                    SysFileSystem.Status.MERGING));
        stats.put("uploadingCount", uploadingCount);
        
        return ReturnResult.ok(stats);
    }

    // ==================== 文件列表接口 ====================

    /**
     * 分页查询文件列表
     *
     * @param page     页码
     * @param size     每页数量
     * @param groupId  分组ID
     * @param status   状态
     * @param keyword  关键词
     * @return 文件列表
     */
    @Operation(summary = "分页查询文件列表")
    @GetMapping("/list")
    public ReturnResult<IPage<SysFileSystem>> listForFileSystem(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Page<SysFileSystem> pageParam = new Page<>(page, size);
        IPage<SysFileSystem> result = sysFileSystemService.page(pageParam, Wrappers.<SysFileSystem>lambdaQuery()
                .eq(groupId != null, SysFileSystem::getSysFileSystemGroupId, groupId)
                .eq(status != null, SysFileSystem::getSysFileSystemStatus, status)
                .and(keyword != null && !keyword.isEmpty(), wrapper -> wrapper
                        .like(SysFileSystem::getSysFileSystemName, keyword)
                        .or()
                        .like(SysFileSystem::getSysFileSystemOriginalName, keyword))
                .orderByDesc(SysFileSystem::getCreateTime));
        
        return ReturnResult.ok(result);
    }

    /**
     * 手动合并文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    @Operation(summary = "手动合并文件")
    @PostMapping("/merge/{fileId}")
    @SysLog
    public ReturnResult<Boolean> mergeForFileSystem(@PathVariable Integer fileId) {
        SysFileSystem file = sysFileSystemService.getById(fileId);
        if (file == null) {
            return ReturnResult.error("文件不存在");
        }
        if (file.getSysFileSystemStatus() != SysFileSystem.Status.PENDING_MERGE) {
            return ReturnResult.error("文件状态不允许合并");
        }
        // 触发合并任务
        file.setSysFileSystemStatus(SysFileSystem.Status.PENDING_MERGE);
        sysFileSystemService.updateById(file);
        return ReturnResult.ok(true);
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    @SysLog
    public ReturnResult<Boolean> deleteForFileSystem(@PathVariable Integer fileId) {
        boolean result = sysFileSystemService.removeById(fileId);
        return result ? ReturnResult.ok(true) : ReturnResult.error("删除失败");
    }

    // ==================== 分组管理接口 ====================

    /**
     * 获取分组树
     *
     * @return 分组树列表
     */
    @Operation(summary = "获取分组树")
    @GetMapping("/group/tree")
    public ReturnResult<List<SysFileSystemGroup>> getGroupTreeForFileSystem() {
        List<SysFileSystemGroup> tree = sysFileSystemGroupService.getTreeForFileSystem();
        return ReturnResult.ok(tree);
    }

    /**
     * 获取分组列表
     *
     * @return 分组列表
     */
    @Operation(summary = "获取分组列表")
    @GetMapping("/group/list")
    public ReturnResult<List<SysFileSystemGroup>> getGroupListForFileSystem() {
        List<SysFileSystemGroup> list = sysFileSystemGroupService.list(
                Wrappers.<SysFileSystemGroup>lambdaQuery()
                        .eq(SysFileSystemGroup::getSysFileSystemGroupStatus, SysFileSystemGroup.Status.ENABLED)
                        .orderByAsc(SysFileSystemGroup::getSysFileSystemGroupSort));
        return ReturnResult.ok(list);
    }

    /**
     * 新增分组
     *
     * @param group 分组信息
     * @return 操作结果
     */
    @Operation(summary = "新增分组")
    @PostMapping("/group")
    @SysLog
    public ReturnResult<SysFileSystemGroup> addGroupForFileSystem(
            @Validated(AddGroup.class) @RequestBody SysFileSystemGroup group) {
        boolean result = sysFileSystemGroupService.save(group);
        return result ? ReturnResult.ok(group) : ReturnResult.error("新增分组失败");
    }

    /**
     * 更新分组
     *
     * @param group 分组信息
     * @return 操作结果
     */
    @Operation(summary = "更新分组")
    @PutMapping("/group")
    @SysLog
    public ReturnResult<Boolean> updateGroupForFileSystem(
            @Validated(UpdateGroup.class) @RequestBody SysFileSystemGroup group) {
        boolean result = sysFileSystemGroupService.updateById(group);
        return result ? ReturnResult.ok(true) : ReturnResult.error("更新分组失败");
    }

    /**
     * 删除分组
     *
     * @param groupId 分组ID
     * @return 操作结果
     */
    @Operation(summary = "删除分组")
    @DeleteMapping("/group/{groupId}")
    @SysLog
    public ReturnResult<Boolean> deleteGroupForFileSystem(@PathVariable Integer groupId) {
        // 检查分组下是否有文件
        long fileCount = sysFileSystemService.count(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemGroupId, groupId));
        if (fileCount > 0) {
            return ReturnResult.error("分组下存在文件，无法删除");
        }
        boolean result = sysFileSystemGroupService.removeById(groupId);
        return result ? ReturnResult.ok(true) : ReturnResult.error("删除分组失败");
    }

    /**
     * 初始化默认分组
     *
     * @return 操作结果
     */
    @Operation(summary = "初始化默认分组")
    @PostMapping("/group/init")
    @Permission(role = {"admin", "superadmin"})
    @SysLog
    public ReturnResult<Boolean> initDefaultGroupsForFileSystem() {
        return sysFileSystemGroupService.initDefaultGroupsForFileSystem();
    }
}
