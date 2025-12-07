package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.date.DateTime;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.project.mapper.*;
import com.chua.starter.project.service.SysDeviceService;
import com.chua.starter.project.service.support.CameraService;
import com.chua.starter.project.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.project.service.support.hikvision.result.DeviceResourceResourcesResult;
import com.chua.starter.project.service.support.hikvision.result.HikRegionCascade;
import com.chua.starter.project.service.support.hikvision.result.OnOffline;
import com.chua.starter.project.entity.*;
import com.chua.starter.project.pojo.device.DevicePreviewUrlV1Request;
import com.chua.starter.project.pojo.project.SyncV1Request;
import com.chua.starter.project.pojo.project.TemplatePageV1Request;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;
import com.chua.starter.project.pojo.device.CheckDeviceOnlineV1Request;
import com.chua.starter.project.pojo.device.DevicePageV1Request;
import com.chua.starter.project.pojo.device.DeviceTimelineV1Request;
import com.github.yulichang.toolkit.MPJWrappers;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@RequiredArgsConstructor
public class SysDeviceServiceImpl extends ServiceImpl<SysDeviceCameraMapper, SysDevice> implements SysDeviceService {

    final SysDeviceCameraRecordMapper sysDeviceCameraRecordMapper;
    final SysDeviceOrgMapper sysDeviceOrgMapper;
    final SysProjectMapper sysProjectMapper;
    final SysDeviceChannelMapper sysDeviceChannelMapper;
    @Autowired(required = false)
    IptablesService iptablesService;

