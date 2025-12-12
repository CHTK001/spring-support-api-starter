package com.chua.starter.monitor.controller.script;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.pojo.request.ScriptExecuteRecordQueryRequest;
import com.chua.starter.monitor.service.script.MonitorSysGenScriptExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 脚本执行记录控制器
 *
 * @author CH
 * @since 2024-12-12
 */
@Slf4j
@Tag(name = "脚本执行记录管理")
@RestController
@RequestMapping("/script/execute-record")
@RequiredArgsConstructor
public class MonitorSysGenScriptExecutionController {

    private final MonitorSysGenScriptExecutionService executionService;

    /**
     * 分页查询执行记录
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Operation(summary = "分页查询执行记录")
    @PostMapping("/page")
    public ReturnResult<Page<MonitorSysGenScriptExecution>> pageForExecuteRecord(
            @RequestBody ScriptExecuteRecordQueryRequest request) {
        log.debug("分页查询脚本执行记录: {}", request);
        try {
            Page<MonitorSysGenScriptExecution> page = executionService.pageForRecord(request);
            return ReturnResult.ok(page);
        } catch (Exception e) {
            log.error("查询脚本执行记录失败", e);
            return ReturnResult.error("查询执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行记录详情
     *
     * @param recordId 记录ID
     * @return 执行记录详情
     */
    @Operation(summary = "获取执行记录详情")
    @GetMapping("/{recordId}")
    public ReturnResult<MonitorSysGenScriptExecution> getDetailForExecuteRecord(
            @Parameter(description = "记录ID") @PathVariable Long recordId) {
        log.debug("获取脚本执行记录详情: recordId={}", recordId);
        try {
            MonitorSysGenScriptExecution record = executionService.getDetailForRecord(recordId);
            if (record == null) {
                return ReturnResult.error("执行记录不存在");
            }
            return ReturnResult.ok(record);
        } catch (Exception e) {
            log.error("获取执行记录详情失败: recordId={}", recordId, e);
            return ReturnResult.error("获取执行记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除执行记录
     *
     * @param recordId 记录ID
     * @return 操作结果
     */
    @Operation(summary = "删除执行记录")
    @DeleteMapping("/{recordId}")
    public ReturnResult<Boolean> deleteForExecuteRecord(
            @Parameter(description = "记录ID") @PathVariable Long recordId) {
        log.info("删除脚本执行记录: recordId={}", recordId);
        try {
            boolean result = executionService.deleteForRecord(recordId);
            if (result) {
                return ReturnResult.ok(true, "删除成功");
            }
            return ReturnResult.error("删除失败，记录可能不存在");
        } catch (Exception e) {
            log.error("删除执行记录失败: recordId={}", recordId, e);
            return ReturnResult.error("删除执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除执行记录
     *
     * @param recordIds 记录ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除执行记录")
    @DeleteMapping("/batch")
    public ReturnResult<Boolean> batchDeleteForExecuteRecord(@RequestBody List<Long> recordIds) {
        log.info("批量删除脚本执行记录: recordIds={}", recordIds);
        try {
            boolean result = executionService.removeByIds(recordIds);
            if (result) {
                return ReturnResult.ok(true, "批量删除成功");
            }
            return ReturnResult.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除执行记录失败: recordIds={}", recordIds, e);
            return ReturnResult.error("批量删除执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 清空执行记录
     *
     * @param scriptId 脚本ID（可选，不传则清空所有）
     * @return 操作结果
     */
    @Operation(summary = "清空执行记录")
    @DeleteMapping("/clear")
    public ReturnResult<Integer> clearForExecuteRecord(
            @Parameter(description = "脚本ID（可选）") @RequestParam(required = false) Integer scriptId) {
        log.info("清空脚本执行记录: scriptId={}", scriptId);
        try {
            int count = executionService.clearForRecord(scriptId);
            return ReturnResult.ok(count, "清空成功，共删除 " + count + " 条记录");
        } catch (Exception e) {
            log.error("清空执行记录失败: scriptId={}", scriptId, e);
            return ReturnResult.error("清空执行记录失败: " + e.getMessage());
        }
    }
}
