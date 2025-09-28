package com.chua.starter.service.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.annotations.ApiPlatform;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysLimitRecordService;
import com.chua.starter.system.entity.SysLimitRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 限流记录接口
 * 
 * @author CH
 * @since 2025/9/28
 */
@Tag(name = "限流记录接口")
@RestController
@RequestMapping("/v2/limit/record")
@RequiredArgsConstructor
public class SysLimitRecordController {

    private final SysLimitRecordService sysLimitRecordService;

    /**
     * 限流记录分页列表
     *
     * @param query 查询条件
     * @param sysLimitRecord 条件
     * @return 限流记录分页列表
     */
    @Operation(summary = "限流记录分页列表")
    @GetMapping("/page")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnPageResult<SysLimitRecord> pageForLimitRecord(
            @ParameterObject Query<SysLimitRecord> query, 
            @ParameterObject SysLimitRecord sysLimitRecord, 
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        IPage<SysLimitRecord> page = sysLimitRecordService.page(query.createPage(), Wrappers.<SysLimitRecord>lambdaQuery()
                .like(StringUtils.isNotBlank(sysLimitRecord.getSysLimitPath()), SysLimitRecord::getSysLimitPath, sysLimitRecord.getSysLimitPath())
                .like(StringUtils.isNotBlank(sysLimitRecord.getSysLimitName()), SysLimitRecord::getSysLimitName, sysLimitRecord.getSysLimitName())
                .eq(sysLimitRecord.getSysUserId() != null, SysLimitRecord::getSysUserId, sysLimitRecord.getSysUserId())
                .like(StringUtils.isNotBlank(sysLimitRecord.getClientIp()), SysLimitRecord::getClientIp, sysLimitRecord.getClientIp())
                .ge(sysLimitRecord.getSysLimitTime() != null, SysLimitRecord::getSysLimitTime, sysLimitRecord.getSysLimitTime())
                .orderByDesc(SysLimitRecord::getSysLimitTime)
        );
        return PageResultUtils.ok(page);
    }

    /**
     * 删除限流记录
     *
     * @param sysLimitRecordId 限流记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除限流记录")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Permission(value = "sys:limit:record:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysLimitRecordId) {
        if (null == sysLimitRecordId) {
            return Result.illegal("限流记录ID不能为空");
        }

        boolean result = sysLimitRecordService.removeById(sysLimitRecordId);
        return ReturnResult.of(result);
    }

    /**
     * 批量删除限流记录
     *
     * @param ids 限流记录ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除限流记录")
    @DeleteMapping("/deleteBatch")
    @ApiPlatform("system")
    @Permission(value = "sys:limit:record:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> deleteBatch(@RequestBody Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.illegal("限流记录ID列表不能为空");
        }

        boolean result = sysLimitRecordService.removeByIds(ids);
        return ReturnResult.of(result);
    }
}