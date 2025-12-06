package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.chua.starter.project.service.support.hikvision.HiYunMouClient;
import com.chua.starter.project.service.support.hikvision.HiYunMouHttpClientResult;
import com.chua.starter.project.service.support.hikvision.HiYunMouMessage;
import com.chua.starter.project.service.support.hikvision.HiYunYaoClientUtils;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.project.service.support.hikvision.query.DoorQuery;
import com.chua.starter.project.service.support.hikvision.result.DoorEventResult;
import com.chua.starter.project.entity.SysProject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CH
 */
@Slf4j
public class YunMouEntranceFactory extends EntranceFactory {
    private final SysProject sysProject;

    public YunMouEntranceFactory(HiYunMouClient hiYunMouClient, SysProject sysProject) {
        super(hiYunMouClient);
        this.sysProject = sysProject;
    }

    @Override
    public DoorEventResult doorEvent(DoorQuery query) {

        Map<String, Object> params = new HashMap<>(6);
        params.put("projectId", sysProject.getSysProjectSign());
        params.put("productCode", sysProject.getSysProjectSignCode());
        params.put("pageNo", query.getPageNo());
        params.put("pageSize", query.getPageSize());
        params.put("eventStartTime", query.getStartTime());
        params.put("eventEndTime", query.getEndTime());

        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/aceventcs/v1/event/acs/person/page", headers, params);
        if (result != null && result.getCode() == 401) {
            log.error("pullMessages failed,result:{}", result);
        } else if (result != null && result.isSuccess()) {
            List<HiYunMouMessage> apiResult = JSON.parseObject(result.getData(), new TypeReference<List<HiYunMouMessage>>() {
            });
            log.info("apiResult:{}", apiResult);
        } else {
            log.error("pullMessages failed,result:{}", result);
        }

        return null;
    }
}
