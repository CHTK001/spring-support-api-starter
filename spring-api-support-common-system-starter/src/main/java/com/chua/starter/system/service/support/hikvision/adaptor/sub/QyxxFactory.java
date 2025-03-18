package com.chua.starter.system.service.support.hikvision.adaptor.sub;


import com.alibaba.fastjson2.JSONObject;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.result.HikDeviceChannel;
import com.chua.starter.system.service.support.hikvision.result.HikRegion;

import java.util.Collections;
import java.util.List;

import static com.chua.starter.system.service.support.hikvision.HikvisionEnum.*;

/**
 * 区域信息
 *
 * @author CH
 */
public class QyxxFactory {

    protected final HikvisionClient hikvisionClient;

    public QyxxFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }

    /**
     * 获取组织机构
     *
     * @return 组织机构
     */
    public HikRegion root() {
        JSONObject json = new JSONObject();
        try {
            return hikvisionClient.executePost(REGION_ROOT, json, HikRegion.class);
        } catch (Exception e) {
            e.printStackTrace();
            return HikRegion.empty();
        }
    }

    /**
     * 获取组织机构
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @return 组织机构
     */
    public HikRegion regions(int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        try {
            return hikvisionClient.executePost(REGIONS, json, HikRegion.class);
        } catch (Exception e) {
            e.printStackTrace();
            return HikRegion.empty();
        }
    }

    /**
     * 获取组织机构
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @return 组织机构
     */
    public List<HikDeviceChannel> channel(String deviceSerialNumber, int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        try {
            hikvisionClient.executePost(REGIONS, json, HikRegion.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * 获取组织机构子机构
     *
     * @param parentIndexCode 父机构ID
     * @param resourceType    资源类型
     * @param authCodes       权限码集合
     * @param page            页码
     * @param pageSize        每页数量
     * @return 组织机构
     */
    public HikRegion regionSub(String parentIndexCode, String resourceType, String[] authCodes, int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("parentIndexCode", parentIndexCode);
        json.put("resourceType", resourceType);
        json.put("authCodes", authCodes);
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        try {
            return hikvisionClient.executePost(REGIONS_SUB, json, HikRegion.class);
        } catch (Exception e) {
            e.printStackTrace();
            return HikRegion.empty();
        }
    }

    /**
     * 根据区域获取组织机构
     *
     * @param regionId 机构ID
     * @return 组织机构
     */
    public HikRegion regionInfo(String[] regionId) {
        JSONObject json = new JSONObject();
        json.put("indexCodes", regionId);
        try {
            return hikvisionClient.executePost(REGIONS_INFO, json, HikRegion.class);
        } catch (Exception e) {
            e.printStackTrace();
            return HikRegion.empty();
        }
    }
}
