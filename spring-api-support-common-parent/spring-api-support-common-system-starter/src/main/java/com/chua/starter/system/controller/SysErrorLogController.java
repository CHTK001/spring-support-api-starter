package com.chua.starter.system.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;
import com.chua.starter.system.service.SysErrorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端异常上报接口
 *
 * <p>接收前端全局异常监控(monitor worker)批量上报的运行时错误,
 * 免登录即可上报, 保证异常发生在未认证页面时也能成功入库。</p>
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

    /**
     * 前端异常上报日志服务
     */
    final SysErrorLogService sysErrorLogService;

    /**
     * 前端异常上报
     *
     * @param request       异常上报请求
     * @param bindingResult 参数校验结果
     * @return 上报结果
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
}
