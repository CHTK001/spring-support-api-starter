package com.chua.starter.system.service.support.hikvision.adaptor.sub;

import com.alibaba.fastjson2.JSONObject;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.result.DeviceResourceResourcesResult;

import static com.chua.starter.system.service.support.hikvision.HikvisionEnum.DEVICE_RESOURCE_RESOURCES;

/**
 * 资源信息
 *
 * @author CH
 */
public class ZyxxFactory {

    private final HikvisionClient hikvisionClient;

    public ZyxxFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }

    /**
     * 根据资源类型分页获取资源列表，主要用于资源信息的全量同步。
     *
     * @param page
     * @param pageSize
     * @return
     */
    public DeviceResourceResourcesResult deviceResourceResources(int page, int pageSize, String resourceType) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        json.put("resourceType", resourceType);
        try {
            return hikvisionClient.executePost(DEVICE_RESOURCE_RESOURCES, json, DeviceResourceResourcesResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new DeviceResourceResourcesResult();
        }
    }
}
