package com.chua.starter.monitor.controller.data;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.storage.backup.Backup;
import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.document.parser.Document;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.base.SchemaMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadataFactory;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.ServerStatic;
import com.chua.starter.monitor.pojo.app.SystemCapabilities;
import com.chua.starter.monitor.pojo.data.SystemDataSettingPageItem;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.service.data.SystemDataSettingService;
import com.chua.starter.monitor.service.impl.SystemDataConsoleServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 数据管理配置
 *
 * 提供数据源配置的增删改查、能力查询与控制台权限配置的读写接口。
 *
 * @author CH
 * @since 2025-04-05
 */
@Slf4j
@RestController
@RequestMapping("/system/data/setting")
@Api(tags = "数据管理配置")
@Tag(name = "数据管理配置")
@RequiredArgsConstructor
public class SystemDataSettingController {

    private final SystemDataSettingService systemDataSettingService;
    private final SystemDataConsoleService dataConsoleService;

    /**
     * 查询全部数据配置
     *
     * @return 返回所有数据源配置列表
     * 示例：ReturnResult.ok(List<SystemDataSetting>)
     */
    @GetMapping("/list")
    @ApiOperation("查询全部数据配置")
    public ReturnResult<List<SystemDataSetting>> list() {
        return ReturnResult.ok(systemDataSettingService.list());
    }

    /**
     * 分页查询数据配置，直接携带能力
     *
     * @param current 当前页码（从1开始），默认值为1
     *                示例：1
     * @param size    每页显示记录数，默认值为20
     *                示例：10
     * @param name    数据源名称模糊查询条件（可选）
     *                示例："MySQL"
     * @param type    数据源类型精确查询条件（可选）
     *                示例："JDBC"
     * @param sort    排序字段（可选）
     *                示例："systemDataSettingName"
     * @param order   排序方式（可选），asc 或 desc
     *                示例："asc"
     * @return 分页结果，包含数据源配置及其能力信息
     */
    @GetMapping("/page")
    @ApiOperation("分页查询数据配置")
    public ReturnResult<Page<SystemDataSettingPageItem>> page(
            @RequestParam(required = false, defaultValue = "1") long current,
            @RequestParam(required = false, defaultValue = "20") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "asc") String order
    ) {
        return systemDataSettingService.pageWithCapabilities(current, size, name, type, sort, order);
    }

    /**
     * 保存或更新数据配置
     *
     * @param setting 数据源配置实体对象，不能为空。
     *                示例：new SystemDataSetting() {{ setSystemDataSettingName("MySQL数据库"); setSystemDataSettingType("JDBC"); }}
     * @return 操作结果，成功返回 true，失败返回 false。
     */
    @PostMapping("/save")
    @ApiOperation("保存或更新数据配置")
    public ReturnResult<Boolean> save(@RequestBody @Schema(description = "数据源配置") SystemDataSetting setting) {
        return systemDataSettingService.saveOrUpdateSetting(setting);
    }

    /**
     * 按ID删除数据配置
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 操作结果，成功返回 true，失败返回 false。
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除数据配置")
    public ReturnResult<Boolean> delete(@PathVariable Integer id) {
        return systemDataSettingService.deleteById(id);
    }

    /**
     * 查询能力支持：是否支持备份/文档/文件
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 系统能力信息，包括备份、文档和文件支持情况。
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
        boolean backup = ServiceProvider.of(Backup.class).isSupport(setting.getSystemDataSettingType());
        boolean document = ServiceProvider.of(Document.class).isSupport(setting.getSystemDataSettingType());
        boolean isFile = "FILE".equalsIgnoreCase(setting.getSystemDataSettingMode());
        capabilities.setBackup(backup);
        capabilities.setDocument(document);
        capabilities.setFile(isFile);
        DataSourceOptions options = SystemDataConsoleServiceImpl.buildOptions(setting);
        if (setting.getSystemDataSettingServer() != null && setting.getSystemDataSettingServer().toLowerCase().startsWith("jdbc:")) {
            HierarchyMetadata jdbcHierarchy = HierarchyMetadataFactory.createJdbcHierarchy(options);
            if(jdbcHierarchy instanceof SchemaMetadata schemaMetadata) {
                Map<String, Boolean> ability = schemaMetadata.ability();
                capabilities.setEmailExists(ability.getOrDefault("idle", false));
                capabilities.setEmailExists(ability.getOrDefault("exists", false));

            }
        }
        return ReturnResult.ok(capabilities);
    }

    /**
     * 上传 JDBC 驱动
     *
     * @param id   数据源配置的主键ID，不能为空。
     *             示例：1
     * @param file 要上传的 JDBC 驱动文件，不能为空。
     *             示例：new MockMultipartFile("driver.jar", "driver.jar", "application/java-archive", new byte[0])
     * @return 上传成功后返回文件存储路径，失败则返回错误信息。
     */
    @PostMapping(value = "/{id}/driver/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("上传 JDBC 驱动")
    public ReturnResult<String> uploadDriver(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return systemDataSettingService.uploadJdbcDriver(id, file);
    }

    /**
     * 生成文档HTML（iframe嵌入）
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 文档数据，以字节数组形式返回生成的文档文件
     */
    @GetMapping(value = "/{id}/document/html", produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation("生成文档HTML")
    public ResponseEntity<byte[]> documentHtml(@PathVariable Integer id) {
        return dataConsoleService.generateDocument(id).getData();
    }

    /**
     * 启动备份
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    @PostMapping("/{id}/backup/start")
    @ApiOperation("启动备份")
    public ReturnResult<Boolean> startBackup(@PathVariable Integer id) {
        return dataConsoleService.startBackup(id);
    }


    /**
     * 获取服务器静态信息
     *
     * @return 服务器静态信息
     */
    @GetMapping("/server/static")
    @ApiOperation("获取服务器静态信息")
    public ReturnResult<ServerStatic> getServerStatic() {
        return ReturnResult.ok(dataConsoleService.getServerStatic());
    }

    /**
     * 停止备份
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    @PostMapping("/{id}/backup/stop")
    @ApiOperation("停止备份")
    public ReturnResult<Boolean> stopBackup(@PathVariable Integer id) {
        return dataConsoleService.stopBackup(id);
    }

    /**
     * 备份状态
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 备份状态，true表示正在备份，false表示未在备份
     */
    @GetMapping("/{id}/backup/status")
    @ApiOperation("备份状态")
    public ReturnResult<Boolean> backupStatus(@PathVariable Integer id) {
        return dataConsoleService.backupStatus(id);
    }

    /**
     * 读取控制台权限配置(JSON)
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 控制台权限配置的JSON字符串。
     */
    @GetMapping("/{id}/console/config")
    @ApiOperation("读取控制台权限配置(JSON)")
    public ReturnResult<String> getConsoleConfig(@PathVariable Integer id) {
        return systemDataSettingService.getConsoleConfig(id);
    }

    /**
     * 保存控制台权限配置(JSON)
     *
     * @param id     数据源配置的主键ID，不能为空。
     *               示例：1
     * @param config 控制台权限配置的JSON字符串，不能为空。
     *               示例："{\"tableAccess\": true, \"graphAccess\": false}"
     * @return 操作结果，成功返回 true，失败返回 false。
     */
    @PostMapping("/{id}/console/config")
    @ApiOperation("保存控制台权限配置(JSON)")
    public ReturnResult<Boolean> saveConsoleConfig(@PathVariable Integer id, @RequestBody String config) {
        return systemDataSettingService.saveConsoleConfig(id, config);
    }
}

