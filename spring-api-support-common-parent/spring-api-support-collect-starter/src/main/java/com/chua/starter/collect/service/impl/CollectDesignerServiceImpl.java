package com.chua.starter.collect.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.crawler.support.SpiderFetcher;
import com.chua.crawler.support.fetcher.HttpFetcher;
import com.chua.crawler.support.model.SpiderRequest;
import com.chua.crawler.support.model.SpiderResponse;
import com.chua.starter.collect.engine.CollectBuiltinInvoker;
import com.chua.starter.collect.engine.CollectExecutorRegistry;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.entity.CollectFlow;
import com.chua.starter.collect.job.CollectFlowJobHandler;
import com.chua.starter.collect.mapper.CollectFlowMapper;
import com.chua.starter.collect.mapper.CollectTaskMapper;
import com.chua.starter.collect.properties.CollectProperties;
import com.chua.starter.collect.service.CollectDesignerService;
import com.chua.starter.collect.service.CollectExecutionService;
import com.chua.starter.collect.service.CollectFlowService;
import com.chua.starter.collect.service.CollectRecordService;
import com.chua.starter.collect.support.CollectCronSupport;
import com.chua.starter.collect.vo.CollectPreviewVO;
import com.chua.starter.collect.vo.CollectRuntimeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 设计器与运行时概览服务实现。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectDesignerServiceImpl implements CollectDesignerService {

    /**
     * 定时预览最多返回次数。
     */
    private static final int MAX_PREVIEW = 10;

    /**
     * 未删除条件值。
     */
    private static final Integer ALIVE = 0;

    /**
     * 启用调度条件值。
     */
    private static final Integer ENABLED = 1;

    /**
     * 节点执行器注册表。
     */
    private final CollectExecutorRegistry registry;

    /**
     * 平台配置。
     */
    private final CollectProperties properties;

    /**
     * 流程表访问。
     */
    private final CollectFlowMapper flowMapper;

    /**
     * 任务表访问。
     */
    private final CollectTaskMapper taskMapper;

    /**
     * 结果服务，取结果总数。
     */
    private final CollectRecordService recordService;

    /**
     * 执行服务，取本机执行中标识。
     */
    private final CollectExecutionService executionService;

    /**
     * 流程服务，取调度台账的登记情况。
     */
    private final CollectFlowService flowService;

    @Override
    public List<CollectShapeMeta> metas() {
        return registry.metas();
    }

    @Override
    public CollectRuntimeVO runtime() {
        CollectRuntimeVO vo = new CollectRuntimeVO();
        vo.setEnable(properties.isEnable());
        vo.setExecutorId(executionService.executorId());
        vo.setRunningTasks(executionService.runningCount());
        vo.setFlowCount(flowMapper.selectCount(Wrappers.<CollectFlow>lambdaQuery()
                .eq(CollectFlow::getCollectFlowIsDeleted, ALIVE)));
        vo.setEnabledFlowCount(flowMapper.selectCount(Wrappers.<CollectFlow>lambdaQuery()
                .eq(CollectFlow::getCollectFlowIsDeleted, ALIVE)
                .eq(CollectFlow::getCollectFlowStatus, ENABLED)));
        vo.setTaskCount(taskMapper.selectCount(null));
        vo.setRecordCount(recordService.countRecords(null));
        vo.setSchedulerGroup(CollectFlowJobHandler.JOB_GROUP);
        vo.setScheduledJobCount((long) flowService.scheduledJobCount());
        vo.setTaskTimeoutMs(properties.getTaskTimeoutMs());
        vo.setMaxRecordPerTask(properties.getMaxRecordPerTask());
        vo.setDefaultThreadCount(properties.getDefaultThreadCount());
        vo.setRequestTimeoutMs(properties.getRequestTimeoutMs());
        vo.setShapes(registry.metas());
        vo.setBuiltinFunctions(CollectBuiltinInvoker.builtinNames().stream().sorted().toList());
        return vo;
    }

    @Override
    public CollectPreviewVO previewFetch(String url, Integer timeoutMs) {
        String target = url == null ? "" : url.trim();
        if (target.isEmpty()) {
            throw new IllegalArgumentException("预览抓取需要填写 URL");
        }
        URI uri;
        try {
            uri = URI.create(target);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("URL 格式不正确: " + target);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("预览抓取只支持 http/https，收到: " + scheme);
        }
        if (blockedHost(uri.getHost())) {
            throw new IllegalArgumentException("出于安全考虑，预览抓取不允许访问内网或本机地址: " + uri.getHost());
        }
        int timeout = timeoutMs == null || timeoutMs <= 0 ? properties.getRequestTimeoutMs() : timeoutMs;
        SpiderFetcher fetcher = ServiceProvider.of(SpiderFetcher.class).getNewExtension("http");
        if (fetcher == null) {
            fetcher = new HttpFetcher(timeout);
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("timeoutMs", timeout);
        attributes.put("userAgent", properties.getUserAgent());
        SpiderRequest request = SpiderRequest.builder()
                .url(target)
                .method("GET")
                .headers(new LinkedHashMap<>())
                .attributes(attributes)
                .build();
        try {
            SpiderResponse response = fetcher.fetch(request);
            String html = response.getContent() == null ? "" : response.getContent();
            log.info("[collect] 预览抓取 {} → {} ({} 字符)", target, response.getStatusCode(), html.length());
            return new CollectPreviewVO(response.getStatusCode(), response.getContentType(), html.length(), html);
        } catch (Exception ex) {
            throw new IllegalStateException("预览抓取失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 判定预览抓取的目标是否落在外网之外。
     *
     * <p>这个接口会按调用方给的 URL 主动发请求，等于开了一个 SSRF 面：不拦的话可以拿它探
     * 内网端口与云元数据地址（169.254.169.254）。这里拦三类：域名白名单式后缀
     * （localhost/*.local/*.internal）、以及**域名解析后**的地址是否本机/内网/链路本地。</p>
     *
     * <p><b>已知局限</b>：解析与真正连接之间仍存在 DNS rebinding 的时间窗（解析到公网、
     * 连接时指回内网）。要彻底堵住得把校验下沉到连接的 socket 层，属独立改造。</p>
     *
     * @param host 目标主机名（已从 URL 取好）
     * @return true 表示必须拒绝
     */
    private static boolean blockedHost(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }
        String name = host.trim().toLowerCase(Locale.ROOT);
        if (name.equals("localhost") || name.endsWith(".localhost")
                || name.endsWith(".local") || name.endsWith(".internal")) {
            return true;
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(name)) {
                if (address.isLoopbackAddress() || address.isAnyLocalAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
                    return true;
                }
                byte[] raw = address.getAddress();
                // fc00::/7 唯一本地地址：isSiteLocalAddress 只覆盖 IPv4 私网
                if (raw.length == 16 && (raw[0] & 0xFE) == 0xFC) {
                    return true;
                }
            }
        } catch (UnknownHostException ex) {
            // 解析不了就当不安全：宁可让用户改用能解析的地址，也不放行未知目标
            return true;
        }
        return false;
    }

    @Override
    public List<LocalDateTime> cronPreview(String cron, int times) {
        CollectCronSupport.validate(cron);
        int count = Math.min(Math.max(times, 1), MAX_PREVIEW);
        long[] next = CollectCronSupport.nextList(cron, System.currentTimeMillis(), count);
        List<LocalDateTime> preview = new ArrayList<>();
        for (long millis : next) {
            preview.add(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
        }
        return preview;
    }
}
