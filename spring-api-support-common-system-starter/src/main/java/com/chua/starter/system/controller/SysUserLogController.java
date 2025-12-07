package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import com.chua.starter.system.service.SysUserLogService;
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
 * 操作日志接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "操作日志接口")
@RestController
@RequestMapping("/v2/login/log")
@RequiredArgsConstructor
public class SysUserLogController {

    private final SysUserLogService sysUserLogService;
    /**
     * 操作日志分页列表
     *
     * @param sysUserLogQuery 条件
     * @return 操作日志分页列表
     */
    @Operation(summary = "操作日志分页列表")
    @GetMapping("/mine-logs")
    public ReturnPageResult<SysUserLog> mineLogs(@ParameterObject Query<SysUserLog> query,
                                                 @ParameterObject SysUserLogQuery sysUserLogQuery,
                                                 @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
                                                 @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserLogService.mineLogs(query, sysUserLogQuery, sysUserId, roles));
    }

    /**
     * 操作日志分页列表
     *
     * @param sysUserLogQuery 条件
     * @return 操作日志分页列表
     */
    @Operation(summary = "操作日志分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysUserLog> pageForRole(@ParameterObject Query<SysUserLog> query, @ParameterObject SysUserLogQuery sysUserLogQuery, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserLogService.pageForUserLog(query, sysUserLogQuery, roles));
    }

    /**
     * 删除操作日志
     *
     * @return 更新操作日志
     */
    @Operation(summary = "删除操作日志")
    @DeleteMapping("/delete")
    @SysLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:log:login:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long userLogId) {
        if (null == userLogId) {
            return ReturnResult.illegal("操作日志信息不存在");
        }

        return sysUserLogService.delete(userLogId);
    }

}