    @Override
    public IPage<SysDeviceCameraRecord> pageForLog(TemplatePageV1Request request) {
        return sysDeviceCameraRecordMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()),
                Wrappers.<SysDeviceCameraRecord>lambdaQuery()
                        .orderByDesc(SysDeviceCameraRecord::getCreateTime)
        );
    }

    @Override
    public IPage<SysDevice> pageFor(TemplatePageV1Request request) {
        return baseMapper.selectPage(new Page<>(request.getPage(), request.getPageSize()),
                MPJWrappers.<SysDevice>lambdaJoin()
                        .selectAll(SysDevice.class)
                        .selectAs(SysDeviceOrg::getSysDeviceOrgName, SysDevice::getSysDeviceOrgName)
                        .leftJoin(SysDeviceOrg.class, SysDeviceOrg::getSysDeviceOrgId, SysDevice::getSysDeviceOrgCode)
                        .eq(SysDevice::getSysProjectId, request.getSysProjectId())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceName()), SysDevice::getSysDeviceName, request.getSysDeviceName())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceSerialNumber()), SysDevice::getSysDeviceSerialNumber, request.getSysDeviceSerialNumber())
                        .eq(null != request.getSysDeviceStatus(), SysDevice::getSysDeviceStatus, request.getSysDeviceStatus())
                        .eq(null != request.getSysDeviceOnline(), SysDevice::getSysDeviceOnline, request.getSysDeviceOnline())
                        .orderByDesc(SysDevice::getCreateTime)
        );
    }

    @Override
    public IPage<SysDevice> pageFor(DevicePageV1Request request) {
        Page<SysDevice> sysDevicePage = baseMapper.selectPage(new Page<>(request.getPage(), request.getPageSize()),
                MPJWrappers.<SysDevice>lambdaJoin()
                        .selectAll(SysDevice.class)
                        .selectAs(SysDeviceOrg::getSysDeviceOrgName, SysDevice::getSysDeviceOrgName)
                        .leftJoin(SysDeviceOrg.class, SysDeviceOrg::getSysDeviceOrgId, SysDevice::getSysDeviceOrgCode)
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceName()), SysDevice::getSysDeviceName, request.getSysDeviceName())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceSerialNumber()), SysDevice::getSysDeviceSerialNumber, request.getSysDeviceSerialNumber())
                        .eq(null != request.getSysDeviceStatus(), SysDevice::getSysDeviceStatus, request.getSysDeviceStatus())
                        .eq(null != request.getSysDeviceOnline(), SysDevice::getSysDeviceOnline, request.getSysDeviceOnline())
                        .orderByDesc(SysDevice::getCreateTime)
        );

        List<SysDevice> records = sysDevicePage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            Set<String> deviceSerialNumberSet = records.stream().map(SysDevice::getSysDeviceSerialNumber).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            sysDeviceChannelMapper.selectList(Wrappers.<SysDeviceChannel>lambdaQuery().in(SysDeviceChannel::getSysDeviceSerialNumber, deviceSerialNumberSet))
                    .stream().collect(Collectors.groupingBy(SysDeviceChannel::getSysDeviceSerialNumber)).forEach((k, v) -> {
                        for (SysDevice sysDevice : records) {
                            if (k.equals(sysDevice.getSysDeviceSerialNumber())) {
                                sysDevice.setSysDeviceChannelCount(v.size());
                                sysDevice.setChannelList(v);
                            }
                        }
                    });
        }

        return sysDevicePage;
    }

    /**
     * 重新加载
     *
     * @param sysProjectId 项目id
     * @return CameraService
     */
    public static CameraService reload(SysProjectMapper sysProjectMapper, Integer sysProjectId) {
        SysProject sysProject = sysProjectMapper.getOne(sysProjectId);
        if (null == sysProject) {
            return null;
        }
        CameraService cameraService = ServiceProvider.of(CameraService.class).getNewExtension(
                sysProject.getSysProjectDictItemCode()
        );

        if (null == cameraService) {
            return null;
        }

        cameraService.reload();
        return cameraService;
    }

    @Override
    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        CameraService cameraService = reload(request.getSysProjectId());
        if (null == cameraService) {
            return new Page<>();
        }
        return cameraService.cardHistoryPage(request);
    }

    @Override
    public ReturnResult<Boolean> syncOrg(SyncV1Request request) {
        CameraService cameraService = reload(request.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        if (syncOrg(cameraService, request.getSysProjectId())) {
            return ReturnResult.ok();
        }
        return ReturnResult.illegal("同步失败");
    }

    @Override
    public ReturnResult<List<SysDeviceOrg>> listOrg(SyncV1Request request) {
        return ReturnResult.ok(sysDeviceOrgMapper.selectList(Wrappers.<SysDeviceOrg>lambdaQuery()
                .eq(SysDeviceOrg::getSysProjectId, request.getSysProjectId())
        ));
    }

    @Override
    public ReturnResult<List<SysDeviceCameraOnOffline>> timeline(DeviceTimelineV1Request request) {
        SysDevice sysDevice = baseMapper.selectById(request.getSysDeviceCameraId());
        if (null == sysDevice) {
            return ReturnResult.illegal("设备不存在");
        }

        CameraService cameraService = reload(sysDevice.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        return ReturnResult.ok(cameraService.timeline(sysDevice.getSysProjectId(), sysDevice.getSysDeviceSerialNumber(), sysDevice.getSysDeviceResourceType()));
    }

    @Override
    public ReturnResult<Boolean> syncTemplate(SyncV1Request request) {
        CameraService cameraService = reload(request.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        if (cameraService.sync(request.getSysProjectId(), Set.of("camera"), it -> {
            List<DeviceResourceResourcesResult.ListDTO> list = it.getList();
            for (DeviceResourceResourcesResult.ListDTO listDTO : list) {
                SysDevice sysDevice = baseMapper.selectOne(
                        Wrappers.<SysDevice>lambdaQuery()
                                .eq(SysDevice::getSysDeviceSerialNumber, listDTO.getIndexCode()), false
                );
                if (null == sysDevice) {
                    sysDevice = new SysDevice();
                }
                sysDevice.setSysDeviceChannelCount(listDTO.getChannelCount());
                sysDevice.setSysDeviceName(listDTO.getName());
                sysDevice.setSysDeviceOrgCode(listDTO.getRegionIndexCode());
                sysDevice.setSysDeviceStatus(listDTO.getStatus());
                sysDevice.setSysDeviceOnline(listDTO.getOnline());
                sysDevice.setUpdateTime(null == listDTO.getUpdateTime() ? null : DateTime.of(listDTO.getUpdateTime()).toLocalDateTime());
                sysDevice.setCreateTime(null == listDTO.getCreateTime() ? null : DateTime.of(listDTO.getCreateTime()).toLocalDateTime());
                sysDevice.setSysProjectId(request.getSysProjectId());
                sysDevice.setSysDeviceSerialNumber(listDTO.getIndexCode());
                sysDevice.setSysDevicePosition(listDTO.getInstallLocation());
                sysDevice.setSysDeviceResourceType(StringUtils.defaultIfBlank(listDTO.getResourceType(), EMPTY).toUpperCase());
                sysDevice.setSysDeviceVersion(listDTO.getVersion());
                sysDevice.setSysDeviceValidateCode(listDTO.getValidateCode());
                sysDevice.setSysDeviceType(listDTO.getDeviceType());
                sysDevice.setSysDeviceLongitude(Converter.convertIfNecessary(listDTO.getLongitude(), BigDecimal.class));
                sysDevice.setSysDeviceLatitude(Converter.convertIfNecessary(listDTO.getLatitude(), BigDecimal.class));
                sysDevice.setSysDeviceNetAddress(listDTO.getNetAddress());
                if (StringUtils.isNotBlank(sysDevice.getSysDeviceNetAddress()) && null != iptablesService) {
                    ReturnResult<GeoCity> returnResult = iptablesService.transferAddress(sysDevice.getSysDeviceNetAddress());
                    if (returnResult.isOk()) {
                        GeoCity geoCity = returnResult.getData();
                        sysDevice.setSysDeviceNetPhysicalAddress(geoCity.getProvince() + " " + geoCity.getCity());
                    }
                }

                sysDevice.setSysDeviceDescription(listDTO.getDescription());
                int syncChannelCount = syncChannel(cameraService, sysDevice, request.getSysProjectId());
                if (null == sysDevice.getSysDeviceChannelCount()) {
                    sysDevice.setSysDeviceChannelCount(syncChannelCount);
                }
                if (null == sysDevice.getSysDeviceId()) {
                    baseMapper.insert(sysDevice);
                    continue;
                }
                baseMapper.updateById(sysDevice);
            }
        })) {
            syncOrg(cameraService, request.getSysProjectId());
            return ReturnResult.ok();
        }
        return ReturnResult.illegal("同步失败");
    }


    @Override
    public ReturnResult<String> previewUrl(DevicePreviewUrlV1Request request) {
        SysDevice sysDevice = baseMapper.selectById(request.getSysDeviceId());
        if (null == sysDevice) {
            return ReturnResult.illegal("设备不存在");
        }

        CameraService cameraService = reload(sysDevice.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }

        String sysDeviceRtsp = sysDevice.getSysDeviceRtsp();
        if (StringUtils.isNotBlank(sysDeviceRtsp)) {
            return ReturnResult.ok(
                    com.chua.common.support.utils.StringUtils.format(
                            sysDeviceRtsp,
                            JsonObject.create(sysDevice)
                    )
            );
        }
        return ReturnResult.ok(cameraService.previewUrl(sysDevice.getSysProjectId(), sysDevice.getSysDeviceSerialNumber(), sysDevice.getSysDeviceResourceType(), request));
    }

    /**
     * 同步通道
     *
     * @param cameraService cameraService
     * @param sysDevice     sysDevice
     * @param sysProjectId  sysProjectId
     */
    private int syncChannel(CameraService cameraService, SysDevice sysDevice, @NotNull(message = "项目ID不能为空", groups = SelectGroup.class) Integer sysProjectId) {
        AtomicInteger counter = new AtomicInteger(1);
        cameraService.syncChannel(sysProjectId, sysDevice, it -> {

            SysDeviceChannel sysDeviceChannel = sysDeviceChannelMapper.selectOne(Wrappers.<SysDeviceChannel>lambdaQuery()
                            .eq(SysDeviceChannel::getSysDeviceSerialNumber, it.getDeviceSerial())
                            .eq(SysDeviceChannel::getSysDeviceChannelNo, it.getChannelNo())
                    , false);
            if (null == sysDeviceChannel) {
                sysDeviceChannel = new SysDeviceChannel();
            }
            sysDeviceChannel.setSysDeviceChannelName(it.getChannelName());
            sysDeviceChannel.setSysDeviceChannelNo(it.getChannelNo());
            sysDeviceChannel.setSysDeviceChannelStatus(it.getChannelStatus());
            sysDeviceChannel.setSysDeviceChannelType(it.getChannelType());
            sysDeviceChannel.setSysDeviceChannelUse(it.getIsUse());
            sysDeviceChannel.setSysDeviceSerialNumber(it.getDeviceSerial());

            counter.incrementAndGet();
            if (null == sysDeviceChannel.getSysDeviceChannelId()) {
                sysDeviceChannelMapper.insert(sysDeviceChannel);
                return;
            }
            sysDeviceChannelMapper.updateById(sysDeviceChannel);
        });
        return counter.get();
    }
    /**
     * 同步组织
     *
     * @param cameraService cameraService
     * @param sysProjectId  sysProjectId
     * @return boolean
     */
    private boolean syncOrg(CameraService cameraService, Integer sysProjectId) {
        try {
            return cameraService.syncOrg(sysProjectId, it -> {
                List<HikRegionCascade> list = it.getList();
                for (HikRegionCascade hikRegionCascade : list) {
                    SysDeviceOrg sysDeviceOrg = sysDeviceOrgMapper.selectOne(
                            Wrappers.<SysDeviceOrg>lambdaQuery()
                                    .eq(SysDeviceOrg::getSysDeviceOrgId, hikRegionCascade.getIndexCode()), false
                    );
                    boolean isUpdate = true;
                    if (null == sysDeviceOrg) {
                        sysDeviceOrg = new SysDeviceOrg();
                        isUpdate = false;
                    }
                    sysDeviceOrg.setSysDeviceOrgId(hikRegionCascade.getIndexCode());
                    sysDeviceOrg.setSysDeviceOrgPid(hikRegionCascade.getParentIndexCode());
                    sysDeviceOrg.setSysProjectId(sysProjectId);
                    sysDeviceOrg.setSysDeviceOrgName(hikRegionCascade.getName());
                    sysDeviceOrg.setSysDeviceOrgTreePath(hikRegionCascade.getRegionPath());
                    String createTime = hikRegionCascade.getCreateTime();
                    String updateTime = hikRegionCascade.getUpdateTime();
                    sysDeviceOrg.setCreateTime(null == createTime ? LocalDateTime.now() : DateTime.of(createTime).toLocalDateTime());
                    sysDeviceOrg.setUpdateTime(null == updateTime ? LocalDateTime.now() : DateTime.of(updateTime).toLocalDateTime());

                    if (isUpdate) {
                        sysDeviceOrgMapper.updateById(sysDeviceOrg);
                        continue;
                    }
                    sysDeviceOrgMapper.insert(sysDeviceOrg);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ReturnResult<Boolean> deleteFor(SysDevice sysDevice) {
        SysDevice sysDevice1 = baseMapper.selectById(sysDevice.getSysProjectId());
        if (null == sysDevice1) {
            return ReturnResult.illegal("项目不存在");
        }
        try {
            return ReturnResult.of(baseMapper.deleteById(sysDevice) > 0);
        } finally {
            reload(sysDevice.getSysProjectId());
        }
    }

    @Override
    public ReturnResult<List<OnOffline>> check(CheckDeviceOnlineV1Request request) {
        List<SysDevice> sysDevices = baseMapper.selectList(
                Wrappers.<SysDevice>lambdaQuery()
                        .in(SysDevice::getSysDeviceId, request.getSysDeviceCameraIds())
        );
        if (CollectionUtils.isEmpty(sysDevices)) {
            return ReturnResult.illegal("设备不存在");
        }

        Integer sysProjectId = sysDevices.getFirst().getSysProjectId();
        CameraService cameraService = reload(sysProjectId);
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        List<OnOffline> onlineResults = cameraService.checkOnline(sysProjectId, sysDevices.stream().map(SysDevice::getSysDeviceSerialNumber).toArray(String[]::new));
        Map<String, List<OnOffline>> collect = onlineResults.stream().collect(Collectors.groupingBy(OnOffline::getSysCameraTemplateSerialNumber));
        for (SysDevice sysDevice : sysDevices) {
            List<OnOffline> onOfflines = collect.get(sysDevice.getSysDeviceSerialNumber());
            if (CollectionUtils.isEmpty(onOfflines)) {
                continue;
            }
            OnOffline onOffline = onOfflines.getFirst();
            sysDevice.setSysDeviceOnline(onOffline.getOnline());
            sysDevice.setUpdateTime(LocalDateTime.now());
        }

        updateBatchById(sysDevices);
        return ReturnResult.ok(onlineResults);
    }

    /**
     * 重新加载
     *
     * @param sysProjectId 项目id
     * @return CameraService
     */
    private CameraService reload(Integer sysProjectId) {
        return reload(sysProjectMapper, sysProjectId);
    }

    @Override
    public ReturnResult<Boolean> updateFor(SysDevice sysDevice) {
        SysDevice sysDevice1 = baseMapper.selectById(sysDevice.getSysProjectId());
        if (null == sysDevice1) {
            return ReturnResult.illegal("项目不存在");
        }
        if (StringUtils.isNotBlank(sysDevice.getSysDeviceNetAddress()) && null != iptablesService) {
            ReturnResult<GeoCity> returnResult = iptablesService.transferAddress(sysDevice.getSysDeviceNetAddress());
            if (returnResult.isOk()) {
                GeoCity geoCity = returnResult.getData();
                sysDevice.setSysDeviceNetPhysicalAddress(geoCity.getProvince() + " " + geoCity.getCity());
            }
        }
        try {
            return ReturnResult.of(baseMapper.updateById(sysDevice) > 0);
        } finally {
            reload(sysDevice.getSysProjectId());
        }
    }

    @Override
    public ReturnResult<SysDevice> saveFor(SysDevice sysDevice) {
        if (StringUtils.isNotBlank(sysDevice.getSysDeviceNetAddress()) && null != iptablesService) {
            ReturnResult<GeoCity> returnResult = iptablesService.transferAddress(sysDevice.getSysDeviceNetAddress());
            if (returnResult.isOk()) {
                GeoCity geoCity = returnResult.getData();
                sysDevice.setSysDeviceNetPhysicalAddress(geoCity.getProvince() + " " + geoCity.getCity());
            }
        }
        baseMapper.insert(sysDevice);
        try {
            return ReturnResult.of(sysDevice);
        } finally {
            reload(sysDevice.getSysProjectId());
        }
    }


}
