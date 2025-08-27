package com.chua.starter.project.service.support.hikvision.yunyao;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.chua.common.support.bean.BeanUtils;
import com.chua.starter.project.service.support.hikvision.HiYunMouHttpClientResult;
import com.chua.starter.project.service.support.hikvision.HiYunYaoClient;
import com.chua.starter.project.service.support.hikvision.HiYunYaoClientUtils;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.project.service.support.hikvision.query.DoorQuery;
import com.chua.starter.project.service.support.hikvision.result.DoorEventResult;
import com.chua.starter.system.entity.SysProject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 海康云曜门禁
 *
 * @author CH
 */
@Slf4j
public class YunYaoEntranceFactory extends EntranceFactory {

    private final SysProject factory;
    private final String token;

    public YunYaoEntranceFactory(HiYunYaoClient hiYunYaoClient, SysProject factory, String token) {
        super(hiYunYaoClient);
        this.factory = factory;
        this.token = token;
    }

    @Override
    public DoorEventResult doorEvent(DoorQuery query) {

        Map<String, Object> params = new HashMap<>(8);
        params.put("projectId", factory.getSysProjectSign());
        params.put("productCode", factory.getSysProjectSignCode());
        params.put("pageNo", query.getPageNo());
        params.put("personType", "normal");
        params.put("pageSize", query.getPageSize());
        params.put("eventStartTime", query.getStartTime());
        params.put("eventEndTime", query.getEndTime());

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/aceventcs/v1/event/acs/person/page", headers, params);
        if (result != null && result.getCode() == 401) {
            log.error("pullMessages failed,result:{}", result);
        } else if (result != null && result.isSuccess()) {
            JSONObject jsonObject = JSON.parseObject(result.getData());
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
            DoorEventResult result1 = new DoorEventResult();
            List<DoorEventResult.ListDTO> list = new LinkedList<>();
            for (Object o : jsonArray) {
                JSONObject it = (JSONObject) o;
                DoorEventResult.ListDTO item = new DoorEventResult.ListDTO();
                BeanUtils.copyProperties(it, item);
                item.setPersonId(it.getString("personNum"));
                item.setDoorName(item.getDevName());
                item.setDevName(item.getDevName());
                item.setEventTime(it.getString("eventTime"));
                item.setEventName(it.getString("eventCodeStr"));
                item.setDevIndexCode(it.getString("devId"));
                item.setPicUri(it.getString("picUrl"));
                item.setOrgIndexCode(it.getString("orgId"));
                item.setOrgName(it.getString("orgPathName"));
                item.setInAndOutType(it.getInteger("inOrOut"));

                list.add(item);
            }

            result1.setList(list);
            return result1;
        } else {
            log.error("pullMessages failed,result:{}", result);
        }

        return null;
    }
}
