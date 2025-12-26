package com.chua.starter.monitor.controller.spider;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSpiderTask;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.impl.MonitorSpiderTaskServiceImpl;
import com.chua.starter.mybatis.controller.AbstractSwaggerController;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 爬虫任务管理控制器
 *
 * @author CH
 * @since 2024/12/25
 */
@RestController
@RequestMapping("/v1/spider")
@Api(tags = "爬虫任务管理")
@Tag(name = "爬虫任务管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSpiderController extends AbstractSwaggerController<MonitorSpiderTaskService, MonitorSpiderTask> {

    @Getter
    private final MonitorSpiderTaskService service;

    /**
     * 预览网页内容
     *
     * @param url 目标URL
     * @return 网页内容
     */
    @GetMapping("/preview")
    @Operation(summary = "预览网页内容")
    @ApiOperation("预览网页内容")
    public ReturnResult<Map<String, Object>> previewUrl(
            @RequestParam("url") @Parameter(description = "目标URL") @ApiParam("目标URL") String url) {
        log.info("预览网页内容: url={}", url);
        return service.previewUrl(url);
    }

    /**
     * 解析HTML结构
     *
     * @param url 目标URL
     * @return HTML结构树
     */
    @GetMapping("/parse-html")
    @Operation(summary = "解析HTML结构")
    @ApiOperation("解析HTML结构")
    public ReturnResult<Map<String, Object>> parseHtml(
            @RequestParam("url") @Parameter(description = "目标URL") @ApiParam("目标URL") String url) {
        log.info("解析HTML结构: url={}", url);
        return service.parseHtml(url);
    }

    /**
     * 测试XPath/CSS选择器
     *
     * @param request 测试请求
     * @return 提取结果
     */
    @PostMapping("/test-selector")
    @Operation(summary = "测试选择器")
    @ApiOperation("测试选择器")
    public ReturnResult<List<String>> testSelector(
            @RequestBody @Parameter(description = "测试请求") @ApiParam("测试请求") SelectorTestRequest request) {
        log.info("测试选择器: url={}, selector={}, type={}", request.getUrl(), request.getSelector(), request.getType());
        return service.testSelector(request.getUrl(), request.getSelector(), request.getType());
    }

    /**
     * 根据元素生成XPath
     *
     * @param request 生成请求
     * @return XPath表达式
     */
    @PostMapping("/generate-xpath")
    @Operation(summary = "生成XPath")
    @ApiOperation("生成XPath")
    public ReturnResult<String> generateXpath(
            @RequestBody @Parameter(description = "生成请求") @ApiParam("生成请求") XPathGenerateRequest request) {
        log.info("生成XPath: url={}, elementPath={}", request.getUrl(), request.getElementPath());
        return service.generateXpath(request.getUrl(), request.getElementPath());
    }

    /**
     * 立即运行爬虫任务
     *
     * @param id 任务ID
     * @return 运行结果
     */
    @PostMapping("/run/{id}")
    @Operation(summary = "立即运行爬虫任务")
    @ApiOperation("立即运行爬虫任务")
    public ReturnResult<Boolean> runSpiderTask(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id) {
        log.info("立即运行爬虫任务: taskId={}", id);
        return service.runSpiderTask(id);
    }

    /**
     * 停止爬虫任务
     *
     * @param id 任务ID
     * @return 停止结果
     */
    @PostMapping("/stop/{id}")
    @Operation(summary = "停止爬虫任务")
    @ApiOperation("停止爬虫任务")
    public ReturnResult<Boolean> stopSpiderTask(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id) {
        log.info("停止爬虫任务: taskId={}", id);
        return service.stopSpiderTask(id);
    }

    /**
     * 获取爬虫任务运行状态
     *
     * @param id 任务ID
     * @return 运行状态
     */
    @GetMapping("/status/{id}")
    @Operation(summary = "获取爬虫任务运行状态")
    @ApiOperation("获取爬虫任务运行状态")
    public ReturnResult<Map<String, Object>> getSpiderTaskStatus(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id) {
        return service.getSpiderTaskStatus(id);
    }

    /**
     * 获取爬虫任务日志
     *
     * @param id   任务ID
     * @param page 页码
     * @param size 每页大小
     * @return 日志列表
     */
    @GetMapping("/logs/{id}")
    @Operation(summary = "获取爬虫任务日志")
    @ApiOperation("获取爬虫任务日志")
    public ReturnPageResult<Map<String, Object>> getSpiderTaskLogs(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return service.getSpiderTaskLogs(id, page, size);
    }

    /**
     * 获取爬取的数据
     *
     * @param id   任务ID
     * @param page 页码
     * @param size 每页大小
     * @return 爬取的数据
     */
    @GetMapping("/data/{id}")
    @Operation(summary = "获取爬取的数据")
    @ApiOperation("获取爬取的数据")
    public ReturnPageResult<Map<String, Object>> getSpiderTaskData(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return service.getSpiderTaskData(id, page, size);
    }

    /**
     * 导出爬取的数据
     *
     * @param id     任务ID
     * @param format 导出格式 (json/csv/excel)
     * @return 导出文件路径
     */
    @GetMapping("/export/{id}")
    @Operation(summary = "导出爬取的数据")
    @ApiOperation("导出爬取的数据")
    public ReturnResult<String> exportSpiderTaskData(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id,
            @RequestParam(value = "format", defaultValue = "json") String format) {
        log.info("导出爬取数据: taskId={}, format={}", id, format);
        return service.exportSpiderTaskData(id, format);
    }

    /**
     * 清空爬取的数据
     *
     * @param id 任务ID
     * @return 清空结果
     */
    @DeleteMapping("/data/{id}")
    @Operation(summary = "清空爬取的数据")
    @ApiOperation("清空爬取的数据")
    public ReturnResult<Boolean> clearSpiderTaskData(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id) {
        log.info("清空爬取数据: taskId={}", id);
        return service.clearSpiderTaskData(id);
    }

    /**
     * 获取爬虫任务统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取爬虫任务统计信息")
    @ApiOperation("获取爬虫任务统计信息")
    public ReturnResult<Map<String, Object>> getSpiderTaskStatistics() {
        return service.getSpiderTaskStatistics();
    }

    /**
     * 删除单条爬取数据
     *
     * @param taskId 任务ID
     * @param dataId 数据ID
     * @return 删除结果
     */
    @DeleteMapping("/data/{taskId}/{dataId}")
    @Operation(summary = "删除单条爬取数据")
    @ApiOperation("删除单条爬取数据")
    public ReturnResult<Boolean> deleteSpiderData(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer taskId,
            @PathVariable("dataId") @Parameter(description = "数据ID") @ApiParam("数据ID") Long dataId) {
        log.info("删除爬取数据: taskId={}, dataId={}", taskId, dataId);
        return service.deleteSpiderData(dataId);
    }

    /**
     * 切换任务状态（启用/停用）
     *
     * @param id 任务ID
     * @return 切换结果
     */
    @PutMapping("/toggle-status/{id}")
    @Operation(summary = "切换任务状态")
    @ApiOperation("切换任务状态")
    public ReturnResult<Boolean> toggleTaskStatus(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id) {
        log.info("切换任务状态: taskId={}", id);
        return service.toggleTaskStatus(id);
    }

    /**
     * 下载导出数据
     *
     * @param id     任务ID
     * @param format 导出格式
     */
    @GetMapping("/download/{id}")
    @Operation(summary = "下载导出数据")
    @ApiOperation("下载导出数据")
    public void downloadExportData(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id,
            @RequestParam(value = "format", defaultValue = "json") String format,
            HttpServletResponse response) throws IOException {
        log.info("下载导出数据: taskId={}, format={}", id, format);
        ((MonitorSpiderTaskServiceImpl) service).downloadExportData(id, format, response);
    }

    /**
     * 批量运行任务
     *
     * @param request 请求参数
     * @return 运行结果
     */
    @PostMapping("/batch/run")
    @Operation(summary = "批量运行任务")
    @ApiOperation("批量运行任务")
    public ReturnResult<Map<String, Object>> batchRunTasks(
            @RequestBody @Parameter(description = "批量操作请求") BatchOperationRequest request) {
        log.info("批量运行任务: taskIds={}", request.getTaskIds());
        return ((MonitorSpiderTaskServiceImpl) service).batchRunTasks(request.getTaskIds());
    }

    /**
     * 批量停止任务
     *
     * @param request 请求参数
     * @return 停止结果
     */
    @PostMapping("/batch/stop")
    @Operation(summary = "批量停止任务")
    @ApiOperation("批量停止任务")
    public ReturnResult<Map<String, Object>> batchStopTasks(
            @RequestBody @Parameter(description = "批量操作请求") BatchOperationRequest request) {
        log.info("批量停止任务: taskIds={}", request.getTaskIds());
        return ((MonitorSpiderTaskServiceImpl) service).batchStopTasks(request.getTaskIds());
    }

    /**
     * 批量删除任务
     *
     * @param request 请求参数
     * @return 删除结果
     */
    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除任务")
    @ApiOperation("批量删除任务")
    public ReturnResult<Map<String, Object>> batchDeleteTasks(
            @RequestBody @Parameter(description = "批量操作请求") BatchOperationRequest request) {
        log.info("批量删除任务: taskIds={}", request.getTaskIds());
        return ((MonitorSpiderTaskServiceImpl) service).batchDeleteTasks(request.getTaskIds());
    }

    /**
     * 批量切换状态
     *
     * @param request 请求参数
     * @return 切换结果
     */
    @PutMapping("/batch/toggle-status")
    @Operation(summary = "批量切换状态")
    @ApiOperation("批量切换状态")
    public ReturnResult<Map<String, Object>> batchToggleStatus(
            @RequestBody @Parameter(description = "批量状态切换请求") BatchStatusRequest request) {
        log.info("批量切换状态: taskIds={}, status={}", request.getTaskIds(), request.getStatus());
        return ((MonitorSpiderTaskServiceImpl) service).batchToggleStatus(request.getTaskIds(), request.getStatus());
    }

    /**
     * 克隆任务
     *
     * @param id 任务ID
     * @return 新任务
     */
    @PostMapping("/clone/{id}")
    @Operation(summary = "克隆任务")
    @ApiOperation("克隆任务")
    public ReturnResult<MonitorSpiderTask> cloneTask(
            @PathVariable("id") @Parameter(description = "任务ID") @ApiParam("任务ID") Integer id) {
        log.info("克隆任务: taskId={}", id);
        return ((MonitorSpiderTaskServiceImpl) service).cloneTask(id);
    }

    /**
     * 选择器测试请求
     */
    public static class SelectorTestRequest {
        private String url;
        private String selector;
        private String type; // XPATH, CSS, REGEX, JSON_PATH

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSelector() {
            return selector;
        }

        public void setSelector(String selector) {
            this.selector = selector;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * XPath生成请求
     */
    public static class XPathGenerateRequest {
        private String url;
        private String elementPath; // 元素在HTML中的路径

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getElementPath() {
            return elementPath;
        }

        public void setElementPath(String elementPath) {
            this.elementPath = elementPath;
        }
    }

    /**
     * 批量操作请求
     */
    public static class BatchOperationRequest {
        private List<Integer> taskIds;

        public List<Integer> getTaskIds() {
            return taskIds;
        }

        public void setTaskIds(List<Integer> taskIds) {
            this.taskIds = taskIds;
        }
    }

    /**
     * 批量状态切换请求
     */
    public static class BatchStatusRequest {
        private List<Integer> taskIds;
        private Integer status;

        public List<Integer> getTaskIds() {
            return taskIds;
        }

        public void setTaskIds(List<Integer> taskIds) {
            this.taskIds = taskIds;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
