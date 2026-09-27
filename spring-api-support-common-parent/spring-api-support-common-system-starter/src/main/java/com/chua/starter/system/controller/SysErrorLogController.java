package com.chua.starter.system.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;
import com.chua.starter.system.service.SysErrorLogService;
import com.chua.starter.entity.system.SysErrorLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 前端异常上报接口
 *
 * @since 2026/8/8
 * @author CH
 */
@Tag(name = "前端异常上报接口")
@RestController
@RequestMapping("/v2/log")
@RequiredArgsConstructor
public class SysErrorLogController {

    final SysErrorLogService sysErrorLogService; // sys错误日志服务

    /**
     * 前端异常上报
     */
    @Ignore
    @PostMapping("/report")
    @Operation(summary = "前端异常上报")
    public ReturnResult<Boolean> report(@Validated @RequestBody ErrorReportV1Request request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysErrorLogService.saveReport(request);
    }

    /**
     * 异常日志分页列表
     */
    @Operation(summary = "异常日志分页列表")
    @GetMapping("/error-page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysErrorLog> page(@ParameterObject Query<SysErrorLog> query,
                                              @ParameterObject SysErrorLog sysErrorLog) {
        return PageResultUtils.ok(sysErrorLogService.pageForLog(query, sysErrorLog));
    }

    /**
     * 异常日志详情(含完整堆栈)
     * @param sysErrorLogId sysErrorLogID，不允许为 null
     * @return Return结果 对象
     */
    @Operation(summary = "异常日志详情")
    @GetMapping("/error-detail")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysErrorLog> detail(@RequestParam Long sysErrorLogId) {
        if (null == sysErrorLogId) {
            return ReturnResult.illegal("日志ID不能为空");
        }
        return ReturnResult.ok(sysErrorLogService.getDetail(sysErrorLogId));
    }

    /**
     * 异常日志综合统计(总数/今日/来源分布/Top5/处理率)
     * @return Return结果 对象
     */
    @Operation(summary = "异常日志统计")
    @GetMapping("/error-statistic")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> statistic() {
        return ReturnResult.ok(sysErrorLogService.statistic());
    }

    /**
     * 按异常类型统计数量(时间范围)
     */
    @Operation(summary = "按异常类型统计")
    @GetMapping("/error-source-statistic")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<Map<String, Object>>> sourceStatistic(@RequestParam(required = false) Long startTime,
                                                                     @RequestParam(required = false) Long endTime) {
        return ReturnResult.ok(sysErrorLogService.statisticBySource(startTime, endTime));
    }

    /**
     * 清理指定天数前的错误日志
     * @param days 方法入参 days
     * @return Return结果 对象
     */
    @Operation(summary = "清理错误日志")
    @DeleteMapping("/error/clean")
    @SysLog(module = "清理")
    @Permission(value = "sys:log:clean", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Integer> clean(@RequestParam(defaultValue = "30") int days) {
        return ReturnResult.ok(sysErrorLogService.cleanBefore(days));
    }

    /**
     * 删除异常日志(支持批量, 多个ID用逗号分隔)
     * @param sysErrorLogId sysErrorLogID，不允许为 null
     * @return Return结果 对象
     */
    @Operation(summary = "删除异常日志")
    @DeleteMapping("/error/delete")
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:log:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(@RequestParam String sysErrorLogId) {
        if (null == sysErrorLogId || sysErrorLogId.trim().isEmpty()) {
            return ReturnResult.illegal("异常日志ID不能为空");
        }
        List<Long> ids = Arrays.stream(sysErrorLogId.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
        return sysErrorLogService.deleteByIds(ids);
    }

    /**
     * 更新异常日志处理状态
     *
     * @param sysErrorLogId 异常日志ID
     * @param status        状态: 0-未处理 1-已处理 2-已忽略
     * @return Return结果 对象
     */
    @Operation(summary = "更新异常日志处理状态")
    @PutMapping("/error/status")
    @SysLog
    @Permission(value = "sys:log:status", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> updateStatus(@RequestParam Long sysErrorLogId, @RequestParam Integer status) {
        return sysErrorLogService.updateStatus(sysErrorLogId, status);
    }
}
