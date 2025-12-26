package com.chua.starter.monitor.service.spider.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.PageResult;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.entity.MonitorSpiderData;
import com.chua.starter.monitor.entity.MonitorSpiderLog;
import com.chua.starter.monitor.entity.MonitorSpiderTask;
import com.chua.starter.monitor.mapper.spider.MonitorSpiderDataMapper;
import com.chua.starter.monitor.mapper.spider.MonitorSpiderLogMapper;
import com.chua.starter.monitor.mapper.spider.MonitorSpiderTaskMapper;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.SpiderExecutorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 爬虫任务服务实现类
 *
 * @author CH
 */
@Service
public class MonitorSpiderTaskServiceImpl extends ServiceImpl<MonitorSpiderTaskMapper, MonitorSpiderTask>
        implements MonitorSpiderTaskService {

    @Autowired
    @Lazy
    private SpiderExecutorService spiderExecutorService;

    @Autowired
    private MonitorSpiderDataMapper spiderDataMapper;

    @Autowired
    private MonitorSpiderLogMapper spiderLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ReturnResult<Map<String, Object>> previewUrl(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();

            Map<String, Object> result = new HashMap<>();
            result.put("html", doc.outerHtml());
            result.put("title", doc.title());
            result.put("baseUri", doc.baseUri());

            return ReturnResult.ok(result);
        } catch (Exception e) {
            return ReturnResult.error("预览失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> parseHtml(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();

            Map<String, Object> result = new HashMap<>();
            result.put("tree", buildHtmlTree(doc.body(), "/html/body", ""));

            return ReturnResult.ok(result);
        } catch (Exception e) {
            return ReturnResult.error("解析HTML失败: " + e.getMessage());
        }
    }

    /**
     * 构建HTML树结构
     */
    private Map<String, Object> buildHtmlTree(Element element, String xpath, String cssPath) {
        Map<String, Object> node = new HashMap<>();
        node.put("tag", element.tagName());
        node.put("xpath", xpath);
        node.put("cssPath", buildCssPath(element));

        if (element.id() != null && !element.id().isEmpty()) {
            node.put("id", element.id());
        }

        String classNames = element.className();
        if (classNames != null && !classNames.isEmpty()) {
            node.put("class", classNames);
        }

        String text = element.ownText();
        if (text != null && !text.trim().isEmpty()) {
            node.put("text", text.trim().length() > 100 ? text.trim().substring(0, 100) + "..." : text.trim());
        }

        node.put("label", element.tagName() + 
                (element.id() != null && !element.id().isEmpty() ? "#" + element.id() : "") +
                (classNames != null && !classNames.isEmpty() ? "." + classNames.split(" ")[0] : ""));

        List<Map<String, Object>> children = new ArrayList<>();
        Elements childElements = element.children();
        Map<String, Integer> tagCount = new HashMap<>();

        for (Element child : childElements) {
            String childTag = child.tagName();
            int count = tagCount.getOrDefault(childTag, 0) + 1;
            tagCount.put(childTag, count);

            String childXpath = xpath + "/" + childTag + "[" + count + "]";
            children.add(buildHtmlTree(child, childXpath, ""));
        }

        if (!children.isEmpty()) {
            node.put("children", children);
        }

        return node;
    }

    /**
     * 构建CSS选择器路径
     */
    private String buildCssPath(Element element) {
        if (element.id() != null && !element.id().isEmpty()) {
            return "#" + element.id();
        }

        StringBuilder path = new StringBuilder();
        Element current = element;

        while (current != null && !current.tagName().equals("html")) {
            String selector = current.tagName();
            if (current.className() != null && !current.className().isEmpty()) {
                selector += "." + current.className().split(" ")[0];
            }
            path.insert(0, selector + " > ");
            current = current.parent();
        }

        return path.length() > 3 ? path.substring(0, path.length() - 3) : path.toString();
    }

    @Override
    public ReturnResult<List<String>> testSelector(String url, String selector, String type) {
        try {
            List<String> results = new ArrayList<>();

            if ("JSON_PATH".equalsIgnoreCase(type)) {
                // JSON_PATH 选择器 - 直接获取JSON响应
                String jsonContent = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(30000)
                        .ignoreContentType(true)
                        .execute()
                        .body();
                
                try {
                    Object result = com.jayway.jsonpath.JsonPath.read(jsonContent, selector);
                    if (result instanceof List) {
                        for (Object item : (List<?>) result) {
                            results.add(item != null ? item.toString() : "null");
                        }
                    } else {
                        results.add(result != null ? result.toString() : "null");
                    }
                } catch (com.jayway.jsonpath.PathNotFoundException e) {
                    return ReturnResult.ok(results); // 返回空结果
                }
            } else {
                // HTML 选择器
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(30000)
                        .get();

                if ("XPATH".equalsIgnoreCase(type)) {
                    // 使用 W3C DOM + XPath 进行真正的 XPath 查询
                    results = evaluateXPath(doc, selector);
                } else if ("CSS".equalsIgnoreCase(type)) {
                    Elements elements = doc.select(selector);
                    for (Element el : elements) {
                        results.add(el.text());
                    }
                } else if ("REGEX".equalsIgnoreCase(type)) {
                    String html = doc.html();
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(selector);
                    java.util.regex.Matcher matcher = pattern.matcher(html);
                    while (matcher.find()) {
                        results.add(matcher.group(matcher.groupCount() > 0 ? 1 : 0));
                    }
                }
            }

            return ReturnResult.ok(results);
        } catch (Exception e) {
            return ReturnResult.error("测试选择器失败: " + e.getMessage());
        }
    }

    /**
     * 使用 W3C DOM 执行 XPath 查询
     */
    private List<String> evaluateXPath(Document jsoupDoc, String xpathExpr) {
        List<String> results = new ArrayList<>();
        try {
            // 将 Jsoup Document 转换为 W3C Document
            org.w3c.dom.Document w3cDoc = new org.jsoup.helper.W3CDom().fromJsoup(jsoupDoc);
            
            // 创建 XPath 实例
            javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
            
            // 执行 XPath 查询
            javax.xml.xpath.XPathExpression expr = xpath.compile(xpathExpr);
            Object result = expr.evaluate(w3cDoc, javax.xml.xpath.XPathConstants.NODESET);
            
            if (result instanceof org.w3c.dom.NodeList) {
                org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) result;
                for (int i = 0; i < nodes.getLength(); i++) {
                    org.w3c.dom.Node node = nodes.item(i);
                    String text = node.getTextContent();
                    if (text != null && !text.trim().isEmpty()) {
                        results.add(text.trim());
                    }
                }
            }
        } catch (Exception e) {
            // 如果W3C XPath失败，尝试简化的XPath解析
            results = evaluateSimpleXPath(jsoupDoc, xpathExpr);
        }
        return results;
    }

    /**
     * 简化的 XPath 解析（支持常见模式）
     */
    private List<String> evaluateSimpleXPath(Document doc, String xpathExpr) {
        List<String> results = new ArrayList<>();
        try {
            // 元素标签匹配: //tagname
            if (xpathExpr.startsWith("//")) {
                String remaining = xpathExpr.substring(2);
                // 处理 //tagname/text()
                if (remaining.endsWith("/text()")) {
                    remaining = remaining.replace("/text()", "");
                }
                // 处理属性选择器 [@attr='value']
                String cssSelector = convertXPathToCss(remaining);
                Elements elements = doc.select(cssSelector);
                for (Element el : elements) {
                    results.add(el.text());
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return results;
    }

    /**
     * 将简单XPath转换为CSS选择器
     */
    private String convertXPathToCss(String xpath) {
        String css = xpath;
        // 处理属性选择器: [@id='xxx'] -> #xxx
        java.util.regex.Matcher idMatcher = java.util.regex.Pattern.compile("\\[@id=['\"]([^'\"]+)['\"]\\]").matcher(css);
        if (idMatcher.find()) {
            css = css.replaceAll("\\[@id=['\"][^'\"]+['\"]\\]", "#" + idMatcher.group(1));
        }
        // 处理class属性: [@class='xxx'] -> .xxx
        java.util.regex.Matcher classMatcher = java.util.regex.Pattern.compile("\\[@class=['\"]([^'\"]+)['\"]\\]").matcher(css);
        if (classMatcher.find()) {
            css = css.replaceAll("\\[@class=['\"][^'\"]+['\"]\\]", "." + classMatcher.group(1).replace(" ", "."));
        }
        // 处理其他属性: [@attr='value'] -> [attr='value']
        css = css.replaceAll("\\[@", "[");
        // 处理路径分隔符: / -> " "
        css = css.replace("/", " ").trim();
        return css;
    }

    @Override
    public ReturnResult<String> generateXpath(String url, String elementPath) {
        // 根据元素路径生成XPath表达式
        return ReturnResult.ok(elementPath);
    }

    @Override
    public ReturnResult<Boolean> runSpiderTask(Integer taskId) {
        MonitorSpiderTask task = getById(taskId);
        if (task == null) {
            return ReturnResult.error("任务不存在");
        }

        if (spiderExecutorService.isRunning(taskId)) {
            return ReturnResult.error("任务正在运行中");
        }

        // 更新任务状态
        task.setSpiderTaskRunStatus("RUNNING");
        task.setSpiderTaskLastRunTime(LocalDateTime.now());
        updateById(task);

        // 使用SpiderExecutorService执行任务
        spiderExecutorService.executeTask(task);

        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Boolean> stopSpiderTask(Integer taskId) {
        spiderExecutorService.stopTask(taskId);

        MonitorSpiderTask task = getById(taskId);
        if (task != null) {
            task.setSpiderTaskRunStatus("STOPPED");
            updateById(task);
        }

        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Map<String, Object>> getSpiderTaskStatus(Integer taskId) {
        MonitorSpiderTask task = getById(taskId);
        if (task == null) {
            return ReturnResult.error("任务不存在");
        }

        Map<String, Object> status = new HashMap<>();
        status.put("taskId", taskId);
        status.put("status", task.getSpiderTaskRunStatus());
        status.put("isRunning", spiderExecutorService.isRunning(taskId));
        status.put("lastRunTime", task.getSpiderTaskLastRunTime());
        status.put("totalSuccess", task.getSpiderTaskTotalSuccess());
        status.put("totalFail", task.getSpiderTaskTotalFail());

        return ReturnResult.ok(status);
    }

    @Override
    public ReturnPageResult<Map<String, Object>> getSpiderTaskLogs(Integer taskId, Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 20 : size;

        // 从数据库分页查询日志
        Page<MonitorSpiderLog> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<MonitorSpiderLog> wrapper = new LambdaQueryWrapper<MonitorSpiderLog>()
                .eq(MonitorSpiderLog::getSpiderTaskId, taskId.longValue())
                .orderByDesc(MonitorSpiderLog::getSpiderLogTime);
        Page<MonitorSpiderLog> pageResult = spiderLogMapper.selectPage(pageParam, wrapper);

        // 转换为 Map 列表
        List<Map<String, Object>> logs = new ArrayList<>();
        for (MonitorSpiderLog log : pageResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getSpiderLogId());
            map.put("level", log.getSpiderLogLevel());
            map.put("message", log.getSpiderLogMessage());
            map.put("url", log.getSpiderLogUrl());
            map.put("time", log.getSpiderLogTime() != null ? log.getSpiderLogTime().toString() : null);
            logs.add(map);
        }

        return ReturnPageResult.ok(PageResult.<Map<String, Object>>builder()
                .data(logs)
                .total(pageResult.getTotal())
                .pageNo(p)
                .pageSize(s)
                .build());
    }

    @Override
    public ReturnPageResult<Map<String, Object>> getSpiderTaskData(Integer taskId, Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;

        // 从数据库分页查询数据
        Page<MonitorSpiderData> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<MonitorSpiderData> wrapper = new LambdaQueryWrapper<MonitorSpiderData>()
                .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue())
                .orderByDesc(MonitorSpiderData::getCrawlTime);
        Page<MonitorSpiderData> pageResult = spiderDataMapper.selectPage(pageParam, wrapper);

        // 转换为 Map 列表
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (MonitorSpiderData data : pageResult.getRecords()) {
            try {
                Map<String, Object> map = objectMapper.readValue(data.getSpiderDataContent(), new TypeReference<>() {});
                map.put("_id", data.getSpiderDataId());
                map.put("_url", data.getSpiderDataUrl());
                map.put("_crawlTime", data.getCrawlTime() != null ? data.getCrawlTime().toString() : null);
                dataList.add(map);
            } catch (Exception e) {
                // 如果解析失败，返回基本信息
                Map<String, Object> map = new HashMap<>();
                map.put("_id", data.getSpiderDataId());
                map.put("_url", data.getSpiderDataUrl());
                map.put("_content", data.getSpiderDataContent());
                map.put("_crawlTime", data.getCrawlTime() != null ? data.getCrawlTime().toString() : null);
                dataList.add(map);
            }
        }

        return ReturnPageResult.ok(PageResult.<Map<String, Object>>builder()
                .data(dataList)
                .total(pageResult.getTotal())
                .pageNo(p)
                .pageSize(s)
                .build());
    }

    @Override
    public ReturnResult<String> exportSpiderTaskData(Integer taskId, String format) {
        // 返回下载路径，实际下载由downloadExportData处理
        return ReturnResult.ok("/v1/spider/download/" + taskId + "?format=" + format);
    }

    /**
     * 下载导出的数据
     */
    public void downloadExportData(Integer taskId, String format, HttpServletResponse response) throws IOException {
        MonitorSpiderTask task = getById(taskId);
        if (task == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 查询所有数据
        List<MonitorSpiderData> dataList = spiderDataMapper.selectList(
                new LambdaQueryWrapper<MonitorSpiderData>()
                        .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue())
                        .orderByDesc(MonitorSpiderData::getCrawlTime)
        );

        // 解析数据内容
        List<Map<String, Object>> records = new ArrayList<>();
        Set<String> allKeys = new LinkedHashSet<>();
        for (MonitorSpiderData data : dataList) {
            try {
                Map<String, Object> map = objectMapper.readValue(data.getSpiderDataContent(), new TypeReference<>() {});
                map.put("_url", data.getSpiderDataUrl());
                map.put("_crawlTime", data.getCrawlTime() != null ? data.getCrawlTime().toString() : "");
                allKeys.addAll(map.keySet());
                records.add(map);
            } catch (Exception e) {
                Map<String, Object> map = new HashMap<>();
                map.put("_content", data.getSpiderDataContent());
                map.put("_url", data.getSpiderDataUrl());
                map.put("_crawlTime", data.getCrawlTime() != null ? data.getCrawlTime().toString() : "");
                records.add(map);
            }
        }

        String fileName = "spider_data_" + taskId + "_" + System.currentTimeMillis();
        
        switch (format.toLowerCase()) {
            case "csv":
                exportToCsv(response, fileName, records, allKeys);
                break;
            case "excel":
                exportToExcel(response, fileName, records, allKeys);
                break;
            case "json":
            default:
                exportToJson(response, fileName, records);
                break;
        }
    }

    private void exportToJson(HttpServletResponse response, String fileName, List<Map<String, Object>> records) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".json", StandardCharsets.UTF_8));
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(records));
        }
    }

    private void exportToCsv(HttpServletResponse response, String fileName, List<Map<String, Object>> records, Set<String> allKeys) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".csv", StandardCharsets.UTF_8));
        
        // 添加BOM以支持Excel正确识别UTF-8
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            List<String> headers = new ArrayList<>(allKeys);
            
            // 写入表头
            writer.println(headers.stream().map(this::escapeCsv).collect(Collectors.joining(",")));
            
            // 写入数据
            for (Map<String, Object> record : records) {
                String line = headers.stream()
                        .map(key -> escapeCsv(String.valueOf(record.getOrDefault(key, ""))))
                        .collect(Collectors.joining(","));
                writer.println(line);
            }
        }
    }

    private void exportToExcel(HttpServletResponse response, String fileName, List<Map<String, Object>> records, Set<String> allKeys) throws IOException {
        // 简化实现：使用XML格式的Excel (可以被Excel打开)
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xls", StandardCharsets.UTF_8));
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            List<String> headers = new ArrayList<>(allKeys);
            
            // 写入XML格式的Excel
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<?mso-application progid=\"Excel.Sheet\"?>");
            writer.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");
            writer.println("<Worksheet ss:Name=\"Sheet1\"><Table>");
            
            // 写入表头
            writer.println("<Row>");
            for (String header : headers) {
                writer.println("<Cell><Data ss:Type=\"String\">" + escapeXml(header) + "</Data></Cell>");
            }
            writer.println("</Row>");
            
            // 写入数据
            for (Map<String, Object> record : records) {
                writer.println("<Row>");
                for (String header : headers) {
                    Object value = record.getOrDefault(header, "");
                    writer.println("<Cell><Data ss:Type=\"String\">" + escapeXml(String.valueOf(value)) + "</Data></Cell>");
                }
                writer.println("</Row>");
            }
            
            writer.println("</Table></Worksheet></Workbook>");
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    @Override
    public ReturnResult<Boolean> clearSpiderTaskData(Integer taskId) {
        // 从数据库删除数据和日志
        spiderDataMapper.delete(new LambdaQueryWrapper<MonitorSpiderData>()
                .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue()));
        spiderLogMapper.deleteByTaskId(taskId.longValue());

        // 重置任务统计
        MonitorSpiderTask task = getById(taskId);
        if (task != null) {
            task.setSpiderTaskTotalSuccess(0L);
            task.setSpiderTaskTotalFail(0L);
            updateById(task);
        }

        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Map<String, Object>> getSpiderTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总任务数
        long totalCount = count();
        stats.put("totalCount", totalCount);

        // 运行中的任务数
        long runningCount = count(new LambdaQueryWrapper<MonitorSpiderTask>()
                .eq(MonitorSpiderTask::getSpiderTaskRunStatus, "RUNNING"));
        stats.put("runningCount", runningCount);

        // 启用的任务数
        long enabledCount = count(new LambdaQueryWrapper<MonitorSpiderTask>()
                .eq(MonitorSpiderTask::getSpiderTaskStatus, 1));
        stats.put("enabledCount", enabledCount);

        // 今日爬取数据量
        Long todayDataCount = spiderDataMapper.countToday();
        stats.put("todayDataCount", todayDataCount != null ? todayDataCount : 0L);

        // 总数据量
        Long totalDataCount = spiderDataMapper.countTotal();
        stats.put("totalDataCount", totalDataCount != null ? totalDataCount : 0L);

        return ReturnResult.ok(stats);
    }

    @Override
    public ReturnResult<Boolean> deleteSpiderData(Long dataId) {
        int result = spiderDataMapper.deleteById(dataId);
        if (result > 0) {
            return ReturnResult.ok(true);
        }
        return ReturnResult.error("数据不存在或删除失败");
    }

    @Override
    public ReturnResult<Boolean> toggleTaskStatus(Integer taskId) {
        MonitorSpiderTask task = getById(taskId);
        if (task == null) {
            return ReturnResult.error("任务不存在");
        }

        // 切换状态: 0 -> 1, 1 -> 0
        Integer newStatus = task.getSpiderTaskStatus() == 1 ? 0 : 1;
        task.setSpiderTaskStatus(newStatus);
        updateById(task);

        return ReturnResult.ok(true);
    }

    /**
     * 批量运行任务
     */
    public ReturnResult<Map<String, Object>> batchRunTasks(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return ReturnResult.error("任务ID列表不能为空");
        }

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Integer taskId : taskIds) {
            try {
                ReturnResult<Boolean> result = runSpiderTask(taskId);
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
    public ReturnResult<Map<String, Object>> batchStopTasks(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return ReturnResult.error("任务ID列表不能为空");
        }

        int success = 0;
        int failed = 0;

        for (Integer taskId : taskIds) {
            try {
                stopSpiderTask(taskId);
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
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Map<String, Object>> batchDeleteTasks(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return ReturnResult.error("任务ID列表不能为空");
        }

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Integer taskId : taskIds) {
            try {
                // 先停止运行中的任务
                if (spiderExecutorService.isRunning(taskId)) {
                    spiderExecutorService.stopTask(taskId);
                }

                // 删除关联数据和日志
                spiderDataMapper.delete(new LambdaQueryWrapper<MonitorSpiderData>()
                        .eq(MonitorSpiderData::getSpiderTaskId, taskId.longValue()));
                spiderLogMapper.deleteByTaskId(taskId.longValue());

                // 删除任务
                removeById(taskId);
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

    /**
     * 批量切换任务状态
     */
    public ReturnResult<Map<String, Object>> batchToggleStatus(List<Integer> taskIds, Integer status) {
        if (taskIds == null || taskIds.isEmpty()) {
            return ReturnResult.error("任务ID列表不能为空");
        }

        int success = 0;
        int failed = 0;

        for (Integer taskId : taskIds) {
            try {
                MonitorSpiderTask task = getById(taskId);
                if (task != null) {
                    task.setSpiderTaskStatus(status);
                    updateById(task);
                    success++;
                } else {
                    failed++;
                }
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
     * 克隆任务
     */
    public ReturnResult<MonitorSpiderTask> cloneTask(Integer taskId) {
        MonitorSpiderTask sourceTask = getById(taskId);
        if (sourceTask == null) {
            return ReturnResult.error("源任务不存在");
        }

        MonitorSpiderTask newTask = new MonitorSpiderTask();
        BeanUtils.copyProperties(sourceTask, newTask);

        // 重置特定字段
        newTask.setSpiderTaskId(null);
        newTask.setSpiderTaskName(sourceTask.getSpiderTaskName() + " (副本)");
        newTask.setSpiderTaskStatus(0);  // 默认停用
        newTask.setSpiderTaskRunStatus("IDLE");
        newTask.setSpiderTaskTotalSuccess(0L);
        newTask.setSpiderTaskTotalFail(0L);
        newTask.setSpiderTaskLastRunTime(null);
        newTask.setSpiderTaskNextRunTime(null);
        newTask.setSpiderTaskJobId(null);
        newTask.setCreateTime(null);
        newTask.setUpdateTime(null);

        save(newTask);

        return ReturnResult.ok(newTask);
    }
}
