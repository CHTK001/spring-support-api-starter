package com.chua.starter.service.support.hikvision.yunyao;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chua.common.support.bean.BeanMap;
import com.chua.starter.service.support.hikvision.HiYunMouHttpClientResult;
import com.chua.starter.service.support.hikvision.HiYunYaoClientUtils;
import com.chua.starter.service.support.hikvision.HikvisionClient;
import com.chua.starter.service.support.hikvision.adaptor.sub.level3.RyjzpFactory;
import com.chua.starter.service.support.hikvision.query.PeopleInfo;
import com.chua.starter.service.support.hikvision.result.PeopleSearchResult;
import com.chua.starter.service.support.hikvision.result.ResourceResult;
import com.chua.starter.system.entity.SysProject;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 域控人脸
 *
 * @author CH
 */
@Slf4j
public class YunYaoRyjzpFactory extends RyjzpFactory {
    private final SysProject factory;
    private final String token;

    public YunYaoRyjzpFactory(HikvisionClient client, SysProject factory, String token) {
        super(client);
        this.factory = factory;
        this.token = token;
    }

    @Override
    public ResourceResult add(PeopleInfo peopleInfo) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("projectId", factory.getSysProjectSign());
        params.put("productCode", factory.getSysProjectSignCode());
        params.putAll(BeanMap.of(peopleInfo));
        params.put("personId", peopleInfo.getPersonId());
        params.put("personLabelId", "1816707257544");
        params.put("personNum", peopleInfo.getPersonId());
        params.put("orgId", peopleInfo.getOrgIndexCode());
        params.put("phone", peopleInfo.getPhoneNo());
        params.put("personType", "normal");

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/add", headers, params);
            JSONObject jsonObject = JSON.parseObject(result.getData());
            log.info("{}", jsonObject.getString("msg"));
            peopleInfo.setPersonId(jsonObject.getJSONObject("data").getString("personId"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return new ResourceResult();
    }

    @Override
    public List<ResourceResult> remove(String[] personIds) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("projectId", factory.getSysProjectSign());
        params.put("productCode", factory.getSysProjectSignCode());
        params.put("personIds", personIds);

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/delete", headers, params);
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
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", factory.getSysProjectSign());
        params.put("productCode", factory.getSysProjectSignCode());
        params.put("faceData", faceData);
        params.put("personId", personId);

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        HiYunMouHttpClientResult result = null;
        try {
            result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/upload", headers, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(result.getData());
        log.info("{} : {}", personId, jsonObject.getString("msg"));
        return jsonObject.getJSONObject("data").toJSONString();
    }

    @Override
    public String deleteFace(String faceId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", factory.getSysProjectSign());
        params.put("productCode", factory.getSysProjectSignCode());
        params.put("faceIds", new String[]{faceId});

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
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
