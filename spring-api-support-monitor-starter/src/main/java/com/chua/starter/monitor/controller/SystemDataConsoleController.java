package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.starter.monitor.pojo.TableOpenResponse;
import com.chua.starter.monitor.pojo.ValueCount;
import com.chua.starter.monitor.service.SystemDataConsoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * 数据控制台控制器
 * 提供数据层级结构的查询、搜索、执行命令等功能接口
 *
 * @author CH
 * @since 2023-11-27
 */
@RestController
@RequestMapping("/system/data/console")
@Api(tags = "数据控制台")
@Tag(name = "数据控制台")
@RequiredArgsConstructor
public class SystemDataConsoleController {

    private final SystemDataConsoleService systemDataConsoleService;

    /**
     * 获取指定配置的根节点信息
     *
     * @param id    配置ID
     * @param query 元数据查询参数
     * @return 根节点层级结果
     */
    @GetMapping("/{id}/root")
    @ApiOperation("获取根节点")
    public ReturnResult<HierarchyResult<HierarchyNode>> root(@PathVariable Integer id,
                                                             MetadataQuery query) {
        return systemDataConsoleService.getRoot(id, query);
    }

    /**
     * 获取指定路径下的子节点信息
     *
     * @param id         配置ID
     * @param parentPath 父节点路径
     * @param query      元数据查询参数
     * @return 子节点层级结果
     */
    @GetMapping("/{id}/children")
    @ApiOperation("获取子节点")
    public ReturnResult<HierarchyResult<HierarchyNode>> children(@PathVariable Integer id,
                                                                 @RequestParam String parentPath,
                                                                 @RequestParam(required = false) Integer page,
                                                                 @RequestParam(required = false) Integer size,
                                                                 MetadataQuery query) {
        if (page != null && size != null) {
            query = query == null ? new MetadataQuery() : query;
            query.setPage(page, size);
        }
        return systemDataConsoleService.getChildren(id, parentPath, query);
    }

    /**
     * 获取指定路径的节点详细信息
     *
     * @param id       配置ID
     * @param nodePath 节点路径
     * @param query    元数据查询参数
     * @return 节点信息
     */
    @GetMapping("/{id}/node")
    @ApiOperation("获取节点详情")
    public ReturnResult<HierarchyNode> node(@PathVariable Integer id,
                                            @RequestParam String nodePath,
                                            MetadataQuery query) {
        return systemDataConsoleService.getNode(id, nodePath, query);
    }

    /**
     * 打开表（查询 + 合并注释）
     */
    @GetMapping("/{id}/table/open")
    @ApiOperation("打开表")
    public ReturnResult<TableOpenResponse> openTable(@PathVariable Integer id,
                                                     @RequestParam String nodePath,
                                                     @RequestParam(required = false, defaultValue = "100") Integer limit) {
        return systemDataConsoleService.openTable(id, nodePath, limit);
    }

    /**
     * 表分析：各字段分组统计
     */
    @GetMapping("/{id}/table/analyze")
    @ApiOperation("表分析")
    public ReturnResult<java.util.Map<String, java.util.List<ValueCount>>> analyze(@PathVariable Integer id,
                                                                                   @RequestParam String nodePath,
                                                                                   @RequestParam(required = false, defaultValue = "1000") Integer limit) {
        return systemDataConsoleService.analyzeTable(id, nodePath, limit);
    }

    /**
     * 根据模式搜索节点
     *
     * @param id      配置ID
     * @param pattern 搜索模式
     * @param query   元数据查询参数
     * @return 匹配的节点层级结果
     */
    @GetMapping("/{id}/search")
    @ApiOperation("搜索节点")
    public ReturnResult<HierarchyResult<HierarchyNode>> search(@PathVariable Integer id,
                                                               @RequestParam String pattern,
                                                               MetadataQuery query) {
        return systemDataConsoleService.search(id, pattern, query);
    }

    /**
     * 执行指定类型的命令或SQL语句
     *
     * @param id      配置ID
     * @param type    命令类型（可选）
     * @param command 命令内容
     * @param query   元数据查询参数
     * @return 命令执行结果
     */
    @PostMapping("/{id}/execute")
    @ApiOperation("执行命令/SQL")
    public ReturnResult<CommandResult> execute(@PathVariable Integer id,
                                               @RequestParam(required = false) String type,
                                               @RequestBody String command,
                                               MetadataQuery query) {
        return systemDataConsoleService.execute(id, type, command, query);
    }

    /**
     * 启动指定配置的数据备份
     *
     * @param id 配置ID
     * @return 启动结果
     */
    @PostMapping("/{id}/backup/start")
    public ReturnResult<Boolean> startBackup(@PathVariable Integer id) {
        return systemDataConsoleService.startBackup(id);
    }

    /**
     * 停止指定配置的数据备份
     *
     * @param id 配置ID
     * @return 停止结果
     */
    @PostMapping("/{id}/backup/stop")
    public ReturnResult<Boolean> stopBackup(@PathVariable Integer id) {
        return systemDataConsoleService.stopBackup(id);
    }

    /**
     * 查询指定配置的备份状态
     *
     * @param id 配置ID
     * @return 备份状态
     */
    @GetMapping("/{id}/backup/status")
    public ReturnResult<Boolean> backupStatus(@PathVariable Integer id) {
        return systemDataConsoleService.backupStatus(id);
    }

    /**
     * 生成并下载指定配置的文档
     *
     * @param id 配置ID
     * @return 文档字节数组响应实体
     */
    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> document(@PathVariable Integer id) {
        return systemDataConsoleService.generateDocument(id).getData();
    }

    /**
     * 下载备份文件
     */
    @GetMapping("/{id}/backup/download")
    public ResponseEntity<FileSystemResource> downloadBackup(@PathVariable Integer id, @RequestParam String path) {
        try {
            File file = new File(path);
            if (!file.exists() || file.isDirectory()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            FileSystemResource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


