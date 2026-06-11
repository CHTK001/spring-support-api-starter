package com.chua.starter.monitor.controller.spider;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.*;
import com.chua.starter.monitor.properties.SpiderProperties;
import com.chua.starter.monitor.pojo.spider.*;
import com.chua.starter.monitor.service.spider.*;
import com.chua.starter.monitor.support.SpiderConsoleSessionSupport;
import jakarta.servlet.http.HttpServletRequest;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 爬虫管理控制器
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@RestController
@RequestMapping("/v1/spider")
@Api(tags = "爬虫管理")
@Tag(name = "爬虫管理")
public class MonitorSpiderController {

    private final MonitorSpiderTaskService taskService;
    private final MonitorSpiderNodeService nodeService;
    private final MonitorSpiderConnectionService connectionService;
    private final MonitorSpiderDataService dataService;
    private final MonitorSpiderLogService logService;
    private final SpiderProperties spiderProperties;
    private final SpiderSchemaService spiderSchemaService;
    private final SpiderOutputTableService spiderOutputTableService;
    private final SpiderAiService spiderAiService;

    // 存储运行中的任务状态
    private static final Map<Integer, SpiderRunStatus> runningTasks = new ConcurrentHashMap<>();

    private static final class SpiderAuthenticationException extends RuntimeException {
        private SpiderAuthenticationException(String message) {
            super(message);
        }
    }

    public MonitorSpiderController(
            MonitorSpiderTaskService taskService,
            MonitorSpiderNodeService nodeService,
            MonitorSpiderConnectionService connectionService,
            MonitorSpiderDataService dataService,
            MonitorSpiderLogService logService,
            SpiderProperties spiderProperties,
            SpiderSchemaService spiderSchemaService,
            SpiderOutputTableService spiderOutputTableService,
            SpiderAiService spiderAiService) {
        this.taskService = taskService;
        this.nodeService = nodeService;
        this.connectionService = connectionService;
        this.dataService = dataService;
        this.logService = logService;
        this.spiderProperties = spiderProperties;
        this.spiderSchemaService = spiderSchemaService;
        this.spiderOutputTableService = spiderOutputTableService;
        this.spiderAiService = spiderAiService;
    }

    @ModelAttribute
    public void ensureAuthenticated(HttpServletRequest request) {
        SpiderProperties.WebAuthConfig config = spiderProperties.getWebAuth();
        if (config == null || "none".equalsIgnoreCase(config.getMode())) {
            return;
        }
        if (!SpiderConsoleSessionSupport.isAuthenticated(request)) {
            throw new SpiderAuthenticationException("未登录");
        }
    }

