package com.chua.starter.tenant.listener;

import com.chua.common.support.json.Json;
import com.chua.starter.system.pojo.tenant.SyncTenantServiceData;
import com.chua.starter.tenant.TenantServiceHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static com.chua.starter.redis.support.RedisConfiguration.createObjectMapper;

/**
 * 消息接收
 *
 * @author CH
 */
public class RedisServiceMessageListener implements ServiceMessageListener {

    private final ObjectMapper objectMapper;
    @Value("${plugin.control.platform.name:system}")
    private String plantfrom;
    @Autowired(required = false)
    private TenantServiceHandler tenantServiceHandler;

    public RedisServiceMessageListener() {
        this.objectMapper = createObjectMapper();
    }

    @Override
    public void listen(String message) {
        if (!"tenant".equalsIgnoreCase(plantfrom)) {
            return;
        }
        SyncTenantServiceData syncTenantServiceData = null;
        try {
            syncTenantServiceData = Json.fromJson(new ObjectMapper().readValue(message, String.class), SyncTenantServiceData.class);
        } catch (JsonProcessingException e) {
            syncTenantServiceData = Json.fromJson(message, SyncTenantServiceData.class);
        }
        switch (syncTenantServiceData.getAction()) {
            case CREATE:
            case UPDATE:
                tenantServiceHandler.saveOrUpdate(syncTenantServiceData.getSysTenantId(), syncTenantServiceData.getMenus());
                break;
            case DELETE:
                tenantServiceHandler.delete(syncTenantServiceData.getSysTenantId(), syncTenantServiceData.getMenus());
                break;
        }
    }
}
