package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import com.chua.starter.monitor.entity.MonitorApplication;
import com.chua.starter.monitor.mapper.MonitorAppMapper;
import com.chua.starter.monitor.pojo.OpenApi;
import com.chua.starter.monitor.service.app.MonitorAppService;
import com.chua.starter.mybatis.entity.Query;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * 监视器应用
 *
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
public class MonitorAppServiceImpl extends ServiceImpl<MonitorAppMapper, MonitorApplication> implements MonitorAppService {
    private final SyncServer syncServer;
    private final RestTemplate restTemplate;


    @Override
    public ReturnResult<MonitorApplication> detail(Integer monitorId) {
        MonitorApplication monitorApplication = getById(monitorId);
        if (null == monitorApplication) {
            return ReturnResult.error("数据不存在");
        }
        List<MonitorApplication> monitorApplications = Collections.singletonList(monitorApplication);
        mergePage(monitorApplications);
        return ReturnResult.ok(monitorApplications.getFirst());
    }

    @Override
    public IPage<MonitorApplication> pageFor(Query<MonitorApplication> page, MonitorApplication entity) {
        Page<MonitorApplication> page1 = page(page.createPage(), Wrappers.lambdaQuery(entity));
        mergePage(page1);
        return page1;
    }

    @Override
    public ReturnResult<List<MonitorApplication>> list(MonitorApplication entity) {
        List<MonitorApplication> page1 = list(Wrappers.lambdaQuery(entity));
        mergePage(page1);
        return ReturnResult.ok(page1);
    }

    @Override
    public ReturnResult<List<OpenApi>> render(Integer monitorId, String index) {
        MonitorApplication monitorApplication = getById(monitorId);
        if (null == monitorApplication) {
            return ReturnResult.error("数据不存在");
        }


        List<String> indexes = Splitter.on(",").trimResults().omitEmptyStrings().skipNull().splitToList(index);
        List<MonitorApplication> monitorApplications = Collections.singletonList(monitorApplication);
        mergePage(monitorApplications);
        List<Discovery> monitorRequests = monitorApplications.getFirst().getMonitorRequests();
        List<OpenApi> result = new ArrayList<>(monitorRequests.size());
        for (Discovery discovery : monitorRequests.stream().filter(it -> indexes.contains(it.getServerId())).collect(Collectors.toSet())) {
            if (null == discovery) {
                return ReturnResult.error("数据不存在");
            }
            String host = discovery.getHost();
            if (null == host) {
                return ReturnResult.error("数据不存在");
            }

            int port = discovery.getPort();
            String contextPath = MapUtils.getString(discovery.getMetadata(), "contextPath");
            MultiValueMap<String, String> headers = new HttpHeaders();
            String token = RequestUtils.getHeader(RequestUtils.getRequest(), "x-oauth-token");
            if (StringUtils.isEmpty(token)) {
                token = CookieUtil.getValue(RequestUtils.getRequest(), "x-oauth-cookie");
            }
            headers.add("x-oauth-token", token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> exchange = restTemplate.exchange("http://" + host + ":" + port + contextPath + "/v3/api-docs", HttpMethod.GET, httpEntity, String.class);
            String body = exchange.getBody();
            OpenApi openApi = Json.fromJson(body, OpenApi.class);
            result.add(openApi);
        }
        return ReturnResult.ok(result);
    }

    @Override
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "#application.monitorId", keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<Boolean> saveOrUpdateApplication(MonitorApplication application) {
        if(null != application.getMonitorId()) {
            return ReturnResult.guess(updateById(application));
        }
        return ReturnResult.guess(save(application));
    }

    @Override
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "#monitorId", keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<MonitorApplication> getByCacheId(Integer monitorId) {
        return ReturnResult.guess(this.getById(monitorId));
    }

    /**
     * 分页查询数据
     *
     * @param page 分页
     */
    private void mergePage(Page<MonitorApplication> page) {
        mergePage(page.getRecords());
    }

    /**
     * 分页查询数据
     *
     * @param page1 分页
     */
    private void mergePage(List<MonitorApplication> page1) {
        if (syncServer == null) {
            return;
        }
        
        // 从 SyncServer 获取所有客户端，转换为 Discovery 对象
        Map<String, ClientInfo> allClients = syncServer.getAllClients();
        Set<Discovery> discoveryAll = allClients.values().stream()
                .map(this::convertToDiscovery)
                .collect(Collectors.toSet());
        
        Map<String, List<Discovery>> applicationName = discoveryAll.stream()
                .collect(Collectors.groupingBy(it -> MapUtils.getString(it.getMetadata(), "applicationName", "unknown")));
        
        for (MonitorApplication record : page1) {
            record.setMonitorRequests(applicationName.get(record.getMonitorApplicationName()));
        }
    }

    /**
     * 将 ClientInfo 转换为 Discovery 对象
     *
     * @param clientInfo 客户端信息
     * @return Discovery 对象
     */
    private Discovery convertToDiscovery(ClientInfo clientInfo) {
        // 构建元数据（Discovery.metadata 类型为 Map<String, String>）
        Map<String, String> metadata = new HashMap<>();
        metadata.put("applicationName", clientInfo.getClientApplicationName());
        metadata.put("contextPath", clientInfo.getClientContextPath());
        metadata.put("hostname", clientInfo.getClientHostname());
        metadata.put("osName", clientInfo.getClientOsName());
        metadata.put("javaVersion", clientInfo.getClientJavaVersion());

        // 合并原有元数据
        if (clientInfo.getClientMetadata() != null) {
            clientInfo.getClientMetadata().forEach((k, v) -> metadata.put(k, String.valueOf(v)));
        }

        // 使用 Builder 模式构建 Discovery
        return Discovery.builder()
                .host(clientInfo.getClientIpAddress())
                .port(clientInfo.getClientPort())
                .serverId(clientInfo.getClientId())
                .metadata(metadata)
                .build();
    }
}
