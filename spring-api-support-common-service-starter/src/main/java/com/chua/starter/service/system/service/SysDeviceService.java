package com.chua.starter.service.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.service.support.hikvision.result.OnOffline;
import com.chua.starter.system.entity.SysDevice;
import com.chua.starter.system.entity.SysDeviceCameraRecord;
import com.chua.starter.system.entity.SysDeviceOrg;
import com.chua.starter.system.pojo.device.CheckDeviceOnlineV1Request;
import com.chua.starter.system.pojo.device.DevicePageV1Request;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;
import com.chua.starter.system.pojo.device.DeviceTimelineV1Request;
import com.chua.starter.system.pojo.project.SyncV1Request;
import com.chua.starter.system.pojo.project.TemplatePageV1Request;

import java.util.List;

/**
 * 设备摄像头服务接口
 * 继承自 IService，专注于SysDeviceCamera实体的服务
 *
 * @author CH
 */
public interface SysDeviceService extends IService<SysDevice> {

    /**
     * 分页查询设备摄像头日志记录
     *
     * @param request 分页查询请求对象，包含查询参数和分页信息
     * @return 返回封装了SysDeviceCameraRecord实体的分页对象
     */
    IPage<SysDeviceCameraRecord> pageForLog(TemplatePageV1Request request);

    /**
     * 分页查询短信模板
     *
     * @param request 分页查询请求对象，包含查询参数和分页信息
     * @return 返回封装了SysSmsTemplate实体的分页对象
     */
    IPage<SysDevice> pageFor(TemplatePageV1Request request);

    /**
     * 同步模板信息
     *
     * @param request 同步请求对象，包含需要同步的模板信息
     * @return 返回同步操作的结果，包括是否成功
     */
    ReturnResult<Boolean> syncTemplate(SyncV1Request request);

    /**
     * 删除设备摄像头信息
     *
     * @param sysDevice 待删除的SysDeviceCamera实体对象
     * @return 返回删除操作的结果，包括是否成功
     */
    ReturnResult<Boolean> deleteFor(SysDevice sysDevice);

    /**
     * 更新设备摄像头信息
     *
     * @param sysDevice 待更新的SysDeviceCamera实体对象
     * @return 返回更新操作的结果，包括是否成功
     */
    ReturnResult<Boolean> updateFor(SysDevice sysDevice);

    /**
     * 保存设备摄像头信息
     *
     * @param sysDevice 待保存的SysDeviceCamera实体对象
     * @return 返回保存操作的结果，包括保存后的SysDeviceCamera实体
     */
    ReturnResult<SysDevice> saveFor(SysDevice sysDevice);

    /**
     * 同步组织信息
     *
     * @param request 同步请求对象，包含需要同步的组织信息
     * @return 返回同步操作的结果，包括是否成功
     */
    ReturnResult<Boolean> syncOrg(SyncV1Request request);

    /**
     * 获取组织列表
     *
     * @param request 同步请求对象，包含需要同步的组织信息
     * @return 返回同步操作的结果，包括是否成功
     */
    ReturnResult<List<SysDeviceOrg>> listOrg(SyncV1Request request);

    /**
     * 获取设备在线情况
     *
     * @param request 同步请求对象，包含需要同步的组织信息
     * @return 返回同步操作的结果，包括是否成功
     */
    ReturnResult<List<SysDeviceCameraOnOffline>> timeline(DeviceTimelineV1Request request);

    /**
     * 检查设备在线情况
     *
     * @param request 同步请求对象，包含需要同步的组织信息
     * @return 返回同步操作的结果，包括是否成功
     */
    ReturnResult<List<OnOffline>> check(CheckDeviceOnlineV1Request request);

    /**
     * 获取设备预览地址
     *
     * @param request 同步请求对象，包含需要同步的组织信息
     * @return 返回同步操作的结果，包括是否成功
     */
    ReturnResult<String> previewUrl(DevicePreviewUrlV1Request request);

    /**
     * 分页查询设备
     *
     * @param request 分页查询请求对象，包含查询参数和分页信息
     * @return 返回封装了SysDeviceCameraRecord实体的分页对象
     */
    IPage<SysDevice> pageFor(DevicePageV1Request request);
}