    @ExceptionHandler(SpiderAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(SpiderAuthenticationException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "401");
        body.put("message", e.getMessage());
        body.put("success", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // ======================== 任务管理 ========================

    /**
     * 分页查询爬虫任务
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询爬虫任务")
    public ReturnPageResult<MonitorSpiderTask> page(Query<MonitorSpiderTask> query, MonitorSpiderTask task) {
        return PageResultUtils.ok(taskService.page(query.createPage(),
                Wrappers.<MonitorSpiderTask>lambdaQuery()
                        .like(StringUtils.isNotEmpty(task.getSpiderTaskName()), MonitorSpiderTask::getSpiderTaskName, task.getSpiderTaskName())
                        .eq(task.getSpiderTaskStatus() != null, MonitorSpiderTask::getSpiderTaskStatus, task.getSpiderTaskStatus())
                        .orderByDesc(MonitorSpiderTask::getUpdateTime)));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情")
    public ReturnResult<MonitorSpiderTask> getById(@ApiParam("任务ID") @PathVariable Integer id) {
        return ReturnResult.ok(taskService.getById(id));
    }

    /**
     * 保存任务
     */
    @PostMapping("/save")
    @ApiOperation("保存任务")
    public ReturnResult<MonitorSpiderTask> save(@RequestBody MonitorSpiderTask task) {
        if (task.getSpiderTaskStatus() == null) {
            task.setSpiderTaskStatus(1);
        }
        if (task.getSpiderTaskRunStatus() == null) {
            task.setSpiderTaskRunStatus("IDLE");
        }
        taskService.save(task);
        return ReturnResult.ok(task);
    }

    /**
     * 更新任务
     */
    @PutMapping("/update")
    @ApiOperation("更新任务")
    public ReturnResult<Boolean> update(@RequestBody MonitorSpiderTask task) {
        return ReturnResult.ok(taskService.updateById(task));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除任务")
    public ReturnResult<Boolean> delete(@RequestParam Integer spiderTaskId) {
        // 删除关联的节点和连接
        nodeService.remove(Wrappers.<MonitorSpiderNode>lambdaQuery().eq(MonitorSpiderNode::getSpiderTaskId, spiderTaskId));
        connectionService.remove(Wrappers.<MonitorSpiderConnection>lambdaQuery().eq(MonitorSpiderConnection::getSpiderTaskId, spiderTaskId));
        return ReturnResult.ok(taskService.removeById(spiderTaskId));
    }

    /**
     * 运行任务
     */
    @PostMapping("/run/{id}")
    @Operation(summary = "运行任务")
    public ReturnResult<Boolean> run(@PathVariable Integer id) {
        MonitorSpiderTask task = taskService.getById(id);
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        if (task.getSpiderTaskStatus() == 0) {
            return ReturnResult.illegal("任务已停用");
        }
        
        // 更新运行状态
        task.setSpiderTaskRunStatus("RUNNING");
        task.setSpiderTaskLastRunTime(LocalDateTime.now());
        taskService.updateById(task);
        
        // 记录运行状态
        SpiderRunStatus status = new SpiderRunStatus();
        status.setTaskId(id);
        status.setStatus("RUNNING");
        status.setStartTime(LocalDateTime.now());
        runningTasks.put(id, status);
        
        log.info("[Spider] 任务开始运行: taskId={}, name={}", id, task.getSpiderTaskName());
        return ReturnResult.ok(true);
    }

    /**
     * 停止任务
     */
    @PostMapping("/stop/{id}")
    @Operation(summary = "停止任务")
    public ReturnResult<Boolean> stop(@PathVariable Integer id) {
        MonitorSpiderTask task = taskService.getById(id);
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        
        task.setSpiderTaskRunStatus("STOPPED");
        taskService.updateById(task);
        runningTasks.remove(id);
        
        log.info("[Spider] 任务已停止: taskId={}", id);
        return ReturnResult.ok(true);
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/status/{id}")
    @Operation(summary = "获取任务状态")
    public ReturnResult<SpiderRunStatus> getStatus(@PathVariable Integer id) {
        SpiderRunStatus status = runningTasks.get(id);
        if (status == null) {
            status = new SpiderRunStatus();
            status.setTaskId(id);
            status.setStatus("IDLE");
        }
        return ReturnResult.ok(status);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息")
    public ReturnResult<SpiderStatistics> getStatistics() {
        SpiderStatistics stats = new SpiderStatistics();
        stats.setTotalCount((int) taskService.count());
        stats.setRunningCount((int) taskService.count(
                Wrappers.<MonitorSpiderTask>lambdaQuery().eq(MonitorSpiderTask::getSpiderTaskRunStatus, "RUNNING")));
        stats.setEnabledCount((int) taskService.count(
                Wrappers.<MonitorSpiderTask>lambdaQuery().eq(MonitorSpiderTask::getSpiderTaskStatus, 1)));
        
        // 今日数据量
        stats.setTodayDataCount((int) dataService.count(
                Wrappers.<MonitorSpiderData>lambdaQuery().ge(MonitorSpiderData::getCreateTime, LocalDate.now().atStartOfDay())));
        stats.setTotalDataCount((int) dataService.count());
        
        return ReturnResult.ok(stats);
    }

    /**
     * 切换任务状态
     */
    @PutMapping("/toggle-status/{id}")
    @Operation(summary = "切换任务状态")
    public ReturnResult<Boolean> toggleStatus(@PathVariable Integer id) {
        MonitorSpiderTask task = taskService.getById(id);
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        task.setSpiderTaskStatus(task.getSpiderTaskStatus() == 1 ? 0 : 1);
        return ReturnResult.ok(taskService.updateById(task));
    }

    /**
     * 克隆任务
     */
    @PostMapping("/clone/{id}")
    @Operation(summary = "克隆任务")
    public ReturnResult<MonitorSpiderTask> clone(@PathVariable Integer id) {
        MonitorSpiderTask task = taskService.getById(id);
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        
        task.setSpiderTaskId(null);
        task.setSpiderTaskName(task.getSpiderTaskName() + " (副本)");
        task.setSpiderTaskRunStatus("IDLE");
        task.setSpiderTaskTotalSuccess(0L);
        task.setSpiderTaskTotalFail(0L);
        task.setCreateTime(null);
        task.setUpdateTime(null);
        taskService.save(task);
        
        return ReturnResult.ok(task);
    }

    // ======================== 数据管理 ========================

    /**
     * 获取爬取数据
     */
    @GetMapping("/data/{taskId}")
    @Operation(summary = "获取爬取数据")
    public ReturnResult<Map<String, Object>> getData(
            @PathVariable Integer taskId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<MonitorSpiderData> pageResult = dataService.page(
                new Page<>(page, size),
                Wrappers.<MonitorSpiderData>lambdaQuery()
                        .eq(MonitorSpiderData::getSpiderTaskId, taskId)
                        .orderByDesc(MonitorSpiderData::getCreateTime));
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return ReturnResult.ok(result);
    }

    /**
     * 导出数据
     */
    @GetMapping("/export/{taskId}")
    @Operation(summary = "导出数据")
    public ReturnResult<String> exportData(
            @PathVariable Integer taskId,
            @RequestParam(defaultValue = "json") String format) {
        // 返回下载URL
        return ReturnResult.ok("/v1/spider/download/" + taskId + "?format=" + format);
    }

    /**
     * 清空数据
     */
    @DeleteMapping("/data/{taskId}")
    @Operation(summary = "清空数据")
    public ReturnResult<Boolean> clearData(@PathVariable Integer taskId) {
        return ReturnResult.ok(dataService.remove(
                Wrappers.<MonitorSpiderData>lambdaQuery().eq(MonitorSpiderData::getSpiderTaskId, taskId)));
    }

    /**
     * 删除单条数据
     */
    @DeleteMapping("/data/{taskId}/{dataId}")
    @Operation(summary = "删除单条数据")
    public ReturnResult<Boolean> deleteData(@PathVariable Integer taskId, @PathVariable Integer dataId) {
        return ReturnResult.ok(dataService.removeById(dataId));
    }

    // ======================== 日志管理 ========================

    /**
     * 获取任务日志
     */
    @GetMapping("/logs/{taskId}")
    @Operation(summary = "获取任务日志")
    public ReturnResult<Map<String, Object>> getLogs(
            @PathVariable Integer taskId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        Page<MonitorSpiderLog> pageResult = logService.page(
                new Page<>(page, size),
                Wrappers.<MonitorSpiderLog>lambdaQuery()
                        .eq(MonitorSpiderLog::getSpiderTaskId, taskId)
                        .orderByDesc(MonitorSpiderLog::getCreateTime));
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return ReturnResult.ok(result);
    }

    // ======================== 数据库支持 ========================

    /**
     * 获取爬虫表状态
     */
    @GetMapping("/table/status")
    @Operation(summary = "获取爬虫表状态")
    public ReturnResult<SpiderTableStatus> getTableStatus() {
        return executeSafely("获取爬虫表状态", spiderSchemaService::checkTableStatus);
    }

    /**
     * 初始化爬虫表
     */
    @PostMapping("/table/initialize")
    @Operation(summary = "初始化爬虫表")
    public ReturnResult<Boolean> initializeTables(@RequestParam(defaultValue = "false") boolean force) {
        return executeSafely("初始化爬虫表", () -> spiderSchemaService.initializeTables(force));
    }

    /**
     * 获取可用数据库数据源
     */
    @GetMapping("/data-source/options")
    @Operation(summary = "获取爬虫可用数据库数据源")
    public ReturnResult<List<SpiderDataSourceOption>> getDataSourceOptions() {
        return executeSafely("获取爬虫数据库数据源", () -> {
            List<SpiderDataSourceOption> result = new ArrayList<>();

            SpiderDataSourceOption builtin = new SpiderDataSourceOption();
            builtin.setBuiltin(true);
            builtin.setSettingId(null);
            builtin.setName("当前监控库");
            builtin.setType("JDBC");
            builtin.setEnabled(true);
            result.add(builtin);
            return result;
        });
    }

    /**
     * 检查输出表是否存在
     */
    @PostMapping("/output/table/exists")
    @Operation(summary = "检查爬虫输出表是否存在")
    public ReturnResult<Boolean> outputTableExists(
            @RequestParam String tableName,
            @RequestBody(required = false) SpiderOutputTableRequest request) {
        return executeSafely("检查输出表是否存在",
                () -> spiderOutputTableService.tableExists(tableName, request == null ? null : request.getDataSourceSettingId()));
    }

    /**
     * 预览输出表建表SQL
     */
    @PostMapping("/output/table/preview-sql")
    @Operation(summary = "预览爬虫输出表建表SQL")
    public ReturnResult<String> previewOutputTableSql(
            @RequestParam String tableName,
            @RequestParam(defaultValue = "mysql") String dbType,
            @RequestBody SpiderOutputTableRequest request) {
        return executeSafely("预览输出表建表SQL",
                () -> spiderOutputTableService.previewCreateTableSql(tableName, request, dbType));
    }

    /**
     * 创建输出表
     */
    @PostMapping("/output/table/create")
    @Operation(summary = "创建爬虫输出表")
    public ReturnResult<Boolean> createOutputTable(
            @RequestParam String tableName,
            @RequestParam(defaultValue = "mysql") String dbType,
            @RequestBody SpiderOutputTableRequest request) {
        return executeSafely("创建输出表",
                () -> spiderOutputTableService.createTable(tableName, request, dbType));
    }

    /**
     * 获取输出表结构
     */
    @PostMapping("/output/table/structure")
    @Operation(summary = "获取爬虫输出表结构")
    public ReturnResult<com.chua.starter.monitor.pojo.data.TableStructureInfo> getOutputTableStructure(
            @RequestParam String tableName,
            @RequestBody(required = false) SpiderOutputTableRequest request) {
        return executeSafely("获取输出表结构",
                () -> spiderOutputTableService.getTableStructure(tableName, request == null ? null : request.getDataSourceSettingId()));
    }

    /**
     * 同步输出表结构
     */
    @PostMapping("/output/table/sync-structure")
    @Operation(summary = "同步爬虫输出表结构")
    public ReturnResult<Boolean> syncOutputTableStructure(
            @RequestParam String tableName,
            @RequestParam(defaultValue = "mysql") String dbType,
            @RequestBody SpiderOutputTableRequest request) {
        return executeSafely("同步输出表结构",
                () -> spiderOutputTableService.syncStructure(tableName, request, dbType));
    }

    // ======================== 网页解析 ========================

    /**
     * 预览网页
     */
    @GetMapping("/preview")
    @Operation(summary = "预览网页")
    public ReturnResult<Map<String, Object>> preview(@RequestParam String url) {
        return executeSafely("预览网页", () -> {
            Document doc = loadDocument(url);
            Map<String, Object> result = new HashMap<>();
            result.put("html", doc.html());
            result.put("bodyHtml", doc.body() == null ? "" : doc.body().html());
            result.put("title", doc.title());
            result.put("baseUrl", doc.baseUri());
            result.put("url", url);
            return result;
        });
    }

    /**
     * 解析HTML结构
     */
    @GetMapping("/parse-html")
    @Operation(summary = "解析HTML结构")
    public ReturnResult<Map<String, Object>> parseHtml(
            @RequestParam String url,
            @RequestParam(required = false) Integer maxDepth,
            @RequestParam(required = false) Integer maxChildren) {
        return executeSafely("解析HTML结构", () -> {
            Document doc = loadDocument(url);
            int resolvedMaxDepth = maxDepth == null ? spiderProperties.getPreviewMaxDepth() : maxDepth;
            int resolvedMaxChildren = maxChildren == null ? spiderProperties.getPreviewMaxChildren() : maxChildren;
            int[] counter = new int[]{0};

            Element body = doc.body();
            Map<String, Object> tree = body == null
                    ? Collections.emptyMap()
                    : parseElement(body, buildElementXpath(body), 0, resolvedMaxDepth, resolvedMaxChildren, counter);

            Map<String, Object> result = new HashMap<>();
            result.put("tree", tree);
            result.put("rawHtml", doc.html());
            result.put("title", doc.title());
            result.put("baseUrl", doc.baseUri());
            result.put("nodeCount", counter[0]);
            result.put("maxDepth", resolvedMaxDepth);
            result.put("maxChildren", resolvedMaxChildren);
            return result;
        });
    }

    /**
     * 测试选择器
     */
    @PostMapping("/test-selector")
    @Operation(summary = "测试选择器")
    public ReturnResult<List<String>> testSelector(@RequestBody SelectorTestRequest request) {
        return executeSafely("测试选择器", () -> {
            Document doc = loadDocument(request);
            Elements elements = selectElements(doc, request);
            List<String> results = new ArrayList<>();
            for (Element element : elements) {
                results.add(abbreviate(element.text(), spiderProperties.getPreviewTextLimit()));
            }
            return results;
        });
    }

    /**
     * 获取选择器匹配节点详情
     */
    @PostMapping("/inspect-selector")
    @Operation(summary = "获取选择器匹配节点详情")
    public ReturnResult<List<Map<String, Object>>> inspectSelector(@RequestBody SelectorTestRequest request) {
        return executeSafely("获取选择器匹配节点详情", () -> {
            Document doc = loadDocument(request);
            Elements elements = selectElements(doc, request);
            List<Map<String, Object>> results = new ArrayList<>();
            for (Element element : elements) {
                results.add(buildElementSummary(element));
            }
            return results;
        });
    }

    /**
     * 生成XPath
     */
    @PostMapping("/generate-xpath")
    @Operation(summary = "生成XPath")
    public ReturnResult<String> generateXpath(@RequestBody XPathGenerateRequest request) {
        return executeSafely("生成XPath", () -> {
            if (StringUtils.isEmpty(request.getElementPath())) {
                throw new IllegalArgumentException("elementPath不能为空");
            }
            if (request.getElementPath().startsWith("/")) {
                return request.getElementPath();
            }

            if (StringUtils.isEmpty(request.getUrl())) {
                return request.getElementPath();
            }

            Document doc = loadDocument(request.getUrl());
            Elements elements = doc.select(request.getElementPath());
            if (elements.isEmpty()) {
                throw new IllegalArgumentException("未找到对应节点");
            }
            return buildElementXpath(elements.first());
        });
    }

    /**
     * 获取 Spider AI 状态
     */
    @GetMapping("/ai/status")
    @Operation(summary = "获取 Spider AI 状态")
    public ReturnResult<SpiderAiStatus> aiStatus() {
        return executeSafely("获取 Spider AI 状态", this::requireSpiderAiServiceStatus);
    }

    /**
     * AI 分析当前页面与选择器
     */
    @PostMapping("/ai/analyze")
    @Operation(summary = "Spider AI 页面分析")
    public ReturnResult<SpiderAiAnalyzeResponse> analyzeByAi(@RequestBody SpiderAiAnalyzeRequest request) {
        return executeSafely("Spider AI 页面分析", () -> requireSpiderAiService().analyzePage(request));
    }

    // ======================== SPI 管理 ========================

    /**
     * 获取所有SPI类型
     */
    @GetMapping("/spi/types")
    @Operation(summary = "获取所有SPI类型")
    public ReturnResult<SpiderSpiTypeList> getSpiTypes() {
        SpiderSpiTypeList types = new SpiderSpiTypeList();
        
        // URL源
        types.setUrlSource(Arrays.asList(
                createSpiInfo("simple", "简单URL源", "直接指定URL列表", "URL_SOURCE"),
                createSpiInfo("file", "文件URL源", "从文件读取URL列表", "URL_SOURCE"),
                createSpiInfo("database", "数据库URL源", "从数据库查询URL", "URL_SOURCE"),
                createSpiInfo("sitemap", "Sitemap URL源", "从sitemap.xml解析URL", "URL_SOURCE")
        ));
        
        // 下载器
        types.setDownloader(Arrays.asList(
                createSpiInfo("http", "HTTP下载器", "标准HTTP下载器", "DOWNLOADER"),
                createSpiInfo("selenium", "Selenium下载器", "支持JavaScript渲染", "DOWNLOADER"),
                createSpiInfo("playwright", "Playwright下载器", "现代浏览器自动化", "DOWNLOADER")
        ));
        
        // 处理器
        types.setProcessor(Arrays.asList(
                createSpiInfo("xpath", "XPath处理器", "使用XPath提取数据", "PROCESSOR"),
                createSpiInfo("css", "CSS选择器处理器", "使用CSS选择器提取", "PROCESSOR"),
                createSpiInfo("regex", "正则表达式处理器", "使用正则表达式提取", "PROCESSOR"),
                createSpiInfo("json", "JSON处理器", "处理JSON响应", "PROCESSOR")
        ));
        
        // 中间件
        types.setMiddleware(Arrays.asList(
                createSpiInfo("retry", "重试中间件", "失败时自动重试", "MIDDLEWARE"),
                createSpiInfo("delay", "延时中间件", "请求间增加延时", "MIDDLEWARE"),
                createSpiInfo("cookie", "Cookie中间件", "自动管理Cookie", "MIDDLEWARE"),
                createSpiInfo("header", "Header中间件", "自定义请求头", "MIDDLEWARE")
        ));
        
        // 数据过滤器
        types.setDataFilter(Arrays.asList(
                createSpiInfo("duplicate", "去重过滤器", "过滤重复数据", "DATA_FILTER"),
                createSpiInfo("empty", "空值过滤器", "过滤空值数据", "DATA_FILTER"),
                createSpiInfo("keyword", "关键词过滤器", "按关键词过滤", "DATA_FILTER")
        ));

        // 数据结构
        types.setDataStructure(Arrays.asList(
                createSpiInfo("default", "表结构定义", "定义输出表的字段名称、类型、精度", "DATA_STRUCTURE")
        ));

        // Pipeline
        types.setPipeline(Arrays.asList(
                createSpiInfo("console", "控制台输出", "输出到控制台", "PIPELINE"),
                createSpiInfo("json", "JSON文件", "保存为JSON文件", "PIPELINE"),
                createSpiInfo("csv", "CSV文件", "保存为CSV文件", "PIPELINE"),
                createSpiInfo("database", "数据库存储", "保存到数据库", "PIPELINE"),
                createSpiInfo("elasticsearch", "Elasticsearch", "存储到ES", "PIPELINE")
        ));
        
        // 调度器
        types.setScheduler(Arrays.asList(
                createSpiInfo("queue", "队列调度器", "基于队列的调度", "SCHEDULER"),
                createSpiInfo("priority", "优先级调度器", "支持优先级的调度", "SCHEDULER"),
                createSpiInfo("redis", "Redis调度器", "基于Redis的分布式调度", "SCHEDULER")
        ));
        
        // 站点配置
        types.setSiteConfig(Arrays.asList(
                createSpiInfo("default", "默认配置", "通用站点配置", "SITE_CONFIG"),
                createSpiInfo("custom", "自定义配置", "自定义站点配置", "SITE_CONFIG")
        ));
        
        // 代理池
        types.setProxyPool(Arrays.asList(
                createSpiInfo("static", "静态代理池", "使用固定代理列表", "PROXY_POOL"),
                createSpiInfo("api", "API代理池", "从API获取代理", "PROXY_POOL")
        ));
        
        // UA池
        types.setUaPool(Arrays.asList(
                createSpiInfo("random", "随机UA池", "随机选择User-Agent", "UA_POOL"),
                createSpiInfo("custom", "自定义UA池", "使用自定义UA列表", "UA_POOL")
        ));
        
        return ReturnResult.ok(types);
    }

    private SpiderSpiInfo createSpiInfo(String name, String displayName, String description, String type) {
        SpiderSpiInfo info = new SpiderSpiInfo();
        info.setName(name);
        info.setDisplayName(displayName);
        info.setDescription(description);
        info.setType(type);
        info.setAvailable(true);
        return info;
    }

    /**
     * 获取SPI参数
     */
    @GetMapping("/spi/parameters")
    @Operation(summary = "获取SPI参数")
    public ReturnResult<List<SpiderSpiParameter>> getSpiParameters(
            @RequestParam String spiType,
            @RequestParam String spiName) {
        List<SpiderSpiParameter> params = new ArrayList<>();
        
        // 根据不同的SPI类型返回不同的参数
        switch (spiType + ":" + spiName) {
            case "URL_SOURCE:simple":
                params.add(createParam("urls", "URL列表", "urls", true, "每行一个URL"));
                break;
            case "URL_SOURCE:file":
                params.add(createParam("filePath", "文件路径", "string", true, "URL文件路径"));
                break;
            case "DOWNLOADER:http":
                params.add(createParam("timeout", "超时时间(秒)", "number", false, "请求超时时间"));
                params.add(createParam("userAgent", "User-Agent", "string", false, "自定义UA"));
                break;
            case "DOWNLOADER:selenium":
                params.add(createParam("browserType", "浏览器类型", "select", false, "浏览器类型"));
                params.add(createParam("headless", "无头模式", "boolean", false, "是否无头运行"));
                break;
            case "PROCESSOR:xpath":
                params.add(createParam("expression", "XPath表达式", "textarea", true, "数据提取表达式"));
                break;
            case "PROCESSOR:css":
                params.add(createParam("selector", "CSS选择器", "textarea", true, "CSS选择器"));
                break;
            case "PIPELINE:database":
                params.add(createParam("tableName", "表名", "string", true, "存储表名"));
                break;
            case "DATA_STRUCTURE:default":
                params.add(createParam("tableName", "表名", "string", true, "要创建/更新的数据库表名"));
                params.add(createParam("columns", "字段定义", "column-editor", true, "输出表的字段结构（名称、类型、长度、主键等）"));
                break;
            default:
                // 默认参数
                break;
        }
        
        return ReturnResult.ok(params);
    }

    private SpiderSpiParameter createParam(String name, String label, String type, boolean required, String description) {
        SpiderSpiParameter param = new SpiderSpiParameter();
        param.setName(name);
        param.setLabel(label);
        param.setType(type);
        param.setRequired(required);
        param.setDescription(description);
        return param;
    }

    // ======================== 设计管理 ========================

    /**
     * 获取任务设计
     */
    @GetMapping("/design/{taskId}")
    @Operation(summary = "获取任务设计")
    public ReturnResult<SpiderTaskDesign> getDesign(@PathVariable Integer taskId) {
        List<MonitorSpiderNode> nodes = nodeService.list(
                Wrappers.<MonitorSpiderNode>lambdaQuery()
                        .eq(MonitorSpiderNode::getSpiderTaskId, taskId)
                        .orderByAsc(MonitorSpiderNode::getNodeOrder));
        
        List<MonitorSpiderConnection> connections = connectionService.list(
                Wrappers.<MonitorSpiderConnection>lambdaQuery()
                        .eq(MonitorSpiderConnection::getSpiderTaskId, taskId));
        
        SpiderTaskDesign design = new SpiderTaskDesign();
        design.setTaskId(taskId);
        design.setNodes(nodes);
        design.setConnections(connections);
        return ReturnResult.ok(design);
    }

    /**
     * 保存任务设计
     */
    @PostMapping("/design/{taskId}")
    @Operation(summary = "保存任务设计")
    public ReturnResult<Boolean> saveDesign(@PathVariable Integer taskId, @RequestBody SpiderTaskDesign design) {
        // 清除旧数据
        nodeService.remove(Wrappers.<MonitorSpiderNode>lambdaQuery().eq(MonitorSpiderNode::getSpiderTaskId, taskId));
        connectionService.remove(Wrappers.<MonitorSpiderConnection>lambdaQuery().eq(MonitorSpiderConnection::getSpiderTaskId, taskId));
        
        // 保存节点
        if (design.getNodes() != null) {
            for (MonitorSpiderNode node : design.getNodes()) {
                node.setSpiderTaskId(taskId);
                nodeService.save(node);
            }
        }
        
        // 保存连接
        if (design.getConnections() != null) {
            for (MonitorSpiderConnection conn : design.getConnections()) {
                conn.setSpiderTaskId(taskId);
                connectionService.save(conn);
            }
        }

        // 自动建表：检测 DATA_STRUCTURE 节点
        autoCreateTablesFromDesign(design.getNodes());

        return ReturnResult.ok(true);
    }

    private void autoCreateTablesFromDesign(List<MonitorSpiderNode> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();

        for (MonitorSpiderNode node : nodes) {
            if (!"DATA_STRUCTURE".equals(node.getNodeType())) continue;
            if (StringUtils.isBlank(node.getNodeConfig())) continue;

            try {
                com.fasterxml.jackson.databind.JsonNode config = om.readTree(node.getNodeConfig());
                String tableName = config.has("tableName") ? config.get("tableName").asText() : null;
                if (StringUtils.isBlank(tableName)) continue;

                com.fasterxml.jackson.databind.JsonNode columns = config.get("columns");
                if (columns == null || !columns.isArray() || columns.size() == 0) continue;

                SpiderOutputTableRequest req = new SpiderOutputTableRequest();
                req.setTableComment(node.getNodeName());
                List<SpiderOutputColumnDefinition> colDefs = new ArrayList<>();

                for (com.fasterxml.jackson.databind.JsonNode col : columns) {
                    SpiderOutputColumnDefinition def = new SpiderOutputColumnDefinition();
                    def.setName(col.has("name") ? col.get("name").asText() : null);
                    def.setType(col.has("type") ? col.get("type").asText() : "VARCHAR");
                    if (col.has("length") && !col.get("length").isNull()) def.setLength(col.get("length").asInt());
                    if (col.has("scale") && !col.get("scale").isNull()) def.setScale(col.get("scale").asInt());
                    def.setNullable(!col.has("nullable") || col.get("nullable").asBoolean());
                    if (col.has("defaultValue") && !col.get("defaultValue").isNull()) def.setDefaultValue(col.get("defaultValue").asText());
                    def.setPrimaryKey(col.has("primaryKey") && col.get("primaryKey").asBoolean());
                    def.setAutoIncrement(col.has("autoIncrement") && col.get("autoIncrement").asBoolean());
                    if (col.has("comment")) def.setComment(col.get("comment").asText());
                    colDefs.add(def);
                }

                req.setColumns(colDefs);
                spiderOutputTableService.syncStructure(tableName, req, "mysql");
                log.info("[Spider] 数据结构节点已同步表结构: table={}, node={}", tableName, node.getNodeName());
            } catch (Exception e) {
                log.warn("[Spider] 自动建表失败: node={}, error={}", node.getNodeName(), e.getMessage());
            }
        }
    }

    /**
     * 验证任务设计
     */
    @GetMapping("/design/{taskId}/validate")
    @Operation(summary = "验证任务设计")
    public ReturnResult<List<String>> validateDesign(@PathVariable Integer taskId) {
        List<String> errors = new ArrayList<>();
        
        List<MonitorSpiderNode> nodes = nodeService.list(
                Wrappers.<MonitorSpiderNode>lambdaQuery().eq(MonitorSpiderNode::getSpiderTaskId, taskId));
        
        if (nodes.isEmpty()) {
            errors.add("设计为空，请添加节点");
            return ReturnResult.ok(errors);
        }
        
        // 检查是否有URL源
        boolean hasUrlSource = nodes.stream().anyMatch(n -> "URL_SOURCE".equals(n.getNodeType()));
        if (!hasUrlSource) {
            errors.add("缺少URL源节点");
        }
        
        // 检查是否有下载器
        boolean hasDownloader = nodes.stream().anyMatch(n -> "DOWNLOADER".equals(n.getNodeType()));
        if (!hasDownloader) {
            errors.add("缺少下载器节点");
        }
        
        // 检查是否有处理器
        boolean hasProcessor = nodes.stream().anyMatch(n -> "PROCESSOR".equals(n.getNodeType()));
        if (!hasProcessor) {
            errors.add("缺少处理器节点");
        }
        
        return ReturnResult.ok(errors);
    }

    /**
     * 清空任务设计
     */
    @DeleteMapping("/design/{taskId}")
    @Operation(summary = "清空任务设计")
    public ReturnResult<Boolean> clearDesign(@PathVariable Integer taskId) {
        nodeService.remove(Wrappers.<MonitorSpiderNode>lambdaQuery().eq(MonitorSpiderNode::getSpiderTaskId, taskId));
        connectionService.remove(Wrappers.<MonitorSpiderConnection>lambdaQuery().eq(MonitorSpiderConnection::getSpiderTaskId, taskId));
        return ReturnResult.ok(true);
    }

    /**
     * 添加节点
     */
    @PostMapping("/design/{taskId}/nodes")
    @Operation(summary = "添加节点")
    public ReturnResult<Integer> addNode(@PathVariable Integer taskId, @RequestBody MonitorSpiderNode node) {
        node.setSpiderTaskId(taskId);
        node.setNodeKey(UUID.randomUUID().toString());
        nodeService.save(node);
        return ReturnResult.ok(node.getNodeId());
    }

    /**
     * 更新节点
     */
    @PutMapping("/design/nodes/{nodeId}")
    @Operation(summary = "更新节点")
    public ReturnResult<Boolean> updateNode(@PathVariable Integer nodeId, @RequestBody MonitorSpiderNode node) {
        node.setNodeId(nodeId);
        return ReturnResult.ok(nodeService.updateById(node));
    }

    /**
     * 删除节点
     */
    @DeleteMapping("/design/nodes/{nodeId}")
    @Operation(summary = "删除节点")
    public ReturnResult<Boolean> deleteNode(@PathVariable Integer nodeId) {
        // 删除相关连接
        connectionService.remove(Wrappers.<MonitorSpiderConnection>lambdaQuery()
                .eq(MonitorSpiderConnection::getSourceNodeId, nodeId)
                .or()
                .eq(MonitorSpiderConnection::getTargetNodeId, nodeId));
        return ReturnResult.ok(nodeService.removeById(nodeId));
    }

    /**
     * 批量更新节点位置
     */
    @PutMapping("/design/{taskId}/nodes/positions")
    @Operation(summary = "批量更新节点位置")
    public ReturnResult<Boolean> updateNodePositions(@PathVariable Integer taskId, @RequestBody List<NodePosition> positions) {
        for (NodePosition pos : positions) {
            MonitorSpiderNode node = new MonitorSpiderNode();
            node.setNodeId(pos.getNodeId());
            node.setPositionX(pos.getX());
            node.setPositionY(pos.getY());
            nodeService.updateById(node);
        }
        return ReturnResult.ok(true);
    }

    /**
     * 添加连接
     */
    @PostMapping("/design/{taskId}/connections")
    @Operation(summary = "添加连接")
    public ReturnResult<Integer> addConnection(@PathVariable Integer taskId, @RequestBody MonitorSpiderConnection connection) {
        connection.setSpiderTaskId(taskId);
        connectionService.save(connection);
        return ReturnResult.ok(connection.getConnectionId());
    }

    /**
     * 删除连接
     */
    @DeleteMapping("/design/connections/{connectionId}")
    @Operation(summary = "删除连接")
    public ReturnResult<Boolean> deleteConnection(@PathVariable Integer connectionId) {
        return ReturnResult.ok(connectionService.removeById(connectionId));
    }

    // ======================== 内部方法 ========================

    private <T> ReturnResult<T> executeSafely(String action, Supplier<T> supplier) {
        try {
            return ReturnResult.ok(supplier.get());
        } catch (IllegalArgumentException e) {
            log.warn("[Spider] {}参数错误: {}", action, e.getMessage());
            return ReturnResult.illegal(e.getMessage());
        } catch (Exception e) {
            log.error("[Spider] {}失败", action, e);
            return ReturnResult.illegal(action + "失败: " + e.getMessage());
        }
    }

    private SpiderAiService requireSpiderAiService() {
        if (spiderAiService == null) {
            throw new IllegalStateException("未检测到 BigModelService，Spider AI 不可用");
        }
        return spiderAiService;
    }

    private SpiderAiStatus requireSpiderAiServiceStatus() {
        return requireSpiderAiService().status();
    }

    private Document loadDocument(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url不能为空");
        }
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(Math.max(1, spiderProperties.getRequestTimeoutSeconds()) * 1000)
                    .get();
        } catch (Exception e) {
            throw new IllegalStateException("加载页面失败: " + e.getMessage(), e);
        }
    }

    private Document loadDocument(SelectorTestRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (StringUtils.isNotEmpty(request.getHtml())) {
            return Jsoup.parse(request.getHtml(), StringUtils.isEmpty(request.getUrl()) ? "" : request.getUrl());
        }
        return loadDocument(request.getUrl());
    }

    private Elements selectElements(Document doc, SelectorTestRequest request) {
        if (request == null || StringUtils.isEmpty(request.getSelector())) {
            throw new IllegalArgumentException("selector不能为空");
        }
        String type = StringUtils.isEmpty(request.getType()) ? "XPATH" : request.getType();
        if ("CSS".equalsIgnoreCase(type)) {
            return doc.select(request.getSelector());
        }
        return doc.selectXpath(request.getSelector());
    }

    private Map<String, Object> parseElement(
            Element element,
            String xpath,
            int depth,
            int maxDepth,
            int maxChildren,
            int[] counter) {
        if (element == null || depth > maxDepth) {
            return null;
        }

        counter[0]++;
        Map<String, Object> node = buildElementSummary(element, xpath);
        node.put("depth", depth);

        Elements childElements = element.children();
        node.put("childCount", childElements.size());
        if (depth >= maxDepth || childElements.isEmpty()) {
            return node;
        }

        int limit = Math.max(0, Math.min(maxChildren, childElements.size()));
        List<Map<String, Object>> children = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            Element child = childElements.get(i);
            Map<String, Object> childNode = parseElement(
                    child,
                    buildElementXpath(child),
                    depth + 1,
                    maxDepth,
                    maxChildren,
                    counter);
            if (childNode != null) {
                children.add(childNode);
            }
        }

        if (!children.isEmpty()) {
            node.put("children", children);
        }
        if (childElements.size() > limit) {
            node.put("childrenTruncated", true);
            node.put("truncatedCount", childElements.size() - limit);
        }
        return node;
    }

    private Map<String, Object> buildElementSummary(Element element) {
        return buildElementSummary(element, buildElementXpath(element));
    }

    private Map<String, Object> buildElementSummary(Element element, String xpath) {
        Map<String, Object> node = new LinkedHashMap<>();
        String previewText = StringUtils.isNotEmpty(element.ownText()) ? element.ownText() : element.text();
        node.put("tag", element.tagName());
        node.put("displayName", buildDisplayName(element));
        node.put("id", element.id());
        node.put("class", element.className());
        node.put("xpath", xpath);
        node.put("cssSelector", buildCssSelector(element));
        node.put("text", abbreviate(previewText, spiderProperties.getPreviewTextLimit()));
        node.put("ownText", abbreviate(element.ownText(), spiderProperties.getPreviewTextLimit()));
        node.put("attributes", extractAttributes(element));
        node.put("htmlSnippet", abbreviate(element.outerHtml(), 500));
        node.put("hasChildren", !element.children().isEmpty());
        return node;
    }

    private String buildDisplayName(Element element) {
        StringBuilder builder = new StringBuilder(element.tagName());
        if (StringUtils.isNotEmpty(element.id())) {
            builder.append("#").append(element.id());
        }
        if (!element.classNames().isEmpty()) {
            int index = 0;
            for (String className : element.classNames()) {
                builder.append(".").append(className);
                index++;
                if (index >= 2) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    private Map<String, String> extractAttributes(Element element) {
        Map<String, String> attributes = new LinkedHashMap<>();
        element.attributes().forEach(attribute ->
                attributes.put(attribute.getKey(), abbreviate(attribute.getValue(), 120)));
        return attributes;
    }

    private String buildCssSelector(Element element) {
        LinkedList<String> selectors = new LinkedList<>();
        Element current = element;
        int guard = 0;
        while (current != null && guard++ < 6) {
            StringBuilder builder = new StringBuilder(current.tagName());
            if (StringUtils.isNotEmpty(current.id())) {
                builder.append("#").append(current.id());
                selectors.addFirst(builder.toString());
                break;
            }

            if (!current.classNames().isEmpty()) {
                int index = 0;
                for (String className : current.classNames()) {
                    builder.append(".").append(className);
                    index++;
                    if (index >= 2) {
                        break;
                    }
                }
            }

            if (current.parent() != null && !"#root".equals(current.parent().tagName())) {
                int sameTagIndex = 1;
                Element sibling = current.previousElementSibling();
                while (sibling != null) {
                    if (current.tagName().equals(sibling.tagName())) {
                        sameTagIndex++;
                    }
                    sibling = sibling.previousElementSibling();
                }
                builder.append(":nth-of-type(").append(sameTagIndex).append(")");
            }

            selectors.addFirst(builder.toString());
            current = current.parent();
            if (current != null && "#root".equals(current.tagName())) {
                break;
            }
        }
        return String.join(" > ", selectors);
    }

    private String buildElementXpath(Element element) {
        LinkedList<String> paths = new LinkedList<>();
        Element current = element;
        while (current != null && !"#root".equals(current.tagName())) {
            String tag = current.tagName();
            if (current.parent() == null || "#root".equals(current.parent().tagName())) {
                paths.addFirst("/" + tag);
            } else {
                int index = 1;
                Element sibling = current.previousElementSibling();
                while (sibling != null) {
                    if (tag.equals(sibling.tagName())) {
                        index++;
                    }
                    sibling = sibling.previousElementSibling();
                }
                paths.addFirst("/" + tag + "[" + index + "]");
            }
            current = current.parent();
        }
        return String.join("", paths);
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength)) + "...";
    }

    // ======================== 批量操作 ========================

    /**
     * 批量运行任务
     */
    @PostMapping("/batch/run")
    @Operation(summary = "批量运行任务")
    public ReturnResult<Map<String, Object>> batchRun(@RequestBody BatchTaskRequest request) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        
        for (Integer taskId : request.getTaskIds()) {
            try {
                ReturnResult<Boolean> result = run(taskId);
                if (result.isOk()) {
                    success++;
                } else {
                    failed++;
                    errors.add("任务" + taskId + ": " + result.getMsg());
                }
            } catch (Exception e) {
                failed++;
                errors.add("任务" + taskId + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);
        return ReturnResult.ok(result);
    }

    /**
     * 批量停止任务
     */
    @PostMapping("/batch/stop")
    @Operation(summary = "批量停止任务")
    public ReturnResult<Map<String, Object>> batchStop(@RequestBody BatchTaskRequest request) {
        int success = 0;
        int failed = 0;
        
        for (Integer taskId : request.getTaskIds()) {
            try {
                stop(taskId);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        return ReturnResult.ok(result);
    }

    /**
     * 批量删除任务
     */
    @DeleteMapping("/batch/delete")
    @Operation(summary = "批量删除任务")
    public ReturnResult<Map<String, Object>> batchDelete(@RequestBody BatchTaskRequest request) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        
        for (Integer taskId : request.getTaskIds()) {
            try {
                delete(taskId);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add("任务" + taskId + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);
        return ReturnResult.ok(result);
    }
}
