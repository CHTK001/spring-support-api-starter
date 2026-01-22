package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chua.common.support.base.bean.BeanMap;
import com.chua.starter.project.service.support.hikvision.HiYunMouHttpClientResult;
import com.chua.starter.project.service.support.hikvision.HiYunYaoClientUtils;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.RyjzpFactory;
import com.chua.starter.project.service.support.hikvision.query.PeopleInfo;
import com.chua.starter.project.service.support.hikvision.result.PeopleSearchResult;
import com.chua.starter.project.service.support.hikvision.result.ResourceResult;
import com.chua.starter.entity.project.SysProject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云眸
 *
 * @author CH
 */
public class YunMouRyjzpFactory extends RyjzpFactory {
    private final SysProject sysProject;

    public YunMouRyjzpFactory(HikvisionClient hikvisionClient, SysProject sysProject) {
        super(hikvisionClient);
        this.sysProject = sysProject;
    }

    @Override
    public ResourceResult add(PeopleInfo peopleInfo) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("projectId", sysProject.getSysProjectSign());
        params.put("productCode", sysProject.getSysProjectSignCode());
        params.putAll(BeanMap.of(params));
        params.put("personId", peopleInfo.getPersonId());
        params.put("personLabelId", peopleInfo.getPersonId());
        params.put("personNum", peopleInfo.getPersonId());

        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/add", headers, params);
            JSONObject jsonObject = JSON.parseObject(result.getData());
            peopleInfo.setPersonId(jsonObject.getString("personId"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return new ResourceResult();
    }

    @Override
    public List<ResourceResult> remove(String[] personIds) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("projectId", sysProject.getSysProjectSign());
        params.put("productCode", sysProject.getSysProjectSignCode());
        params.put("personIds", personIds);

        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/delete", headers, params);
            System.out.print(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return Collections.emptyList();
    }

    @Override
    public PeopleSearchResult search(int page, int pageSize) {
        return super.search(page, pageSize);
    }

    @Override
    public String addFace(String personId, String faceData) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("projectId", sysProject.getSysProjectSign());
        params.put("productCode", sysProject.getSysProjectSignCode());
        params.put("faceData", faceData);
        params.put("personId", personId);

        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        HiYunMouHttpClientResult result = null;
        try {
            result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/upload", headers, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result.getData();
    }

    @Override
    public String deleteFace(String faceId) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("projectId", sysProject.getSysProjectSign());
        params.put("productCode", sysProject.getSysProjectSignCode());
        params.put("faceIds", new String[]{faceId});

        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/delete", headers, params);
            System.out.print(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
