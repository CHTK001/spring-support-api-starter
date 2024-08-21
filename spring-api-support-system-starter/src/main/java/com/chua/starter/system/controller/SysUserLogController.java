package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysUserLog;
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

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 操作日志接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "操作日志接口")
@RestController
@RequestMapping("/v2/user/log")
@RequiredArgsConstructor
public class SysUserLogController {

    private final SysUserLogService sysUserLogService;


    /**
     * 操作日志分页列表
     *
     * @param sysUserLog 条件
     * @return 操作日志分页列表
     */
    @Operation(summary = "操作日志分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysUserLog> pageForRole(@ParameterObject Query<SysUserLog> query, @ParameterObject SysUserLog sysUserLog, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserLogService.pageForUserLog(query, sysUserLog, roles));
    }

    /**
     * 删除操作日志
     *
     * @return 更新操作日志
     */
    @Operation(summary = "删除操作日志")
    @DeleteMapping("/delete")
    @OperateLog
    @SysLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:log:user:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long userLogId) {
        if (null == userLogId) {
            return Result.illegal("操作日志信息不存在");
        }

        return sysUserLogService.delete(userLogId);
    }

}
