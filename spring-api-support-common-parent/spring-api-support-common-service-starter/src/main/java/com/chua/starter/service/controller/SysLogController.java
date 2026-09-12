package com.chua.starter.service.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.service.SysLogService;
import com.chua.starter.entity.service.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 系统日志接口
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "系统日志接口")
@RestController
@RequestMapping("/v2/log")
@RequiredArgsConstructor
public class SysLogController {

    /** SYS日志服务 */
    private final SysLogService sysLogService;

    /**
    * 系统日志分页列表
    *
    * @param sysLog 条件
    * @return 系统日志分页列表
    * @param query 查询
    * @param true true
     */
    @Operation(summary = "系统日志分页列表")
    @GetMapping("/page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysLog> pageForRole(@ParameterObject Query<SysLog> query, @ParameterObject SysLog sysLog, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysLogService.pageForLog(query, sysLog, roles));
    }
}
