package com.chua.starter.monitor.controller.spider;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.*;
import com.chua.starter.monitor.pojo.spider.*;
import com.chua.starter.monitor.service.spider.*;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
@RequiredArgsConstructor
public class MonitorSpiderController {

    private final MonitorSpiderTaskService taskService;
    private final MonitorSpiderNodeService nodeService;
    private final MonitorSpiderConnectionService connectionService;
    private final MonitorSpiderDataService dataService;
    private final MonitorSpiderLogService logService;

    // 存储运行中的任务状态
    private static final Map<Integer, SpiderRunStatus> runningTasks = new ConcurrentHashMap<>();

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

    // ======================== 网页解析 ========================

    /**
     * 预览网页
     */
    @GetMapping("/preview")
    @Operation(summary = "预览网页")
    public ReturnResult<Map<String, Object>> preview(@RequestParam String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();
            
            Map<String, Object> result = new HashMap<>();
            result.put("html", doc.html());
            result.put("title", doc.title());
            result.put("baseUrl", doc.baseUri());
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[Spider] 预览网页失败: url={}", url, e);
            return ReturnResult.illegal("预览失败: " + e.getMessage());
        }
    }

    /**
     * 解析HTML结构
     */
    @GetMapping("/parse-html")
    @Operation(summary = "解析HTML结构")
    public ReturnResult<Map<String, Object>> parseHtml(@RequestParam String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();
            
            Map<String, Object> tree = parseElement(doc.body(), "/html/body", 0);
            
            Map<String, Object> result = new HashMap<>();
            result.put("tree", tree);
            result.put("rawHtml", doc.html());
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[Spider] 解析HTML失败: url={}", url, e);
            return ReturnResult.illegal("解析失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseElement(Element element, String xpath, int depth) {
        if (depth > 10) return null; // 限制深度
        
        Map<String, Object> node = new HashMap<>();
        node.put("tag", element.tagName());
        node.put("id", element.id());
        node.put("class", element.className());
        node.put("xpath", xpath);
        node.put("text", element.ownText().length() > 100 ? element.ownText().substring(0, 100) + "..." : element.ownText());
        
        List<Map<String, Object>> children = new ArrayList<>();
        Elements childElements = element.children();
        Map<String, Integer> tagCounts = new HashMap<>();
        
        for (Element child : childElements) {
            String tag = child.tagName();
            int index = tagCounts.getOrDefault(tag, 0) + 1;
            tagCounts.put(tag, index);
            
            String childXpath = xpath + "/" + tag + "[" + index + "]";
            Map<String, Object> childNode = parseElement(child, childXpath, depth + 1);
            if (childNode != null) {
                children.add(childNode);
            }
        }
        
        if (!children.isEmpty()) {
            node.put("children", children);
        }
        
        return node;
    }

    /**
     * 测试选择器
     */
    @PostMapping("/test-selector")
    @Operation(summary = "测试选择器")
    public ReturnResult<List<String>> testSelector(@RequestBody SelectorTestRequest request) {
        try {
            Document doc = Jsoup.connect(request.getUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();
            
            List<String> results = new ArrayList<>();
            Elements elements;
            
            if ("CSS".equalsIgnoreCase(request.getType())) {
                elements = doc.select(request.getSelector());
            } else {
                // 简单的XPath支持
                elements = doc.selectXpath(request.getSelector());
            }
            
            for (Element element : elements) {
                String text = element.text();
                if (text.length() > 200) {
                    text = text.substring(0, 200) + "...";
                }
                results.add(text);
            }
            
            return ReturnResult.ok(results);
        } catch (Exception e) {
            log.error("[Spider] 测试选择器失败", e);
            return ReturnResult.illegal("测试失败: " + e.getMessage());
        }
    }

    /**
     * 生成XPath
     */
    @PostMapping("/generate-xpath")
    @Operation(summary = "生成XPath")
    public ReturnResult<String> generateXpath(@RequestBody XPathGenerateRequest request) {
        // 简单返回elementPath
        return ReturnResult.ok(request.getElementPath());
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
        
        return ReturnResult.ok(true);
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
