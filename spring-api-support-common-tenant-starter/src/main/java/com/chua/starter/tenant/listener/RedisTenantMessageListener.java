package com.chua.starter.tenant.listener;

import com.chua.common.support.json.Json;
import com.chua.starter.system.pojo.tenant.SyncTenantData;
import com.chua.starter.tenant.TenantHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 消息接收
 *
 * @author CH
 */
public class RedisTenantMessageListener implements TenantMessageListener {

    @Value("${plugin.control.platform.name:system}")
    private String plantfrom;
    @Autowired(required = false)
    private TenantHandler tenantHandler;

    @Override
    public void listen(String message) {
        if (!"tenant".equalsIgnoreCase(plantfrom)) {
            return;
        }
        SyncTenantData syncTenantData = null;
        try {
            syncTenantData = Json.fromJson(new ObjectMapper().readValue(message, String.class), SyncTenantData.class);
        } catch (JsonProcessingException e) {
            syncTenantData = Json.fromJson(message, SyncTenantData.class);
        }
        switch (syncTenantData.getAction()) {
            case CREATE:
            case UPDATE:
                tenantHandler.saveOrUpdate(syncTenantData.getTenant());
                break;
            case DELETE:
                tenantHandler.delete(syncTenantData.getTenant());
                break;
        }
    }

}
