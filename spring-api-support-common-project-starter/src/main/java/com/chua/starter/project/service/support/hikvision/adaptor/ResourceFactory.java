package com.chua.starter.project.service.support.hikvision.adaptor;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.QyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.ZyxxFactory;
import com.chua.starter.project.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.project.service.support.hikvision.result.OnOffline;
import com.chua.starter.project.entity.SysDeviceCardEvent;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;

import java.util.List;

/**
 * 资源接口
 *
 * @author CH
 */
public class ResourceFactory {

    protected final HikvisionClient hikvisionClient;

    public ResourceFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }
    /*人员信息接口==================================================================================================*/

    /**
     * 获取区域信息接口
     *
     * @return 获取组织机构接口
     */
    public QyxxFactory getRegionInfoFactory() {
        return new QyxxFactory(hikvisionClient);
    }

    /**
     * 获取人员信息接口
     *
     * @return 获取组织机构接口
     */
    public YyxxFactory getPeopleFactory() {
        return new YyxxFactory(hikvisionClient);
    }

    /**
     * 获取资源信息接口
     *
     * @return 获取资源信息接口
     */
    public ZyxxFactory getZyxxFactory() {
        return new ZyxxFactory(hikvisionClient);
    }

    /**
     * 获取在线离线时间轴
     *
     * @return 获取组织机构接口
     */
    public List<SysDeviceCameraOnOffline> getOnOfflineTimeline(String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType) {
        return hikvisionClient.getOnOfflineTimeline(
                sysCameraTemplateSerialNumber,
                sysCameraTemplateResourceType
        );
    }

    /**
     * 检测设备在离线状态
     *
     * @return 获取组织机构接口
     */
    public List<OnOffline> checkOnOffline(String... sysCameraTemplateSerialNumbers) {
        return hikvisionClient.checkOnOffline(
                sysCameraTemplateSerialNumbers
        );
    }

    /**
     * 预览地址
     *
     * @param sysDeviceSerialNumber 设备序列号
     * @param sysDeviceResourceType 设备资源类型
     * @param request               请求
     * @return 获取组织机构接口
     */
    public String previewUrl(String sysDeviceSerialNumber,
                             String sysDeviceResourceType,
                             DevicePreviewUrlV1Request request) {
        return hikvisionClient.previewUrl(
                sysDeviceSerialNumber,
                sysDeviceResourceType,
                request
        );
    }

    /**
     * 获取门禁历史数据
     *
     * @param request 请求
     * @return 获取组织机构接口
     */
    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        return hikvisionClient.cardHistoryPage(request);
    }

    /**
     * 获取门禁历史数据
     *
     * @param request 请求
     * @return 获取组织机构接口
     */
    public boolean syncEventMessage(CardDevicePageV1Request request) {
        return hikvisionClient.syncEventMessage(request);
    }

//    /**
//     * 获取资源信息接口
//     *
//     * @return 获取资源接口
//     */
//    public ResourceFactory getResourceFactory() {
//        return new ResourceFactory(hikvisionClient);
//    }

}
