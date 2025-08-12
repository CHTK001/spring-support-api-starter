package com.chua.starter.monitor.controller;

import com.chua.common.support.backup.Backup;
import com.chua.common.support.document.Document;
import com.chua.common.support.lang.code.ReturnResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.SystemCapabilities;
import com.chua.starter.monitor.service.SystemDataSettingService;
import com.chua.starter.monitor.service.DataConsoleService;
import com.chua.starter.monitor.pojo.SystemDataSettingPageItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final DataConsoleService dataConsoleService;

    /**
     * 查询全部数据配置
     */
    @GetMapping("/list")
    @ApiOperation("查询全部数据配置")
    public ReturnResult<List<SystemDataSetting>> list() {
        return ReturnResult.ok(systemDataSettingService.list());
    }

    /**
     * 分页查询数据配置，直接携带能力
     */
    @GetMapping("/page")
    @ApiOperation("分页查询数据配置")
    public ReturnResult<Page<SystemDataSettingPageItem>> page(
            @RequestParam(required = false, defaultValue = "1") long current,
            @RequestParam(required = false, defaultValue = "20") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type
    ) {
        return systemDataSettingService.pageWithCapabilities(current, size, name, type);
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
        boolean isFile = "FILE".equalsIgnoreCase(setting.getSystemDataSettingMode());
        capabilities.setBackup(backup);
        capabilities.setDocument(document);
        capabilities.setFile(isFile);
        return ReturnResult.ok(capabilities);
    }

    /**
     * 上传 JDBC 驱动
     */
    @PostMapping(value = "/{id}/driver/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("上传 JDBC 驱动")
    public ReturnResult<String> uploadDriver(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return systemDataSettingService.uploadJdbcDriver(id, file);
    }

    /**
     * 生成文档HTML（iframe嵌入）
     */
    @GetMapping(value = "/{id}/document/html", produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation("生成文档HTML")
    public String documentHtml(@PathVariable Integer id) {
        return String.valueOf(dataConsoleService.generateDocument(id).getData());
    }

    /** 启动备份 */
    @PostMapping("/{id}/backup/start")
    @ApiOperation("启动备份")
    public ReturnResult<Boolean> startBackup(@PathVariable Integer id) {
        return dataConsoleService.startBackup(id);
    }

    /** 停止备份 */
    @PostMapping("/{id}/backup/stop")
    @ApiOperation("停止备份")
    public ReturnResult<Boolean> stopBackup(@PathVariable Integer id) {
        return dataConsoleService.stopBackup(id);
    }

    /** 备份状态 */
    @GetMapping("/{id}/backup/status")
    @ApiOperation("备份状态")
    public ReturnResult<Boolean> backupStatus(@PathVariable Integer id) {
        return dataConsoleService.backupStatus(id);
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


