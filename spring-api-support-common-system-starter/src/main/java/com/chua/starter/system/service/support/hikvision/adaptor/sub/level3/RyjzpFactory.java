package com.chua.starter.system.service.support.hikvision.adaptor.sub.level3;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chua.common.support.bean.BeanMap;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.query.PeopleInfo;
import com.chua.starter.system.service.support.hikvision.result.PeopleSearchResult;
import com.chua.starter.system.service.support.hikvision.result.ResourceResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import static com.chua.starter.system.service.support.hikvision.HikvisionEnum.*;

/**
 * 人员及照片接口
 *
 * @author CH
 */
@Slf4j
public class RyjzpFactory {

    private final HikvisionClient hikvisionClient;

    public RyjzpFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }

    /**
     * 添加人员v2
     * 添加人员信息接口，注意，在安保基础数据配置的必选字段必须都包括在入参中。
     * 人员添加的时候，可以指定人员personId，不允许与其他人员personId重复，包括已删除的人员。
     * 本接口支持人员信息的扩展字段，按照属性定义key:value上传即可， 可通过获取资源属性接口，获取平台已启用的人员属性信息。
     *
     * @param peopleInfo 人员信息
     * @return 组织机构
     */
    public ResourceResult add(PeopleInfo peopleInfo) {
        JSONObject json = new JSONObject();
        json.putAll(BeanMap.create(peopleInfo));
        try {
            log.info("上报到海康参数\r\n{}", json.toJSONString());
            return hikvisionClient.executePost(PEOPLE_ADD, json, ResourceResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据编号删除人员，人员删除是软删除，可以在安保基础数据查询已删除人员。支持批量删除人员。删除人员将会同时删除人员关联的指纹和人脸信息。
     *
     * @param personIds
     * @return
     */
    public List<ResourceResult> remove(String[] personIds) {
        JSONObject json = new JSONObject();
        json.put("personIds", personIds);
        try {
            return JSON.parseArray(hikvisionClient.executePost(PEOPLE_DELETE, json, String.class)).toJavaList(ResourceResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 人员查询
     * 获取人员列表接口可用来全量同步人员信息，返回结果分页展示。本接口支持自定义属性的返回， 通过获取资源属性接口获取平台已支持人员属性的说明。
     *
     * @param page     页码(1)
     * @param pageSize 分页数量 (1000)
     * @return 组织机构
     */
    public PeopleSearchResult search(int page, int pageSize) {
        JSONObject json = new JSONObject();
        json.put("pageNo", page);
        json.put("pageSize", pageSize);
        try {
            return hikvisionClient.executePost(PEOPLE_SEARCH, json, PeopleSearchResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new PeopleSearchResult();
        }
    }

    public String addFace(String personId, String faceData) {
        JSONObject json = new JSONObject();
        json.put("personId", personId);
        json.put("faceData", faceData);
        return hikvisionClient.executePost(RESOURCE_FACE_ADD, json, String.class);
    }

    public String deleteFace(String faceId) {
        JSONObject json = new JSONObject();
        json.put("faceId", faceId);
        try {
            return hikvisionClient.executePost(RESOURCE_FACE_DELETE, json, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
