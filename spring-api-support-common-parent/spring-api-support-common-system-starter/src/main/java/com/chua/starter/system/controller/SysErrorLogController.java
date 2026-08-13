package com.chua.starter.system.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;
import com.chua.starter.system.service.SysErrorLogService;
import com.chua.starter.entity.system.SysErrorLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端异常上报接口
 *
 * @author CH
 * @since 2026/8/8
 */
@Tag(name = "前端异常上报接口")
@RestController
@RequestMapping("/v2/log")
@RequiredArgsConstructor
@Ignore
public class SysErrorLogController {

    final SysErrorLogService sysErrorLogService;

    /**
     * 前端异常上报
     */
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
    public ReturnPageResult<SysErrorLog> page(@ParameterObject Query<SysErrorLog> query,
                                              @ParameterObject SysErrorLog sysErrorLog) {
        return PageResultUtils.ok(sysErrorLogService.pageForLog(query, sysErrorLog));
    }
}
