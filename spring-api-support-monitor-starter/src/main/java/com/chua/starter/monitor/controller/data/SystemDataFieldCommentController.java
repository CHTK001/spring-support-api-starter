package com.chua.starter.monitor.controller.data;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.SystemDataFieldCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 数据控制台 - 字段注释接口
 *
 * 为 JDBC 控制台的叶子字段节点提供注释的新增与查询能力。
 */
@RestController
@RequestMapping("/system/data/console/{id}/field")
@Api(tags = "数据控制台-字段注释")
@Tag(name = "数据控制台-字段注释")
@RequiredArgsConstructor
public class SystemDataFieldCommentController {

    private final SystemDataFieldCommentService commentService;


    /**
     * 保存字段注释
     * @param settingId 数据源配置ID
     * @param request 请求体，包含节点路径与注释内容
     */
    @PostMapping("/comment")
    @ApiOperation("保存字段注释")
    public ReturnResult<Boolean> save(@PathVariable("id") Integer settingId,
                                      @RequestBody SaveCommentRequest request) {
        if (StringUtils.isBlank(request.getNodePath())) {
            return ReturnResult.error("nodePath 不能为空");
        }
        if (StringUtils.isBlank(request.getComment())) {
            return ReturnResult.error("comment 不能为空");
        }
        return commentService.saveOrUpdateComment(settingId,
                request.getNodePath(), request.getComment(),
                request.getDatabaseName(), request.getTableName(), request.getColumnName());
    }

    /**
     * 保存注释请求
     */
    @Data
    public static class SaveCommentRequest {
        @Schema(description = "节点路径")
        private String nodePath;
        @Schema(description = "注释内容")
        private String comment;
        @Schema(description = "库名")
        private String databaseName;
        @Schema(description = "表名")
        private String tableName;
        @Schema(description = "列名")
        private String columnName;
    }
}


