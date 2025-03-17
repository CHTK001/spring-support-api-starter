package com.chua.starter.system.service.support.hikvision;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.system.entity.SysDeviceCardEvent;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;
import com.chua.starter.system.service.support.hikvision.adaptor.CarFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.FaceFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.MetroCardFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.system.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.system.service.support.hikvision.result.OnOffline;

import java.util.List;

/**
 * @author CH
 * <p>
 * Client接口定义了一组工厂获取方法，用于提供对不同类型工厂的访问。
 * 通过此接口，客户端可以获取各种工厂的实例，以创建和管理不同类型的对象。
 */
public interface Client {
    /**
     * 获取用于创建汽车对象的CarFactory实例。
     *
     * @return 返回CarFactory实例。
     */
    CarFactory getCarFactory();

    /**
     * 获取用于创建资源对象的ResourceFactory实例。
     *
     * @return 返回ResourceFactory实例。
     */
    ResourceFactory getResourceFactory();

    /**
     * 获取用于创建人脸相关对象的FaceFactory实例。
     *
     * @return 返回FaceFactory实例。
     */
    FaceFactory getFaceFactory();

    /**
     * 获取用于创建地铁卡对象的MetroCardFactory实例。
     *
     * @return 返回MetroCardFactory实例。
     */
    MetroCardFactory getMetroCardFactory();

    /**
     * 获取用于创建入口对象的EntranceFactory实例。
     *
     * @return 返回EntranceFactory实例。
     */
    EntranceFactory getEntranceFactory();

    /**
     * 在离线情况
     *
     * @param sysCameraTemplateSerialNumber 序列号
     * @param sysCameraTemplateResourceType 离线
     * @return 结果
     */
    List<SysDeviceCameraOnOffline> getOnOfflineTimeline(String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType);

    /**
     * 检查离线情况
     *
     * @param sysCameraTemplateSerialNumbers 序列号
     * @return 结果
     */
    List<OnOffline> checkOnOffline(String[] sysCameraTemplateSerialNumbers);

    /**
     * 获取预览URL
     *
     * @param sysDeviceSerialNumber 设备序列号
     * @param sysDeviceResourceType 资源类型
     * @param request               请求
     * @return 结果
     */
    String previewUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request);

    /**
     * 获取录像URL
     * 地址的类型，1-预览，2-本地录像回放，3-云存储录像回放，非必选，默认为1；回放仅支持rtmp、ezopen、flv协议
     *
     * @param sysDeviceSerialNumber 设备序列号
     * @param sysDeviceResourceType 资源类型
     * @param request               请求
     * @return 结果
     */
    String recordUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request);

    /**
     * 获取门禁历史数据
     *
     * @param request 请求
     * @return 获取组织机构接口
     */
    IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request);

    /**
     * 同步门禁历史数据
     *
     * @param request 请求
     * @return 获取组织机构接口
     */
    boolean syncEventMessage(CardDevicePageV1Request request);
}
