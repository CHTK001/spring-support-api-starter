package com.chua.starter.monitor.controller.docker;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRecord;
import com.chua.starter.monitor.service.docker.SystemSoftRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统软件操作记录控制器
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/15
 */
@Slf4j
@RestController
@RequestMapping("/api/system/soft/record")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统软件操作记录管理", description = "系统软件操作记录管理相关接口")
public class SystemSoftRecordController {

    private final SystemSoftRecordService systemSoftRecordService;

    /**
     * 分页查询操作记录列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param softId 软件ID
     * @param serverId 服务器ID
     * @param operationType 操作类型
     * @param operationStatus 操作状态
     * @param operationUser 操作用户
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询操作记录列表", description = "根据条件分页查询系统软件操作记录列表")
    public ReturnResult<IPage<SystemSoftRecord>> getRecordPage(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "操作状态") @RequestParam(required = false) Integer operationStatus,
            @Parameter(description = "操作用户") @RequestParam(required = false) String operationUser) {

        log.info("分页查询操作记录列表 - 页码: {}, 每页大小: {}, 软件ID: {}, 服务器ID: {}, 操作类型: {}, 操作状态: {}, 操作用户: {}",
                current, size, softId, serverId, operationType, operationStatus, operationUser);

        try {
            // 创建分页对象
            Page<SystemSoftRecord> page = new Page<>(current, size);

            // 执行分页查询
            IPage<SystemSoftRecord> result = systemSoftRecordService.selectRecordPage(
                    page, softId, serverId, operationType, operationStatus, operationUser);

            log.info("分页查询操作记录列表成功 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("分页查询操作记录列表失败", e);
            return ReturnResult.error("查询操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询操作记录详情
     *
     * @param recordId 记录ID
     * @return 操作记录详情
     */
    @GetMapping("/{recordId}")
    @Operation(summary = "查询操作记录详情", description = "根据记录ID查询操作记录详情")
    public ReturnResult<SystemSoftRecord> getRecordById(
            @Parameter(description = "记录ID", required = true) @PathVariable Integer recordId) {

        log.info("查询操作记录详情 - 记录ID: {}", recordId);

        try {
            SystemSoftRecord record = systemSoftRecordService.getById(recordId);
            if (record == null) {
                log.warn("操作记录不存在 - 记录ID: {}", recordId);
                return ReturnResult.error("操作记录不存在");
            }

            log.info("查询操作记录详情成功 - 记录ID: {}", recordId);
            return ReturnResult.ok(record);

        } catch (Exception e) {
            log.error("查询操作记录详情失败 - 记录ID: {}", recordId, e);
            return ReturnResult.error("查询操作记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据软件ID查询操作记录列表
     *
     * @param softId 软件ID
     * @return 操作记录列表
     */
    @GetMapping("/soft/{softId}")
    @Operation(summary = "根据软件ID查询操作记录", description = "根据软件ID查询相关的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getRecordsBySoftId(
            @Parameter(description = "软件ID", required = true) @PathVariable Integer softId) {

        log.info("根据软件ID查询操作记录列表 - 软件ID: {}", softId);

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectBySoftId(softId);
            log.info("根据软件ID查询操作记录列表成功 - 软件ID: {}, 记录数: {}", softId, records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("根据软件ID查询操作记录列表失败 - 软件ID: {}", softId, e);
            return ReturnResult.error("查询操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据服务器ID查询操作记录列表
     *
     * @param serverId 服务器ID
     * @return 操作记录列表
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "根据服务器ID查询操作记录", description = "根据服务器ID查询相关的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getRecordsByServerId(
            @Parameter(description = "服务器ID", required = true) @PathVariable Integer serverId) {

        log.info("根据服务器ID查询操作记录列表 - 服务器ID: {}", serverId);

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectByServerId(serverId);
            log.info("根据服务器ID查询操作记录列表成功 - 服务器ID: {}, 记录数: {}", serverId, records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("根据服务器ID查询操作记录列表失败 - 服务器ID: {}", serverId, e);
            return ReturnResult.error("查询操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据操作类型查询操作记录列表
     *
     * @param operationType 操作类型
     * @return 操作记录列表
     */
    @GetMapping("/type/{operationType}")
    @Operation(summary = "根据操作类型查询操作记录", description = "根据操作类型查询相关的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getRecordsByOperationType(
            @Parameter(description = "操作类型", required = true) @PathVariable String operationType) {

        log.info("根据操作类型查询操作记录列表 - 操作类型: {}", operationType);

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectByOperationType(operationType);
            log.info("根据操作类型查询操作记录列表成功 - 操作类型: {}, 记录数: {}", operationType, records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("根据操作类型查询操作记录列表失败 - 操作类型: {}", operationType, e);
            return ReturnResult.error("查询操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据操作状态查询操作记录列表
     *
     * @param operationStatus 操作状态
     * @return 操作记录列表
     */
    @GetMapping("/status/{operationStatus}")
    @Operation(summary = "根据操作状态查询操作记录", description = "根据操作状态查询相关的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getRecordsByOperationStatus(
            @Parameter(description = "操作状态", required = true) @PathVariable Integer operationStatus) {

        log.info("根据操作状态查询操作记录列表 - 操作状态: {}", operationStatus);

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectByOperationStatus(operationStatus);
            log.info("根据操作状态查询操作记录列表成功 - 操作状态: {}, 记录数: {}", operationStatus, records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("根据操作状态查询操作记录列表失败 - 操作状态: {}", operationStatus, e);
            return ReturnResult.error("查询操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取失败的操作记录列表
     *
     * @return 失败的操作记录列表
     */
    @GetMapping("/failed")
    @Operation(summary = "获取失败的操作记录", description = "获取所有失败的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getFailedRecords() {

        log.info("获取失败的操作记录列表");

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectFailedRecords();
            log.info("获取失败的操作记录列表成功 - 记录数: {}", records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("获取失败的操作记录列表失败", e);
            return ReturnResult.error("获取失败的操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取成功的操作记录列表
     *
     * @return 成功的操作记录列表
     */
    @GetMapping("/success")
    @Operation(summary = "获取成功的操作记录", description = "获取所有成功的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getSuccessRecords() {

        log.info("获取成功的操作记录列表");

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectSuccessRecords();
            log.info("获取成功的操作记录列表成功 - 记录数: {}", records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("获取成功的操作记录列表失败", e);
            return ReturnResult.error("获取成功的操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取进行中的操作记录列表
     *
     * @return 进行中的操作记录列表
     */
    @GetMapping("/running")
    @Operation(summary = "获取进行中的操作记录", description = "获取所有进行中的操作记录列表")
    public ReturnResult<List<SystemSoftRecord>> getRunningRecords() {

        log.info("获取进行中的操作记录列表");

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.selectRunningRecords();
            log.info("获取进行中的操作记录列表成功 - 记录数: {}", records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("获取进行中的操作记录列表失败", e);
            return ReturnResult.error("获取进行中的操作记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据关键字搜索操作记录
     *
     * @param keyword 关键字
     * @return 操作记录列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索操作记录", description = "根据关键字搜索操作记录")
    public ReturnResult<List<SystemSoftRecord>> searchRecords(
            @Parameter(description = "搜索关键字", required = true) @RequestParam String keyword) {

        log.info("根据关键字搜索操作记录 - 关键字: {}", keyword);

        try {
            List<SystemSoftRecord> records = systemSoftRecordService.searchByKeyword(keyword);
            log.info("根据关键字搜索操作记录成功 - 关键字: {}, 记录数: {}", keyword, records.size());
            return ReturnResult.ok(records);

        } catch (Exception e) {
            log.error("根据关键字搜索操作记录失败 - 关键字: {}", keyword, e);
            return ReturnResult.error("搜索操作记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作记录统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取操作记录统计信息", description = "获取操作记录的统计信息")
    public ReturnResult<Map<String, Object>> getStatistics() {

        log.info("获取操作记录统计信息");

        try {
            // 获取各种统计数据
            long totalCount = systemSoftRecordService.count();
            List<SystemSoftRecord> failedRecords = systemSoftRecordService.selectFailedRecords();
            List<SystemSoftRecord> successRecords = systemSoftRecordService.selectSuccessRecords();
            List<SystemSoftRecord> runningRecords = systemSoftRecordService.selectRunningRecords();

            // 构建统计结果
            Map<String, Object> statistics = Map.of(
                    "totalCount", totalCount,
                    "failedCount", failedRecords.size(),
                    "successCount", successRecords.size(),
                    "runningCount", runningRecords.size()
            );

            log.info("获取操作记录统计信息成功 - 总数: {}, 失败: {}, 成功: {}, 进行中: {}",
                    totalCount, failedRecords.size(), successRecords.size(), runningRecords.size());
            return ReturnResult.ok(statistics);

        } catch (Exception e) {
            log.error("获取操作记录统计信息失败", e);
            return ReturnResult.error("获取操作记录统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除操作记录
     *
     * @param recordId 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{recordId}")
    @Operation(summary = "删除操作记录", description = "根据记录ID删除操作记录")
    public ReturnResult<Boolean> deleteRecord(
            @Parameter(description = "记录ID", required = true) @PathVariable Integer recordId) {

        log.info("删除操作记录 - 记录ID: {}", recordId);

        try {
            boolean result = systemSoftRecordService.removeById(recordId);
            if (result) {
                log.info("删除操作记录成功 - 记录ID: {}", recordId);
                return ReturnResult.ok(true);
            } else {
                log.warn("删除操作记录失败，记录不存在 - 记录ID: {}", recordId);
                return ReturnResult.error("操作记录不存在");
            }

        } catch (Exception e) {
            log.error("删除操作记录失败 - 记录ID: {}", recordId, e);
            return ReturnResult.error("删除操作记录失败: " + e.getMessage());
        }
    }
}