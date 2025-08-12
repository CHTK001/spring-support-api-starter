package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.starter.monitor.service.DataConsoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/data/console")
@Api(tags = "数据控制台")
@Tag(name = "数据控制台")
@RequiredArgsConstructor
public class DataConsoleController {

    private final DataConsoleService dataConsoleService;

    @GetMapping("/{id}/root")
    @ApiOperation("获取根节点")
    public ReturnResult<HierarchyResult<HierarchyNode>> root(@PathVariable Integer id,
                                                             MetadataQuery query) {
        return dataConsoleService.getRoot(id, query);
    }

    @GetMapping("/{id}/children")
    @ApiOperation("获取子节点")
    public ReturnResult<HierarchyResult<HierarchyNode>> children(@PathVariable Integer id,
                                                                 @RequestParam String parentPath,
                                                                 MetadataQuery query) {
        return dataConsoleService.getChildren(id, parentPath, query);
    }

    @GetMapping("/{id}/node")
    @ApiOperation("获取节点详情")
    public ReturnResult<HierarchyNode> node(@PathVariable Integer id,
                                            @RequestParam String nodePath,
                                            MetadataQuery query) {
        return dataConsoleService.getNode(id, nodePath, query);
    }

    @GetMapping("/{id}/search")
    @ApiOperation("搜索节点")
    public ReturnResult<HierarchyResult<HierarchyNode>> search(@PathVariable Integer id,
                                                               @RequestParam String pattern,
                                                               MetadataQuery query) {
        return dataConsoleService.search(id, pattern, query);
    }

    @PostMapping("/{id}/execute")
    @ApiOperation("执行命令/SQL")
    public ReturnResult<CommandResult> execute(@PathVariable Integer id,
                                               @RequestParam(required = false) String type,
                                               @RequestBody String command,
                                               MetadataQuery query) {
        return dataConsoleService.execute(id, type, command, query);
    }

    @PostMapping("/{id}/backup/start")
    public ReturnResult<Boolean> startBackup(@PathVariable Integer id) {
        return dataConsoleService.startBackup(id);
    }

    @PostMapping("/{id}/backup/stop")
    public ReturnResult<Boolean> stopBackup(@PathVariable Integer id) {
        return dataConsoleService.stopBackup(id);
    }

    @GetMapping("/{id}/backup/status")
    public ReturnResult<Boolean> backupStatus(@PathVariable Integer id) {
        return dataConsoleService.backupStatus(id);
    }

    @GetMapping("/{id}/document")
    public ReturnResult<Object> document(@PathVariable Integer id) {
        return dataConsoleService.generateDocument(id);
    }
}


