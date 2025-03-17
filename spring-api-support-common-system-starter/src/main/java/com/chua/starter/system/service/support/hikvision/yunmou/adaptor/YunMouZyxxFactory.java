package com.chua.starter.system.service.support.hikvision.yunmou.adaptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chua.common.support.utils.PageUtils;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.adaptor.sub.ZyxxFactory;
import com.chua.starter.system.service.support.hikvision.result.DeviceResourceResourcesResult;
import com.chua.starter.system.service.support.hikvision.result.HikRegion;
import com.chua.starter.system.service.support.hikvision.result.HikRegionCascade;
import com.chua.starter.system.service.support.hikvision.yunmou.pojo.YunMouDeviceResourceResourcesResult;

import java.util.*;

import static com.chua.starter.system.service.support.hikvision.HikvisionEnum.YOU_MOU_DEVICE_RESOURCE_RESOURCES;

/**
 * 云眸
 *
 * @author CH
 */
public class YunMouZyxxFactory extends ZyxxFactory {

    private final SysProject sysProject;

    public YunMouZyxxFactory(HikvisionClient hikvisionClient, SysProject sysProject) {
        super(hikvisionClient);
        this.sysProject = sysProject;
    }

    @Override
    public DeviceResourceResourcesResult deviceResourceResources(int page, int pageSize, String resourceType) {
        pageSize = Math.min(pageSize, 999);
        YunMouQyxxFactory qyxxFactory = new YunMouQyxxFactory(hikvisionClient, sysProject);
        HikRegion hikRegion = qyxxFactory.regions(1, 999);
        List<HikRegionCascade> list = hikRegion.getList();
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        String total = hikvisionClient
                .executeGet("/api/v1/open/basic/devices/actions/deviceCount", new JSONObject(), headers, String.class);

        DeviceResourceResourcesResult deviceResourceResourcesResult = new DeviceResourceResourcesResult();
        deviceResourceResourcesResult.setPageNo(page);
        deviceResourceResourcesResult.setTotal(JSON.parseObject(total).getIntValue("totalCount"));
        deviceResourceResourcesResult.setPageSize(pageSize);
        List<DeviceResourceResourcesResult.ListDTO> temp = new ArrayList<>(pageSize);
        for (HikRegionCascade hikRegionCascade : list) {
            List<DeviceResourceResourcesResult.ListDTO> listDTOS = queryDeviceList(page, pageSize, hikRegionCascade);
            temp.addAll(listDTOS);
            if (temp.size() >= pageSize) {
                break;
            }

        }
        deviceResourceResourcesResult.setList(PageUtils.toPage(page, pageSize).page(temp));
        return deviceResourceResourcesResult;
    }

    private List<DeviceResourceResourcesResult.ListDTO> queryDeviceList(int page, int pageSize, HikRegionCascade hikRegionCascade) {
        try {
            JSONObject json = new JSONObject();
            json.put("pageNo", page);
            json.put("pageSize", pageSize);
            json.put("groupNo", hikRegionCascade.getRegionPath());
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
            YunMouDeviceResourceResourcesResult yunMouDeviceResourceResourcesResult = hikvisionClient
                    .executeGet(YOU_MOU_DEVICE_RESOURCE_RESOURCES.getUrl(), json, headers, YunMouDeviceResourceResourcesResult.class);
            DeviceResourceResourcesResult deviceResourceResourcesResult = new DeviceResourceResourcesResult();
            deviceResourceResourcesResult.setPageNo(page);
            deviceResourceResourcesResult.setPageSize(pageSize);

            return yunMouDeviceResourceResourcesResult.getRows().stream()
                    .map(item -> {
                        DeviceResourceResourcesResult.ListDTO listDTO = new DeviceResourceResourcesResult.ListDTO();
                        listDTO.setIndexCode(item.getDeviceSerial());
                        listDTO.setName(item.getDeviceName());
                        listDTO.setRegionIndexCode(hikRegionCascade.getIndexCode());
                        listDTO.setParentIndexCodes(hikRegionCascade.getParentIndexCode());
                        listDTO.setDeviceType(item.getDeviceModel());
                        listDTO.setVersion(item.getDeviceVersion());

                        return listDTO;
                    }).toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
