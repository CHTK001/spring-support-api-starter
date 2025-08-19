package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.SystemDataSetting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 系统数据源配置 控制器（最小实现）
 * - 提供保存接口，并进行基础校验
 */
@Slf4j
@RestController
@RequestMapping("/system/data/setting")
@Tag(name = "系统数据源配置", description = "数据管理-数据源配置")
public class SystemDataSettingController {

    @PostMapping("/save")
    @Operation(summary = "保存配置", description = "保存系统数据源配置；远程模式下校验 Server 或 Host+Port")
    public ReturnResult<SystemDataSetting> save(@RequestBody SystemDataSetting setting) {
        try {
            if (setting == null) {
                return ReturnResult.error("参数为空");
            }
            if (!StringUtils.hasText(setting.getSystemDataSettingName())) {
                return ReturnResult.error("名称必填");
            }
            if (!StringUtils.hasText(setting.getSystemDataSettingMode())) {
                return ReturnResult.error("模式必填");
            }
            if (!StringUtils.hasText(setting.getSystemDataSettingType())) {
                return ReturnResult.error("类型必填");
            }
            // 远程模式下校验：地址 或 主机+端口
            if ("REMOTE".equalsIgnoreCase(setting.getSystemDataSettingMode())) {
                boolean hasServer = StringUtils.hasText(setting.getSystemDataSettingServer());
                boolean hasHost = StringUtils.hasText(setting.getSystemDataSettingHost());
                Integer port = setting.getSystemDataSettingPort();
                if (!hasServer) {
                    if (!hasHost || port == null || port <= 0) {
                        return ReturnResult.error("远程模式下需填写“地址”或同时填写“主机+端口”");
                    }
                }
            }
            // 控制台启用/禁用：后端仅接收字段，不做强逻辑处理（为空表示禁用）
            // TODO: 在真实项目中应将配置持久化到数据库，这里简化为直接返回
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("保存配置异常", e);
            return ReturnResult.error("保存异常: " + e.getMessage());
        }
    }
}

