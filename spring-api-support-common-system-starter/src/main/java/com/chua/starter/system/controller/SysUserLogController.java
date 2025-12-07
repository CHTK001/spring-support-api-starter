package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysUserLog;
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
 * 鎿嶄綔鏃ュ織鎺ュ彛
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "鎿嶄綔鏃ュ織鎺ュ彛")
@RestController
@RequestMapping("/v2/login/log")
@RequiredArgsConstructor
public class SysUserLogController {

    private final SysUserLogService sysUserLogService;
    /**
     * 鎿嶄綔鏃ュ織鍒嗛〉鍒楄〃
     *
     * @param sysUserLogQuery 鏉′欢
     * @return 鎿嶄綔鏃ュ織鍒嗛〉鍒楄〃
     */
    @Operation(summary = "鏌ヨ褰撳墠璐﹀彿鏃ュ織")
    @GetMapping("/mine-logs")
    public ReturnPageResult<SysUserLog> mineLogs(@ParameterObject Query<SysUserLog> query,
                                                 @ParameterObject SysUserLogQuery sysUserLogQuery,
                                                 @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
                                                 @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserLogService.mineLogs(query, sysUserLogQuery, sysUserId, roles));
    }

    /**
     * 鎿嶄綔鏃ュ織鍒嗛〉鍒楄〃
     *
     * @param sysUserLogQuery 鏉′欢
     * @return 鎿嶄綔鏃ュ織鍒嗛〉鍒楄〃
     */
    @Operation(summary = "鎿嶄綔鏃ュ織鍒嗛〉鍒楄〃")
    @GetMapping("/page")
    public ReturnPageResult<SysUserLog> pageForRole(@ParameterObject Query<SysUserLog> query, @ParameterObject SysUserLogQuery sysUserLogQuery, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserLogService.pageForUserLog(query, sysUserLogQuery, roles));
    }

    /**
     * 鍒犻櫎鎿嶄綔鏃ュ織
     *
     * @return 鏇存柊鎿嶄綔鏃ュ織
     */
    @Operation(summary = "鍒犻櫎鎿嶄綔鏃ュ織")
    @DeleteMapping("/delete")
    @SysLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:log:login:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long userLogId) {
        if (null == userLogId) {
            return Result.illegal("鎿嶄綔鏃ュ織淇℃伅涓嶅瓨鍦");
        }

        return sysUserLogService.delete(userLogId);
    }

}
