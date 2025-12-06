package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.alibaba.fastjson2.JSONObject;
import com.chua.common.support.utils.PageUtils;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.HikvisionEnum;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.QyxxFactory;
import com.chua.starter.project.service.support.hikvision.result.HikDeviceChannel;
import com.chua.starter.project.service.support.hikvision.result.HikRegion;
import com.chua.starter.project.service.support.hikvision.result.HikRegionCascade;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.DeviceChannel;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.OrgInfo;
import com.chua.starter.project.entity.SysProject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 云眸
 *
 * @author CH
 */
public class YunMouQyxxFactory extends QyxxFactory {
    private final SysProject sysProject;

    public YunMouQyxxFactory(HikvisionClient hikvisionClient, SysProject sysProject) {
        super(hikvisionClient);
        this.sysProject = sysProject;
    }

    @Override
    public HikRegion regions(int page, int pageSize) {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        List<OrgInfo> orgInfo = (List<OrgInfo>) hikvisionClient.executeGet(HikvisionEnum.YUN_MOU_GROUPS_LIST_ALL.getUrl(), headers, OrgInfo.class);
        HikRegion hikRegion = new HikRegion();
        hikRegion.setPageNo(page);
        hikRegion.setPageSize(pageSize);
        hikRegion.setTotal(orgInfo.size());
        hikRegion.setList(PageUtils.toPage(page, pageSize).page(orgInfo.stream().map(data -> {
            HikRegionCascade hikRegionCascade = new HikRegionCascade();
            hikRegionCascade.setIndexCode(data.getGroupId());
            hikRegionCascade.setName(data.getGroupName());
            hikRegionCascade.setRegionPath(data.getGroupNo());
            hikRegionCascade.setParentIndexCode(data.getParentId());
            return hikRegionCascade;
        }).collect(Collectors.toList())));

        return hikRegion;
    }


    @Override
    public List<HikDeviceChannel> channel(String deviceSerialNumber, int page, int pageSize) {
        Map<String, String> headers = new HashMap<>(1);
        pageSize = Math.min(pageSize, 999);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        try {
            DeviceChannel deviceChannel = hikvisionClient.executeGet(
                    "/api/v1/open/basic/channels/list?deviceSerial=%s&pageNo=%s&pageSize=%s".formatted(
                            deviceSerialNumber, page, pageSize
                    )
                    , new JSONObject(), headers, DeviceChannel.class);
            return deviceChannel.getRows().stream().map(data -> {
                HikDeviceChannel hikDeviceChannel = new HikDeviceChannel();
                hikDeviceChannel.setChannelNo(data.getChannelNo());
                hikDeviceChannel.setChannelName(data.getChannelName());
                hikDeviceChannel.setChannelType(data.getChannelType());
                hikDeviceChannel.setChannelId(data.getChannelId());
                hikDeviceChannel.setChannelStatus(data.getChannelStatus());
                hikDeviceChannel.setDeviceSerial(data.getDeviceSerial());
                hikDeviceChannel.setIsUse(data.getIsUse());
                hikDeviceChannel.setChildDeviceModel(data.getChildDeviceModel());
                hikDeviceChannel.setIpcSerial(data.getIpcSerial());
                return hikDeviceChannel;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
