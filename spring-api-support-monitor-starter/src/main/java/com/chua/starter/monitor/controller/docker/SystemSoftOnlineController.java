package com.chua.starter.monitor.controller.docker;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.docker.SystemSoftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线软件检索控制器
 *
 * 作者: CH
 * 创建时间: 2025-10-20
 * 版本: 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/soft/online")
@RequiredArgsConstructor
@Validated
@Tag(name = "在线软件检索", description = "基于默认仓库的在线搜索与导入")
public class SystemSoftOnlineController {

    private final SystemSoftService systemSoftService;

    /**
     * 在线搜索
     * @param keyword 关键词
     * @param page 页码
     * @param size 页大小
     * @return 列表
     */
    @GetMapping("/search")
    @Operation(summary = "在线搜索", description = "基于SPI聚合默认仓库进行关键词检索")
    public ReturnPageResult<SystemSoft> search(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false) Integer size
    ) {
        return systemSoftService.searchOnlineByKeywordForOnline(keyword, page, size);
    }

    /**
     * 导入在线检索结果
     * @param items 结果项
     * @return 入队数量
     */
    @PostMapping("/import")
    @Operation(summary = "导入在线检索结果", description = "将检索结果导入到软件库(异步保存由前端提示)")
    public ReturnResult<Integer> importItems(@RequestBody List<SystemSoft> items) {
        return systemSoftService.importOnlineItemsForOnline(items);
    }
}