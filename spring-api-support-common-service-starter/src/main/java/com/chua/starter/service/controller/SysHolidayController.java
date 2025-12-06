package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.ApiPlatform;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.service.SysHolidayService;
import com.chua.starter.service.entity.SysHoliday;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 节假日接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "节假日接口")
@RestController
@RequestMapping("/v2/holiday")
@RequiredArgsConstructor
public class SysHolidayController {

    private final SysHolidayService sysHolidayService;

    /**
     * 同步当前的节假日数据
     * <p>
     * 该接口用于从第三方服务同步最新的节假日信息到本地数据库。
     * 需要管理员或超级管理员权限才能调用。
     * </p>
     *
     * @return {@link ReturnResult}<{@link Boolean}> 同步结果，true表示成功，false表示失败
     */
    @Operation(summary = "同步当前的节假日")
    @GetMapping("/sync")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> sync() {
        try {
            return ReturnResult.of(sysHolidayService.sync());
        } catch (Exception e) {
            return ReturnResult.illegal("节假日同步失败：" + e.getMessage());
        }
    }

    /**
     * 根据年份获取节假日列表
     * <p>
     * 如果不传入年份参数，则默认查询当前年份的节假日信息。
     * </p>
     *
     * @param year 年份，例如：2025（可选参数）
     * @return {@link ReturnResult}<{@link List}<{@link SysHoliday}>> 节假日列表
     */
    @Operation(summary = "节假日(默认查询当前年份)")
    @GetMapping("/get")
    public ReturnResult<List<SysHoliday>> getHolidays(
            @Parameter(description = "年份，例如：2025")
            @RequestParam(required = false) Integer year) {
        try {
            return ReturnResult.of(sysHolidayService.getHolidays(year));
        } catch (Exception e) {
            return ReturnResult.illegal("获取节假日失败：" + e.getMessage());
        }
    }
}
