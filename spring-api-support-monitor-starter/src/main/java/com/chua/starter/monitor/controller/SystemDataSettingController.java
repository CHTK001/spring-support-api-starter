package com.chua.starter.monitor.controller;

import com.chua.common.support.backup.Backup;
import com.chua.common.support.document.Document;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.SystemCapabilities;
import com.chua.starter.monitor.service.SystemDataSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据管理配置
 *
 * 提供数据源配置的增删改查、能力查询与控制台权限配置的读写接口。
 *
 * @author CH
 */
@RestController
@RequestMapping("/system/data/setting")
@Api(tags = "数据管理配置")
@Tag(name = "数据管理配置")
@RequiredArgsConstructor
public class SystemDataSettingController {

    private final SystemDataSettingService systemDataSettingService;

    /**
     * 查询全部数据配置
     */
    @GetMapping("/list")
    @ApiOperation("查询全部数据配置")
    public ReturnResult<List<SystemDataSetting>> list() {
        return ReturnResult.ok(systemDataSettingService.list());
    }

    /**
     * 保存或更新数据配置
     */
    @PostMapping("/save")
    @ApiOperation("保存或更新数据配置")
    public ReturnResult<Boolean> save(@RequestBody @Schema(description = "数据源配置") SystemDataSetting setting) {
        return systemDataSettingService.saveOrUpdateSetting(setting);
    }

    /**
     * 按ID删除数据配置
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除数据配置")
    public ReturnResult<Boolean> delete(@PathVariable Integer id) {
        return systemDataSettingService.deleteById(id);
    }

    /**
     * 查询能力支持：是否支持备份/文档/文件
     */
    @GetMapping("/{id}/capabilities")
    @ApiOperation("查询能力支持：是否支持备份/文档/文件")
    public ReturnResult<SystemCapabilities> capabilities(@PathVariable Integer id) {
        SystemDataSetting setting = systemDataSettingService.getById(id);
        if (setting == null) {
            return ReturnResult.error("不存在");
        }
        SystemCapabilities capabilities = new SystemCapabilities();
        // 通过 SPI 判断能力
        boolean backup = ServiceProvider.of(Backup.class ).isSupport(setting.getSystemDataSettingType());
        boolean document = ServiceProvider.of(Document.class).isSupport(setting.getSystemDataSettingType());
        boolean isFile = Boolean.TRUE.equals(setting.getSystemDataSettingIsFile());
        capabilities.setBackup(backup);
        capabilities.setDocument(document);
        capabilities.setFile(isFile);
        return ReturnResult.ok(capabilities);
    }

    /**
     * 读取控制台权限配置(JSON)
     */
    @GetMapping("/{id}/console/config")
    @ApiOperation("读取控制台权限配置(JSON)")
    public ReturnResult<String> getConsoleConfig(@PathVariable Integer id) {
        return systemDataSettingService.getConsoleConfig(id);
    }

    /**
     * 保存控制台权限配置(JSON)
     */
    @PostMapping("/{id}/console/config")
    @ApiOperation("保存控制台权限配置(JSON)")
    public ReturnResult<Boolean> saveConsoleConfig(@PathVariable Integer id, @RequestBody String config) {
        return systemDataSettingService.saveConsoleConfig(id, config);
    }
}


