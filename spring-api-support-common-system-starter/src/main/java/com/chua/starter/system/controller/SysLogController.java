package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysLog;
import com.chua.starter.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.Set;

/**
 * 系统日志接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "系统日志接口")
@RestController
@RequestMapping("/v2/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;


    /**
     * 系统日志分页列表
     *
     * @param sysLog 条件
     * @return 系统日志分页列表
     */
    @Operation(summary = "系统日志分页列表")
    @GetMapping("/page")
    @Permission(role = {AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysLog> pageForRole(@ParameterObject Query<SysLog> query, @ParameterObject SysLog sysLog, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysLogService.pageForLog(query, sysLog, roles));
    }

    /**
     * 删除系统日志
     *
     * @return 更新系统日志
     */
    @Operation(summary = "删除系统日志")
    @DeleteMapping("/delete")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:log:delete", role = {AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long logId) {
        if (null == logId) {
            return Result.illegal("系统日志信息不存在");
        }

        return sysLogService.delete(logId);
    }

}
