package com.chua.starter.monitor.service.spider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.advanced.support.lang.spider.Page;
import com.chua.advanced.support.lang.spider.ResultItems;
import com.chua.advanced.support.lang.spider.Site;
import com.chua.advanced.support.lang.spider.Spider;
import com.chua.advanced.support.lang.spider.pipeline.Pipeline;
import com.chua.advanced.support.lang.spider.processor.PageProcessor;
import com.chua.starter.monitor.entity.MonitorSpiderData;
import com.chua.starter.monitor.entity.MonitorSpiderLog;
import com.chua.starter.monitor.entity.MonitorSpiderTask;
import com.chua.starter.monitor.mapper.spider.MonitorSpiderDataMapper;
import com.chua.starter.monitor.mapper.spider.MonitorSpiderLogMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import jakarta.annotation.PreDestroy;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬虫执行器服务
 * 集成 utils-support-advanced-starter 的 Spider 框架
 *
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpiderExecutorService {

    private final MonitorSpiderTaskService spiderTaskService;
    private final MonitorSpiderDataMapper spiderDataMapper;
    private final MonitorSpiderLogMapper spiderLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 运行中的爬虫实例
     */
    private final Map<Integer, Spider> runningSpiders = new ConcurrentHashMap<>();

    /**
     * 爬虫日志缓存
     */
    private final Map<Integer, List<Map<String, Object>>> spiderLogs = new ConcurrentHashMap<>();

    /**
     * 爬虫数据缓存
     */
    private final Map<Integer, List<Map<String, Object>>> spiderData = new ConcurrentHashMap<>();

    /**
     * 速率限制器
     */
    private final Map<Integer, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    /**
     * 深度跟踪器
     */
    private final Map<Integer, Map<String, Integer>> depthTrackers = new ConcurrentHashMap<>();

    /**
     * 执行爬虫任务
     *
     * @param task 爬虫任务
     */
    public void executeTask(MonitorSpiderTask task) {
        Integer taskId = task.getSpiderTaskId();

        if (runningSpiders.containsKey(taskId)) {
            log.warn("Task {} is already running", taskId);
            return;
        }

        // 初始化日志和数据缓存
        spiderLogs.put(taskId, Collections.synchronizedList(new ArrayList<>()));
        spiderData.put(taskId, Collections.synchronizedList(new ArrayList<>()));

        // 初始化深度跟踪器
        depthTrackers.put(taskId, new ConcurrentHashMap<>());
        depthTrackers.get(taskId).put(task.getSpiderTaskUrl(), 0);

        // 初始化速率限制器
        Integer rateLimit = task.getSpiderTaskRateLimit();
        if (rateLimit != null && rateLimit > 0) {
            rateLimiters.put(taskId, new RateLimiter(rateLimit));
            addLog(taskId, "INFO", "已启用速率限制: " + rateLimit + " 请求/分钟", null);
        }

        try {
            // 解析提取规则
            List<ExtractRule> extractRules = parseExtractRules(task.getSpiderTaskExtractRules());

            // 创建页面处理器
            PageProcessor pageProcessor = new DynamicPageProcessor(task, extractRules);

            // 创建爬虫实例
            Spider spider = Spider.create(pageProcessor)
                    .addUrl(task.getSpiderTaskUrl())
                    .thread(task.getSpiderTaskThreadNum())
                    .setExitWhenComplete(true);

            // 添加分页URL
            addPaginationUrls(spider, task);

            // 添加数据管道
            spider.addPipeline(new MonitorPipeline(taskId));

            // 设置爬虫监听器
            spider.setSpiderListeners(List.of(new TaskSpiderListener(taskId)));

            // 保存爬虫实例
            runningSpiders.put(taskId, spider);

            // 记录开始日志
            addLog(taskId, "INFO", "爬虫任务开始执行", task.getSpiderTaskUrl());

            // 异步执行，并添加完成回调
            Thread.ofVirtual().start(() -> {
                try {
                    spider.run();
                } finally {
                    // 爬虫完成后更新状态
                    onSpiderComplete(taskId);
                }
            });

        } catch (Exception e) {
            log.error("Failed to execute spider task {}", taskId, e);
            addLog(taskId, "ERROR", "爬虫任务启动失败: " + e.getMessage(), null);

            // 更新任务状态
            task.setSpiderTaskRunStatus("ERROR");
            task.setSpiderTaskTotalFail(task.getSpiderTaskTotalFail() + 1);
            spiderTaskService.updateById(task);
        }
    }

    /**
     * 添加分页URL
     */
    private void addPaginationUrls(Spider spider, MonitorSpiderTask task) {
        String pagePattern = task.getSpiderTaskPagePattern();
        Integer maxPages = task.getSpiderTaskMaxPages();

        if (pagePattern != null && !pagePattern.isEmpty() && maxPages != null && maxPages > 1) {
            for (int i = 2; i <= maxPages; i++) {
                String pageUrl = pagePattern.replace("{page}", String.valueOf(i));
                spider.addUrl(pageUrl);
                // 记录分页URL的深度为0（与主页面同级）
                depthTrackers.get(task.getSpiderTaskId()).put(pageUrl, 0);
            }
            addLog(task.getSpiderTaskId(), "INFO", "已添加分页URL: " + (maxPages - 1) + " 页", null);
        }
    }

    /**
     * 停止爬虫任务
     *
     * @param taskId 任务ID
     */
    public void stopTask(Integer taskId) {
        Spider spider = runningSpiders.get(taskId);
        if (spider != null) {
            spider.stop();
            runningSpiders.remove(taskId);
            addLog(taskId, "INFO", "爬虫任务已停止", null);
        }
    }

    /**
     * 检查任务是否在运行
     *
     * @param taskId 任务ID
     * @return 是否运行中
     */
    public boolean isRunning(Integer taskId) {
        return runningSpiders.containsKey(taskId);
    }

    /**
     * 获取任务日志（分页查询数据库）
     *
     * @param taskId 任务ID
     * @param page   页码
     * @param size   每页大小
     * @return 日志分页结果
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<MonitorSpiderLog> getTaskLogsPage(Integer taskId, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MonitorSpiderLog> pageParam = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<MonitorSpiderLog> wrapper = new LambdaQueryWrapper<MonitorSpiderLog>()
                .eq(MonitorSpiderLog::getSpiderTaskId, taskId.longValue())
                .orderByDesc(MonitorSpiderLog::getSpiderLogTime);
        return spiderLogMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取任务日志（兼容旧接口）
     *
     * @param taskId 任务ID
     * @param limit  限制数量
     * @return 日志列表
     */
    public List<Map<String, Object>> getTaskLogs(Integer taskId, int limit) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MonitorSpiderLog> pageResult = getTaskLogsPage(taskId, 1, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (MonitorSpiderLog log : pageResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("level", log.getSpiderLogLevel());
            map.put("message", log.getSpiderLogMessage());
            map.put("url", log.getSpiderLogUrl());
            map.put("time", log.getSpiderLogTime() != null ? log.getSpiderLogTime().toString() : null);
            result.add(map);
        }
        return result;
    }

    /**
     * 获取任务爬取的数据（分页查询数据库）
     *
     * @param taskId 任务ID
     * @param page   页码
     * @param size   每页大小
     * @return 数据分页结果
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<MonitorSpiderData> getTaskDataPage(Integer taskId, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MonitorSpiderData> pageParam = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<MonitorSpiderData> wrapper = new LambdaQueryWrapper<MonitorSpiderData>()
                .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue())
                .orderByDesc(MonitorSpiderData::getCrawlTime);
        return spiderDataMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取任务爬取的数据（兼容旧接口）
     *
     * @param taskId 任务ID
     * @return 数据列表
     */
    public List<Map<String, Object>> getTaskData(Integer taskId) {
        List<MonitorSpiderData> dataList = spiderDataMapper.selectList(
                new LambdaQueryWrapper<MonitorSpiderData>()
                        .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue())
                        .orderByDesc(MonitorSpiderData::getCrawlTime)
                        .last("LIMIT 1000")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (MonitorSpiderData data : dataList) {
            try {
                Map<String, Object> map = objectMapper.readValue(data.getSpiderDataContent(), new TypeReference<>() {});
                map.put("_id", data.getSpiderDataId());
                map.put("_url", data.getSpiderDataUrl());
                map.put("_crawlTime", data.getCrawlTime() != null ? data.getCrawlTime().toString() : null);
                result.add(map);
            } catch (Exception e) {
                log.warn("Failed to parse spider data content: {}", data.getSpiderDataId(), e);
            }
        }
        return result;
    }

    /**
     * 清空任务数据（从数据库删除）
     *
     * @param taskId 任务ID
     */
    public void clearTaskData(Integer taskId) {
        spiderDataMapper.delete(new LambdaQueryWrapper<MonitorSpiderData>()
                .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue()));
        spiderLogMapper.deleteByTaskId(taskId.longValue());
        // 清除内存缓存
        spiderData.remove(taskId);
        spiderLogs.remove(taskId);
    }

    /**
     * 删除单条数据
     *
     * @param dataId 数据ID
     * @return 是否删除成功
     */
    public boolean deleteDataById(Long dataId) {
        return spiderDataMapper.deleteById(dataId) > 0;
    }

    /**
     * 爬虫完成回调
     *
     * @param taskId 任务ID
     */
    private void onSpiderComplete(Integer taskId) {
        try {
            // 从运行列表中移除
            runningSpiders.remove(taskId);

            // 清理速率限制器
            rateLimiters.remove(taskId);

            // 清理深度跟踪器
            depthTrackers.remove(taskId);

            // 更新任务状态
            MonitorSpiderTask task = spiderTaskService.getById(taskId);
            if (task != null) {
                task.setSpiderTaskRunStatus("IDLE");
                spiderTaskService.updateById(task);
            }

            // 记录完成日志
            addLog(taskId, "INFO", "爬虫任务执行完成", null);
            log.info("Spider task {} completed", taskId);
        } catch (Exception e) {
            log.error("Failed to handle spider complete for task {}", taskId, e);
        }
    }

    /**
     * 添加日志（保存到数据库）
     */
    private void addLog(Integer taskId, String level, String message, String url) {
        try {
            MonitorSpiderLog logEntity = new MonitorSpiderLog();
            logEntity.setSpiderTaskId(taskId.longValue());
            logEntity.setSpiderLogLevel(level);
            logEntity.setSpiderLogMessage(message);
            logEntity.setSpiderLogUrl(url);
            logEntity.setSpiderLogTime(LocalDateTime.now());
            spiderLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("Failed to save spider log to database", e);
        }

        // 同时保存到内存缓存（用于实时查看）
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("level", level);
        logEntry.put("message", message);
        logEntry.put("url", url);
        logEntry.put("time", LocalDateTime.now().toString());

        List<Map<String, Object>> logs = spiderLogs.get(taskId);
        if (logs != null) {
            logs.add(logEntry);
            // 保持最近1000条日志
            if (logs.size() > 1000) {
                logs.remove(0);
            }
        }
    }

    /**
     * 解析提取规则
     */
    private List<ExtractRule> parseExtractRules(String rulesJson) {
        if (rulesJson == null || rulesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(rulesJson, new TypeReference<List<ExtractRule>>() {});
        } catch (Exception e) {
            log.error("Failed to parse extract rules", e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算数据哈希
     */
    private String calculateHash(Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(json.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    @PreDestroy
    public void destroy() {
        // 停止所有运行中的爬虫
        for (Map.Entry<Integer, Spider> entry : runningSpiders.entrySet()) {
            try {
                entry.getValue().stop();
            } catch (Exception e) {
                log.error("Failed to stop spider {}", entry.getKey(), e);
            }
        }
        runningSpiders.clear();
    }

    /**
     * 提取规则
     */
    public static class ExtractRule {
        private String name;
        private String selector;
        private String type;
        private boolean multi;
        private boolean required;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSelector() { return selector; }
        public void setSelector(String selector) { this.selector = selector; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isMulti() { return multi; }
        public void setMulti(boolean multi) { this.multi = multi; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
    }

    /**
     * 动态页面处理器
     */
    private class DynamicPageProcessor implements PageProcessor {
        private final MonitorSpiderTask task;
        private final List<ExtractRule> extractRules;
        private final Site site;

        public DynamicPageProcessor(MonitorSpiderTask task, List<ExtractRule> extractRules) {
            this.task = task;
            this.extractRules = extractRules;
            this.site = buildSite(task);
        }

        /**
         * 构建Site配置，应用高级配置
         */
        private Site buildSite(MonitorSpiderTask task) {
            Site site = Site.me()
                    .setRetryTimes(task.getSpiderTaskRetryTimes() != null ? task.getSpiderTaskRetryTimes() : 3)
                    .setSleepTime(task.getSpiderTaskSleepTime() != null ? task.getSpiderTaskSleepTime() : 1000)
                    .setTimeOut(task.getSpiderTaskTimeout() != null ? task.getSpiderTaskTimeout() * 1000 : 30000);

            // 应用User-Agent
            String userAgent = task.getSpiderTaskUserAgent();
            if (userAgent != null && !userAgent.isEmpty()) {
                site.setUserAgent(userAgent);
            } else {
                site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            }

            // 应用Headers
            String headersJson = task.getSpiderTaskHeaders();
            if (headersJson != null && !headersJson.isEmpty()) {
                try {
                    Map<String, String> headers = objectMapper.readValue(headersJson, new TypeReference<Map<String, String>>() {});
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        site.addHeader(entry.getKey(), entry.getValue());
                    }
                    addLog(task.getSpiderTaskId(), "INFO", "已应用自定义Headers: " + headers.size() + "个", null);
                } catch (Exception e) {
                    addLog(task.getSpiderTaskId(), "WARN", "解析Headers配置失败: " + e.getMessage(), null);
                }
            }

            // 应用Cookies
            String cookiesStr = task.getSpiderTaskCookies();
            if (cookiesStr != null && !cookiesStr.isEmpty()) {
                try {
                    // 支持两种格式: JSON对象或cookie字符串
                    if (cookiesStr.trim().startsWith("{")) {
                        Map<String, String> cookies = objectMapper.readValue(cookiesStr, new TypeReference<Map<String, String>>() {});
                        for (Map.Entry<String, String> entry : cookies.entrySet()) {
                            site.addCookie(entry.getKey(), entry.getValue());
                        }
                    } else {
                        // 解析 "key1=value1; key2=value2" 格式
                        String[] pairs = cookiesStr.split(";\\s*");
                        for (String pair : pairs) {
                            String[] kv = pair.split("=", 2);
                            if (kv.length == 2) {
                                site.addCookie(kv[0].trim(), kv[1].trim());
                            }
                        }
                    }
                    addLog(task.getSpiderTaskId(), "INFO", "已应用Cookies配置", null);
                } catch (Exception e) {
                    addLog(task.getSpiderTaskId(), "WARN", "解析Cookies配置失败: " + e.getMessage(), null);
                }
            }

            // 应用代理配置
            String proxyJson = task.getSpiderTaskProxy();
            if (proxyJson != null && !proxyJson.isEmpty()) {
                try {
                    Map<String, Object> proxyConfig = objectMapper.readValue(proxyJson, new TypeReference<Map<String, Object>>() {});
                    String host = (String) proxyConfig.get("host");
                    Integer port = proxyConfig.get("port") instanceof Number ? ((Number) proxyConfig.get("port")).intValue() : null;
                    if (host != null && !host.isEmpty() && port != null) {
                        // 使用反射设置代理，避免 API 兼容性问题
                        try {
                            java.lang.reflect.Method setProxyMethod = site.getClass().getMethod("setHttpProxy", org.apache.http.HttpHost.class);
                            setProxyMethod.invoke(site, new org.apache.http.HttpHost(host, port));
                            addLog(task.getSpiderTaskId(), "INFO", "已应用代理配置: " + host + ":" + port, null);
                        } catch (NoSuchMethodException e) {
                            addLog(task.getSpiderTaskId(), "WARN", "当前版本不支持代理配置", null);
                        }
                    }
                } catch (Exception e) {
                    addLog(task.getSpiderTaskId(), "WARN", "解析代理配置失败: " + e.getMessage(), null);
                }
            }

            return site;
        }

        @Override
        public void process(Page page) {
            Integer taskId = task.getSpiderTaskId();
            String currentUrl = page.getUrl().toString();

            // 应用速率限制
            RateLimiter limiter = rateLimiters.get(taskId);
            if (limiter != null) {
                limiter.acquire();
            }

            // 获取当前页面深度
            Map<String, Integer> depthMap = depthTrackers.get(taskId);
            int currentDepth = depthMap != null ? depthMap.getOrDefault(currentUrl, 0) : 0;

            Map<String, Object> extractedData = new HashMap<>();
            extractedData.put("_url", currentUrl);
            extractedData.put("_crawlTime", LocalDateTime.now().toString());
            extractedData.put("_depth", currentDepth);

            for (ExtractRule rule : extractRules) {
                try {
                    Object value = extractValue(page, rule);
                    if (value != null) {
                        extractedData.put(rule.getName(), value);
                    } else if (rule.isRequired()) {
                        addLog(taskId, "WARN",
                                "必填字段 " + rule.getName() + " 未提取到数据", currentUrl);
                    }
                } catch (Exception e) {
                    addLog(taskId, "ERROR",
                            "提取字段 " + rule.getName() + " 失败: " + e.getMessage(), currentUrl);
                }
            }

            // 设置提取结果
            for (Map.Entry<String, Object> entry : extractedData.entrySet()) {
                page.putField(entry.getKey(), entry.getValue());
            }

            // URL自动发现
            if (Boolean.TRUE.equals(task.getSpiderTaskEnableUrlDiscover())) {
                discoverUrls(page, currentDepth, taskId);
            }
        }

        /**
         * 发现并添加新的URL
         */
        private void discoverUrls(Page page, int currentDepth, Integer taskId) {
            Integer maxDepth = task.getSpiderTaskMaxDepth();
            // 检查深度限制
            if (maxDepth != null && maxDepth > 0 && currentDepth >= maxDepth) {
                return;
            }

            String urlPattern = task.getSpiderTaskUrlPattern();
            if (urlPattern == null || urlPattern.isEmpty()) {
                return;
            }

            try {
                Pattern pattern = Pattern.compile(urlPattern);
                // 提取页面中所有链接
                List<String> links = page.getHtml().links().all();
                int addedCount = 0;

                Map<String, Integer> depthMap = depthTrackers.get(taskId);
                for (String link : links) {
                    if (pattern.matcher(link).matches()) {
                        // 检查是否已爬取
                        if (depthMap != null && !depthMap.containsKey(link)) {
                            page.addTargetRequest(link);
                            depthMap.put(link, currentDepth + 1);
                            addedCount++;
                        }
                    }
                }

                if (addedCount > 0) {
                    addLog(taskId, "INFO", "发现新URL: " + addedCount + " 个 (深度:" + (currentDepth + 1) + ")", page.getUrl().toString());
                }
            } catch (Exception e) {
                addLog(taskId, "WARN", "URL发现失败: " + e.getMessage(), page.getUrl().toString());
            }
        }

        private Object extractValue(Page page, ExtractRule rule) {
            String type = rule.getType().toUpperCase();
            String selector = rule.getSelector();

            if ("XPATH".equals(type)) {
                if (rule.isMulti()) {
                    return page.getHtml().xpath(selector).all();
                } else {
                    return page.getHtml().xpath(selector).get();
                }
            } else if ("CSS".equals(type)) {
                if (rule.isMulti()) {
                    return page.getHtml().css(selector).all();
                } else {
                    return page.getHtml().css(selector).get();
                }
            } else if ("REGEX".equals(type)) {
                if (rule.isMulti()) {
                    return page.getHtml().regex(selector).all();
                } else {
                    return page.getHtml().regex(selector).get();
                }
            } else if ("JSON_PATH".equals(type)) {
                // 支持JSON_PATH选择器（用于JSON响应的API）
                try {
                    String rawText = page.getRawText();
                    if (rawText != null && rawText.trim().startsWith("{") || rawText.trim().startsWith("[")) {
                        Object result = JsonPath.read(rawText, selector);
                        if (rule.isMulti() && !(result instanceof List)) {
                            return Collections.singletonList(result);
                        }
                        return result;
                    }
                } catch (PathNotFoundException e) {
                    return null;
                } catch (Exception e) {
                    addLog(task.getSpiderTaskId(), "WARN", "JSON_PATH解析失败: " + e.getMessage(), page.getUrl().toString());
                }
            }

            return null;
        }

        @Override
        public Site getSite() {
            return site;
        }
    }

    /**
     * 数据管道
     */
    private class MonitorPipeline implements Pipeline {
        private final Integer taskId;

        public MonitorPipeline(Integer taskId) {
            this.taskId = taskId;
        }

        @Override
        public void process(ResultItems resultItems, com.chua.advanced.support.lang.spider.Task task) {
            Map<String, Object> data = resultItems.getAll();
            if (data.isEmpty()) {
                return;
            }

            // 计算哈希进行去重
            String hash = calculateHash(data);

            // 检查是否已存在相同哈希的数据（去重）
            MonitorSpiderData existing = spiderDataMapper.selectByHash(hash);
            if (existing != null) {
                addLog(taskId, "INFO", "数据已存在，跳过", (String) data.get("_url"));
                return;
            }

            // 保存到数据库
            try {
                MonitorSpiderData spiderData = new MonitorSpiderData();
                spiderData.setSpiderTaskId(taskId.longValue());
                spiderData.setSpiderDataContent(objectMapper.writeValueAsString(data));
                spiderData.setSpiderDataUrl((String) data.get("_url"));
                spiderData.setSpiderDataHash(hash);
                spiderData.setCrawlTime(LocalDateTime.now());
                spiderDataMapper.insert(spiderData);

                // 记录日志
                addLog(taskId, "INFO", "成功提取数据", (String) data.get("_url"));

                // 更新任务统计
                MonitorSpiderTask spiderTask = spiderTaskService.getById(taskId);
                if (spiderTask != null) {
                    spiderTask.setSpiderTaskTotalSuccess(spiderTask.getSpiderTaskTotalSuccess() + 1);
                    spiderTaskService.updateById(spiderTask);
                }
            } catch (Exception e) {
                log.error("Failed to save spider data to database", e);
                addLog(taskId, "ERROR", "保存数据失败: " + e.getMessage(), (String) data.get("_url"));
            }
        }
    }

    /**
     * 爬虫监听器
     */
    private class TaskSpiderListener implements com.chua.advanced.support.lang.spider.SpiderListener {
        private final Integer taskId;

        public TaskSpiderListener(Integer taskId) {
            this.taskId = taskId;
        }

        @Override
        public void onSuccess(com.chua.advanced.support.lang.spider.Request request) {
            addLog(taskId, "INFO", "页面下载成功", request.getUrl());
        }

        @Override
        public void onError(com.chua.advanced.support.lang.spider.Request request, Exception e) {
            addLog(taskId, "ERROR", "页面下载失败: " + e.getMessage(), request.getUrl());

            // 更新失败计数
            MonitorSpiderTask task = spiderTaskService.getById(taskId);
            if (task != null) {
                task.setSpiderTaskTotalFail(task.getSpiderTaskTotalFail() + 1);
                spiderTaskService.updateById(task);
            }
        }
    }

    /**
     * 速率限制器
     * 基于令牌桶算法实现的简单速率限制器
     */
    private static class RateLimiter {
        private final int permitsPerMinute;
        private final long intervalMillis;
        private long lastAcquireTime = 0;
        private final Object lock = new Object();

        public RateLimiter(int permitsPerMinute) {
            this.permitsPerMinute = permitsPerMinute;
            this.intervalMillis = 60000L / permitsPerMinute;
        }

        /**
         * 获取许可，必要时等待
         */
        public void acquire() {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                long waitTime = lastAcquireTime + intervalMillis - now;
                if (waitTime > 0) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                lastAcquireTime = System.currentTimeMillis();
            }
        }
    }
}
