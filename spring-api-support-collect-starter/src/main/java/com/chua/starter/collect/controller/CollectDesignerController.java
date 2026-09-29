package com.chua.starter.collect.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.service.CollectDesignerService;
import com.chua.starter.collect.vo.CollectRuntimeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集设计器接口。
 *
 * <p>工具箱、属性面板、运行概览与定时预览的数据都来自这里，设计器不内置节点类型清单。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Ignore
@Tag(name = "数据采集-设计器")
@RestController
@RequestMapping("/v2/collect/designer")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.collect", name = "enable", havingValue = "true", matchIfMissing = true)
public class CollectDesignerController {

    /**
     * 设计器服务。
     */
    private final CollectDesignerService designerService;

    /**
     * 全部节点类型元数据。
     *
     * @return 节点元数据列表
     */
    @Operation(summary = "节点类型元数据")
    @GetMapping("/metas")
    public ReturnResult<List<CollectShapeMeta>> metas() {
        return ReturnResult.ok(designerService.metas());
    }

    /**
     * 运行时概览。
     *
     * @return 概览出参
     */
    @Operation(summary = "运行时概览")
    @GetMapping("/runtime")
    public ReturnResult<CollectRuntimeVO> runtime() {
        return ReturnResult.ok(designerService.runtime());
    }

    /**
     * 预览定时表达式的触发时间。
     *
     * @param cron  定时表达式
     * @param times 预览次数
     * @return 触发时间列表
     */
    @Operation(summary = "预览定时触发时间")
    @GetMapping("/cron/next")
    public ReturnResult<List<LocalDateTime>> cronNext(@RequestParam String cron,
                                                      @RequestParam(defaultValue = "5") int times) {
        return ReturnResult.ok(designerService.cronPreview(cron, times));
    }
}
