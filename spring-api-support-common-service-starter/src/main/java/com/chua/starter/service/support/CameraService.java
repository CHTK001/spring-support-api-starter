package com.chua.starter.service.support;

import com.chua.starter.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.service.support.hikvision.result.DeviceResourceResourcesResult;
import com.chua.starter.service.support.hikvision.result.HikRegion;
import com.chua.starter.service.support.hikvision.result.OnOffline;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 摄像头服务接口，定义了摄像头相关的操作
 *
 * @author CH
 */
public interface CameraService {
    /**
     * 重新加载摄像头配置
     * 此方法用于在系统需要重新初始化摄像头设置时调用，例如在更改了摄像头参数或摄像头设备重新连接后
     */
    void reload();

    /**
     * 同步指定项目的摄像头配置
     *
     * @param sysProjectId 系统项目ID，用于标识需要同步摄像头配置的项目
     * @param resources    资源列表，用于标识需要同步的摄像头资源
     * @param consumer     消费者，用于处理摄像头同步结果
     * @return 返回true表示同步成功，false表示同步失败
     * 此方法用于确保摄像头配置与系统项目保持一致，特别是在多设备或网络环境下
     */
    boolean sync(Integer sysProjectId, Set<String> resources, Consumer<DeviceResourceResourcesResult> consumer);

    /**
     * 同步指定项目的组织架构
     *
     * @param sysProjectId 系统项目ID，用于标识需要同步组织架构的项目
     * @param consumer     消费者，用于处理组织架构同步结果
     * @return 返回true表示同步成功，false表示同步失败
     */
    boolean syncOrg(Integer sysProjectId, Consumer<HikRegion> consumer);

    /**
     * 获取指定摄像头的在线情况
     *
     * @param sysProjectId
     * @param sysCameraTemplateSerialNumber 摄像头模板序列号
     * @param sysCameraTemplateResourceType 摄像头模板资源类型
     * @return 返回摄像头的在线情况列表
     */
    List<SysDeviceCameraOnOffline> timeline(Integer sysProjectId, String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType);

    /**
     * 检查摄像头在线状态
     *
     * @param sysProjectId  系统项目ID，用于标识需要检查摄像头状态的项目
     * @param serialNumbers 摄像头序列号数组
     * @return 返回摄像头在线状态列表
     */
    List<OnOffline> checkOnline(Integer sysProjectId, String[] serialNumbers);

    /**
     * 获取摄像头预览地址
     *
     * @param sysProjectId          系统项目ID，用于标识需要获取摄像头预览地址的项目
     * @param sysDeviceSerialNumber 摄像头序列号
     * @param sysDeviceResourceType 摄像头资源类型
     * @param request               预览请求参数
     * @return 返回摄像头的预览地址
     */
    String previewUrl(Integer sysProjectId, String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request);
}
