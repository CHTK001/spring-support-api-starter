package com.chua.starter.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.cloud.support.security.SecurityClient;
import com.chua.cloud.support.security.entity.AccessRecord;
import com.chua.cloud.support.security.entity.Channel;

import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.JsonObject;
import com.chua.starter.device.pojo.DeviceOnlineStatus;
import com.chua.starter.device.pojo.DeviceOnlineTimelinePoint;
import com.chua.starter.device.pojo.request.CardDevicePageV1Request;
import com.chua.starter.device.pojo.request.CheckDeviceOnlineV1Request;
import com.chua.starter.device.pojo.request.DevicePageV1Request;
import com.chua.starter.device.pojo.request.DevicePreviewUrlV1Request;
import com.chua.starter.device.pojo.request.DeviceTimelineV1Request;
import com.chua.starter.device.pojo.request.SyncV1Request;
import com.chua.starter.device.pojo.request.TemplatePageV1Request;
import com.chua.starter.device.mapper.SysDeviceCameraRecordMapper;
import com.chua.starter.device.mapper.SysDeviceCardEventMapper;
import com.chua.starter.device.mapper.SysDeviceChannelMapper;
import com.chua.starter.device.mapper.SysDeviceMapper;
import com.chua.starter.device.mapper.SysDeviceMessageEventMapper;
import com.chua.starter.device.mapper.SysDeviceOrgMapper;
import com.chua.starter.device.support.DeviceSecurityClientFactory;
import com.chua.starter.entity.device.SysDevice;
import com.chua.starter.entity.device.SysDeviceCameraRecord;
import com.chua.starter.entity.device.SysDeviceCardEvent;
import com.chua.starter.entity.device.SysDeviceChannel;
import com.chua.starter.entity.device.SysDeviceMessageEvent;
import com.chua.starter.entity.device.SysDeviceOrg;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 独立设备模块业务服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService extends ServiceImpl<SysDeviceMapper, SysDevice> {

    private static final int DEVICE_SYNC_PAGE_SIZE = 100;
    private static final int CHANNEL_SYNC_PAGE_SIZE = 100;
    private static final int EVENT_SYNC_PAGE_SIZE = 200;
    private static final int MAX_SYNC_PAGES = 50;

    private final SysDeviceCameraRecordMapper sysDeviceCameraRecordMapper;
    private final SysDeviceOrgMapper sysDeviceOrgMapper;
    private final SysDeviceChannelMapper sysDeviceChannelMapper;
    private final SysDeviceCardEventMapper sysDeviceCardEventMapper;
    private final SysDeviceMessageEventMapper sysDeviceMessageEventMapper;
    private final DeviceSecurityClientFactory deviceSecurityClientFactory;

    public IPage<SysDeviceCameraRecord> pageForLog(TemplatePageV1Request request) {
        return sysDeviceCameraRecordMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()),
                Wrappers.<SysDeviceCameraRecord>lambdaQuery().orderByDesc(SysDeviceCameraRecord::getCreateTime)
        );
    }

    public IPage<SysDevice> pageFor(TemplatePageV1Request request) {
        List<Integer> projectIds = parseProjectIds(request.getSysProjectId());
        return baseMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()),
                MPJWrappers.<SysDevice>lambdaJoin()
                        .selectAll(SysDevice.class)
                        .selectAs(SysDeviceOrg::getSysDeviceOrgName, SysDevice::getSysDeviceOrgName)
                        .leftJoin(SysDeviceOrg.class, SysDeviceOrg::getSysDeviceOrgId, SysDevice::getSysDeviceOrgCode)
                        .in(!projectIds.isEmpty(), SysDevice::getSysProjectId, projectIds)
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceName()), SysDevice::getSysDeviceName, request.getSysDeviceName())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceSerialNumber()), SysDevice::getSysDeviceSerialNumber, request.getSysDeviceSerialNumber())
                        .eq(request.getSysDeviceStatus() != null, SysDevice::getSysDeviceStatus, request.getSysDeviceStatus())
                        .eq(request.getSysDeviceOnline() != null, SysDevice::getSysDeviceOnline, request.getSysDeviceOnline())
                        .orderByDesc(SysDevice::getCreateTime)
        );
    }

    public IPage<SysDevice> pageFor(DevicePageV1Request request) {
        List<Integer> projectIds = parseProjectIds(request.getSysProjectId());
        Page<SysDevice> page = baseMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()),
                MPJWrappers.<SysDevice>lambdaJoin()
                        .selectAll(SysDevice.class)
                        .selectAs(SysDeviceOrg::getSysDeviceOrgName, SysDevice::getSysDeviceOrgName)
                        .leftJoin(SysDeviceOrg.class, SysDeviceOrg::getSysDeviceOrgId, SysDevice::getSysDeviceOrgCode)
                        .in(!projectIds.isEmpty(), SysDevice::getSysProjectId, projectIds)
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceName()), SysDevice::getSysDeviceName, request.getSysDeviceName())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceSerialNumber()), SysDevice::getSysDeviceSerialNumber, request.getSysDeviceSerialNumber())
                        .eq(request.getSysDeviceStatus() != null, SysDevice::getSysDeviceStatus, request.getSysDeviceStatus())
                        .eq(request.getSysDeviceOnline() != null, SysDevice::getSysDeviceOnline, request.getSysDeviceOnline())
                        .orderByDesc(SysDevice::getCreateTime)
        );
        attachChannelInfo(page.getRecords());
        return page;
    }

    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        List<Integer> projectIds = parseProjectIds(request.getSysProjectId());
        return sysDeviceCardEventMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()),
                MPJWrappers.<SysDeviceCardEvent>lambdaJoin()
                        .selectAll(SysDeviceCardEvent.class)
                        .leftJoin(SysDevice.class, SysDevice::getSysDeviceSerialNumber, SysDeviceCardEvent::getSysDeviceSerialNumber)
                        .in(!projectIds.isEmpty(), SysDevice::getSysProjectId, projectIds)
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceName()), SysDevice::getSysDeviceName, request.getSysDeviceName())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceSerialNumber()), SysDeviceCardEvent::getSysDeviceSerialNumber, request.getSysDeviceSerialNumber())
                        .ge(request.getStartTime() != null, SysDeviceCardEvent::getSysDeviceCardEventTime, request.getStartTime())
                        .le(request.getEndTime() != null, SysDeviceCardEvent::getSysDeviceCardEventTime, request.getEndTime())
                        .orderByDesc(SysDeviceCardEvent::getSysDeviceCardEventTime)
        );
    }

    public ReturnResult<String> previewUrl(DevicePreviewUrlV1Request request) {
        SysDevice sysDevice = baseMapper.selectById(request.getSysDeviceId());
        if (sysDevice == null) {
            return ReturnResult.illegal("设备不存在");
        }
        if (StringUtils.isNotBlank(sysDevice.getSysDeviceRtsp())) {
            return ReturnResult.ok(com.chua.common.support.utils.StringUtils.format(
                    sysDevice.getSysDeviceRtsp(),
                    JsonObject.create(sysDevice)
            ));
        }

        try (SecurityClient client = deviceSecurityClientFactory.create(sysDevice.getSysProjectId())) {
            Integer channelNo = parseChannelNo(request.getChannelNo());
            Integer streamType = request.getSubtype() == null ? 0 : request.getSubtype();
            String protocol = StringUtils.defaultIfBlank(request.getProtocol(), "hls");
            boolean playback = request.getStartTime() != null && request.getEndTime() != null;
            String previewUrl = playback
                    ? client.device().getPlaybackStreamUrl(
                            sysDevice.getSysDeviceSerialNumber(),
                            channelNo,
                            toEpochMilli(request.getStartTime()),
                            toEpochMilli(request.getEndTime()),
                            protocol
                    )
                    : client.device().getLiveStreamUrl(
                            sysDevice.getSysDeviceSerialNumber(),
                            channelNo,
                            streamType,
                            protocol
                    );
            if (StringUtils.isBlank(previewUrl)) {
                return ReturnResult.illegal("未获取到预览地址");
            }
            return ReturnResult.ok(previewUrl);
        } catch (Exception e) {
            log.warn("获取设备预览地址失败, sysDeviceId={}", request.getSysDeviceId(), e);
            return ReturnResult.illegal(StringUtils.defaultIfBlank(e.getMessage(), "获取预览地址失败"));
        }
    }

    public ReturnResult<List<SysDeviceOrg>> listOrg(SyncV1Request request) {
        return ReturnResult.ok(sysDeviceOrgMapper.selectList(
                Wrappers.<SysDeviceOrg>lambdaQuery()
                        .eq(SysDeviceOrg::getSysProjectId, request.getSysProjectId())
                        .orderByAsc(SysDeviceOrg::getSysDeviceOrgName)
        ));
    }

    public ReturnResult<List<DeviceOnlineTimelinePoint>> timeline(DeviceTimelineV1Request request) {
        SysDevice sysDevice = baseMapper.selectById(request.getSysDeviceCameraId());
        if (sysDevice == null) {
            return ReturnResult.illegal("设备不存在");
        }

        Integer online = sysDevice.getSysDeviceOnline();
        try (SecurityClient client = deviceSecurityClientFactory.create(sysDevice.getSysProjectId())) {
            online = normalizeStatus(client.device().queryDeviceStatus(sysDevice.getSysDeviceSerialNumber()));
            sysDevice.setSysDeviceOnline(online);
            sysDevice.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(sysDevice);
        } catch (Exception e) {
            log.debug("查询设备时间线时实时检测失败, sysDeviceId={}", sysDevice.getSysDeviceId(), e);
        }

        DeviceOnlineTimelinePoint timeline = new DeviceOnlineTimelinePoint();
        timeline.setCollectTime(sysDevice.getUpdateTime() == null ? LocalDateTime.now() : sysDevice.getUpdateTime());
        timeline.setOnline(online);
        return ReturnResult.ok(List.of(timeline));
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> syncTemplate(SyncV1Request request) {
        try (SecurityClient client = deviceSecurityClientFactory.create(request.getSysProjectId())) {
            int syncedCount = 0;
            for (com.chua.cloud.support.security.entity.Device remoteDevice : queryAllDevices(client)) {
                SysDevice sysDevice = saveOrUpdateDevice(request.getSysProjectId(), remoteDevice);
                syncDeviceChannels(client, sysDevice, remoteDevice);
                syncedCount++;
            }
            syncOrg(request.getSysProjectId());
            log.info("同步设备模板完成, sysProjectId={}, count={}", request.getSysProjectId(), syncedCount);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.warn("同步设备模板失败, sysProjectId={}", request.getSysProjectId(), e);
            return ReturnResult.illegal(StringUtils.defaultIfBlank(e.getMessage(), "同步设备失败"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> syncOrg(SyncV1Request request) {
        return syncOrg(request.getSysProjectId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteFor(SysDevice sysDevice) {
        if (sysDevice == null || sysDevice.getSysDeviceId() == null) {
            return ReturnResult.illegal("设备ID不能为空");
        }
        SysDevice existed = baseMapper.selectById(sysDevice.getSysDeviceId());
        if (existed == null) {
            return ReturnResult.illegal("设备不存在");
        }
        return ReturnResult.of(baseMapper.deleteById(existed.getSysDeviceId()) > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateFor(SysDevice sysDevice) {
        if (sysDevice == null || sysDevice.getSysDeviceId() == null) {
            return ReturnResult.illegal("设备ID不能为空");
        }
        SysDevice existed = baseMapper.selectById(sysDevice.getSysDeviceId());
        if (existed == null) {
            return ReturnResult.illegal("设备不存在");
        }
        return ReturnResult.of(baseMapper.updateById(sysDevice) > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysDevice> saveFor(SysDevice sysDevice) {
        if (sysDevice == null || sysDevice.getSysProjectId() == null) {
            return ReturnResult.illegal("项目ID不能为空");
        }
        deviceSecurityClientFactory.getDeviceMerchant(sysDevice.getSysProjectId());
        if (sysDevice.getSysDeviceStatus() == null) {
            sysDevice.setSysDeviceStatus(0);
        }
        if (sysDevice.getSysDeviceOnline() == null) {
            sysDevice.setSysDeviceOnline(0);
        }
        baseMapper.insert(sysDevice);
        return ReturnResult.ok(sysDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<List<DeviceOnlineStatus>> check(CheckDeviceOnlineV1Request request) {
        List<Integer> deviceIds = parseDeviceIds(request.getSysDeviceCameraIds());
        if (CollectionUtils.isEmpty(deviceIds)) {
            return ReturnResult.illegal("设备ID不能为空");
        }
        List<SysDevice> sysDevices = baseMapper.selectList(
                Wrappers.<SysDevice>lambdaQuery().in(SysDevice::getSysDeviceId, deviceIds)
        );
        if (CollectionUtils.isEmpty(sysDevices)) {
            return ReturnResult.illegal("设备不存在");
        }

        Map<Integer, List<SysDevice>> grouped = sysDevices.stream()
                .filter(it -> it.getSysProjectId() != null)
                .collect(Collectors.groupingBy(SysDevice::getSysProjectId));

        List<DeviceOnlineStatus> result = new ArrayList<>();
        for (Map.Entry<Integer, List<SysDevice>> entry : grouped.entrySet()) {
            try (SecurityClient client = deviceSecurityClientFactory.create(entry.getKey())) {
                for (SysDevice sysDevice : entry.getValue()) {
                    Integer online = normalizeStatus(client.device().queryDeviceStatus(sysDevice.getSysDeviceSerialNumber()));
                    sysDevice.setSysDeviceOnline(online);
                    sysDevice.setUpdateTime(LocalDateTime.now());

                    DeviceOnlineStatus onOffline = new DeviceOnlineStatus();
                    onOffline.setSysCameraTemplateSerialNumber(sysDevice.getSysDeviceSerialNumber());
                    onOffline.setOnline(online);
                    result.add(onOffline);
                }
            } catch (Exception e) {
                log.warn("检测设备在线状态失败, sysProjectId={}", entry.getKey(), e);
            }
        }
        updateBatchById(sysDevices);
        return ReturnResult.ok(result);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> syncEventMessage(CardDevicePageV1Request request) {
        List<Integer> projectIds = parseProjectIds(request.getSysProjectId());
        List<SysDevice> devices = baseMapper.selectList(
                Wrappers.<SysDevice>lambdaQuery()
                        .in(!projectIds.isEmpty(), SysDevice::getSysProjectId, projectIds)
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceName()), SysDevice::getSysDeviceName, request.getSysDeviceName())
                        .likeRight(StringUtils.isNotBlank(request.getSysDeviceSerialNumber()), SysDevice::getSysDeviceSerialNumber, request.getSysDeviceSerialNumber())
        );
        if (CollectionUtils.isEmpty(devices)) {
            return ReturnResult.ok(true);
        }

        Map<String, SysDevice> deviceMap = devices.stream()
                .collect(Collectors.toMap(SysDevice::getSysDeviceSerialNumber, it -> it, (a, b) -> a, LinkedHashMap::new));
        Map<Integer, List<SysDevice>> grouped = devices.stream().collect(Collectors.groupingBy(SysDevice::getSysProjectId));
        long startTime = request.getStartTime() == null
                ? Instant.now().minusSeconds(24 * 60 * 60).toEpochMilli()
                : toEpochMilli(request.getStartTime());
        long endTime = request.getEndTime() == null ? System.currentTimeMillis() : toEpochMilli(request.getEndTime());

        for (Map.Entry<Integer, List<SysDevice>> entry : grouped.entrySet()) {
            try (SecurityClient client = deviceSecurityClientFactory.create(entry.getKey())) {
                for (SysDevice sysDevice : entry.getValue()) {
                    List<AccessRecord> records = queryAllAccessRecords(client, sysDevice.getSysDeviceSerialNumber(), startTime, endTime);
                    saveAccessRecords(deviceMap.get(sysDevice.getSysDeviceSerialNumber()), records);
                }
            } catch (Exception e) {
                log.warn("同步设备事件失败, sysProjectId={}", entry.getKey(), e);
            }
        }
        return ReturnResult.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> syncChannels(String sysDeviceSerialNumber, List<SysDeviceChannel> channels) {
        if (StringUtils.isBlank(sysDeviceSerialNumber)) {
            return ReturnResult.illegal("设备编号不能为空");
        }
        sysDeviceChannelMapper.delete(
                Wrappers.<SysDeviceChannel>lambdaQuery().eq(SysDeviceChannel::getSysDeviceSerialNumber, sysDeviceSerialNumber)
        );
        if (CollectionUtils.isNotEmpty(channels)) {
            for (SysDeviceChannel channel : channels) {
                channel.setSysDeviceSerialNumber(sysDeviceSerialNumber);
                sysDeviceChannelMapper.insert(channel);
            }
        }
        return ReturnResult.ok(true);
    }

    private ReturnResult<Boolean> syncOrg(Integer sysProjectId) {
        List<SysDevice> devices = baseMapper.selectList(
                Wrappers.<SysDevice>lambdaQuery().eq(SysDevice::getSysProjectId, sysProjectId)
        );
        Map<String, SysDeviceOrg> orgMap = new LinkedHashMap<>();
        for (SysDevice device : devices) {
            if (StringUtils.isBlank(device.getSysDeviceOrgCode())) {
                continue;
            }
            SysDeviceOrg sysDeviceOrg = orgMap.computeIfAbsent(device.getSysDeviceOrgCode(), key -> {
                SysDeviceOrg newOrg = new SysDeviceOrg();
                newOrg.setSysDeviceOrgId(key);
                newOrg.setSysProjectId(sysProjectId);
                newOrg.setSysDeviceOrgPid("0");
                newOrg.setSysDeviceOrgTreePath(key);
                return newOrg;
            });
            if (StringUtils.isBlank(sysDeviceOrg.getSysDeviceOrgName())) {
                sysDeviceOrg.setSysDeviceOrgName(StringUtils.defaultIfBlank(device.getSysDeviceOrgName(), device.getSysDeviceOrgCode()));
            }
        }

        Set<String> currentIds = orgMap.keySet();
        List<SysDeviceOrg> existingList = sysDeviceOrgMapper.selectList(
                Wrappers.<SysDeviceOrg>lambdaQuery().eq(SysDeviceOrg::getSysProjectId, sysProjectId)
        );
        if (CollectionUtils.isNotEmpty(existingList) && CollectionUtils.isNotEmpty(currentIds)) {
            sysDeviceOrgMapper.delete(
                    Wrappers.<SysDeviceOrg>lambdaQuery()
                            .eq(SysDeviceOrg::getSysProjectId, sysProjectId)
                            .notIn(SysDeviceOrg::getSysDeviceOrgId, currentIds)
            );
        }
        for (SysDeviceOrg sysDeviceOrg : orgMap.values()) {
            SysDeviceOrg existed = sysDeviceOrgMapper.selectById(sysDeviceOrg.getSysDeviceOrgId());
            if (existed == null) {
                sysDeviceOrgMapper.insert(sysDeviceOrg);
                continue;
            }
            sysDeviceOrg.setCreateTime(existed.getCreateTime());
            sysDeviceOrgMapper.updateById(sysDeviceOrg);
        }
        return ReturnResult.ok(true);
    }

    private void attachChannelInfo(List<SysDevice> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        Set<String> serialNumbers = records.stream()
                .map(SysDevice::getSysDeviceSerialNumber)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (CollectionUtils.isEmpty(serialNumbers)) {
            return;
        }

        Map<String, List<SysDeviceChannel>> channelMap = sysDeviceChannelMapper.selectList(
                        Wrappers.<SysDeviceChannel>lambdaQuery().in(SysDeviceChannel::getSysDeviceSerialNumber, serialNumbers)
                ).stream()
                .collect(Collectors.groupingBy(SysDeviceChannel::getSysDeviceSerialNumber));
        for (SysDevice sysDevice : records) {
            List<SysDeviceChannel> channels = channelMap.get(sysDevice.getSysDeviceSerialNumber());
            if (CollectionUtils.isEmpty(channels)) {
                sysDevice.setSysDeviceChannelCount(0);
                sysDevice.setChannelList(List.of());
                continue;
            }
            sysDevice.setSysDeviceChannelCount(channels.size());
            sysDevice.setChannelList(channels);
        }
    }

    private SysDevice saveOrUpdateDevice(Integer sysProjectId, com.chua.cloud.support.security.entity.Device remoteDevice) {
        SysDevice sysDevice = baseMapper.selectOne(
                Wrappers.<SysDevice>lambdaQuery()
                        .eq(SysDevice::getSysProjectId, sysProjectId)
                        .eq(SysDevice::getSysDeviceSerialNumber, remoteDevice.getDeviceSerial()),
                false
        );
        if (sysDevice == null) {
            sysDevice = new SysDevice();
        }
        sysDevice.setSysProjectId(sysProjectId);
        sysDevice.setSysDeviceName(firstNonBlank(remoteDevice.getDeviceName(), remoteDevice.getDeviceSerial()));
        sysDevice.setSysDeviceSerialNumber(remoteDevice.getDeviceSerial());
        sysDevice.setSysDeviceOrgCode(firstNonBlank(remoteDevice.getGroupId(), remoteDevice.getGroupNo()));
        sysDevice.setSysDeviceOrgName(firstNonBlank(remoteDevice.getGroupNo(), remoteDevice.getGroupId()));
        sysDevice.setSysDeviceOnline(normalizeStatus(remoteDevice.getStatus()));
        if (sysDevice.getSysDeviceStatus() == null) {
            sysDevice.setSysDeviceStatus(0);
        }
        sysDevice.setSysDeviceVersion(remoteDevice.getFirmwareVersion());
        sysDevice.setSysDeviceValidateCode(remoteDevice.getValidateCode());
        sysDevice.setSysDeviceType(firstNonBlank(remoteDevice.getDeviceType(), remoteDevice.getDeviceModel()));
        sysDevice.setSysDeviceNetAddress(remoteDevice.getDeviceIp());
        sysDevice.setUpdateTime(toLocalDateTime(remoteDevice.getLastOnlineTime()));
        if (sysDevice.getCreateTime() == null) {
            sysDevice.setCreateTime(toLocalDateTime(remoteDevice.getCreateTime()));
        }
        if (sysDevice.getSysDeviceId() == null) {
            baseMapper.insert(sysDevice);
            return sysDevice;
        }
        baseMapper.updateById(sysDevice);
        return sysDevice;
    }

    private void syncDeviceChannels(SecurityClient client, SysDevice sysDevice, com.chua.cloud.support.security.entity.Device remoteDevice) {
        List<Channel> channels = queryAllChannels(client, remoteDevice);
        if (CollectionUtils.isEmpty(channels)) {
            List<SysDeviceChannel> existingChannels = sysDeviceChannelMapper.selectList(
                    Wrappers.<SysDeviceChannel>lambdaQuery()
                            .eq(SysDeviceChannel::getSysDeviceSerialNumber, sysDevice.getSysDeviceSerialNumber())
            );
            if (CollectionUtils.isEmpty(existingChannels)) {
                SysDeviceChannel syntheticChannel = new SysDeviceChannel();
                syntheticChannel.setSysDeviceSerialNumber(sysDevice.getSysDeviceSerialNumber());
                syntheticChannel.setSysDeviceChannelNo(1);
                syntheticChannel.setSysDeviceChannelName(firstNonBlank(sysDevice.getSysDeviceName(), sysDevice.getSysDeviceSerialNumber()));
                syntheticChannel.setSysDeviceChannelStatus(sysDevice.getSysDeviceOnline());
                syntheticChannel.setSysDeviceChannelType(sysDevice.getSysDeviceResourceType());
                syntheticChannel.setSysDeviceChannelUse(1);
                sysDeviceChannelMapper.insert(syntheticChannel);
                sysDevice.setSysDeviceChannelCount(1);
                baseMapper.updateById(sysDevice);
                return;
            }
            sysDevice.setSysDeviceChannelCount(existingChannels.size());
            baseMapper.updateById(sysDevice);
            return;
        }

        Set<Integer> incomingNos = new LinkedHashSet<>();
        for (Channel channel : channels) {
            Integer channelNo = channel.getChannelNo() == null ? 1 : channel.getChannelNo();
            incomingNos.add(channelNo);
            SysDeviceChannel sysDeviceChannel = sysDeviceChannelMapper.selectOne(
                    Wrappers.<SysDeviceChannel>lambdaQuery()
                            .eq(SysDeviceChannel::getSysDeviceSerialNumber, sysDevice.getSysDeviceSerialNumber())
                            .eq(SysDeviceChannel::getSysDeviceChannelNo, channelNo),
                    false
            );
            if (sysDeviceChannel == null) {
                sysDeviceChannel = new SysDeviceChannel();
            }
            sysDeviceChannel.setSysDeviceSerialNumber(sysDevice.getSysDeviceSerialNumber());
            sysDeviceChannel.setSysDeviceChannelNo(channelNo);
            sysDeviceChannel.setSysDeviceChannelName(firstNonBlank(channel.getChannelName(), sysDevice.getSysDeviceName(), "通道" + channelNo));
            sysDeviceChannel.setSysDeviceChannelStatus(normalizeStatus(channel.getChannelStatus()));
            sysDeviceChannel.setSysDeviceChannelType(channel.getChannelType());
            sysDeviceChannel.setSysDeviceChannelUse(1);
            if (sysDeviceChannel.getSysDeviceChannelId() == null) {
                sysDeviceChannelMapper.insert(sysDeviceChannel);
                continue;
            }
            sysDeviceChannelMapper.updateById(sysDeviceChannel);
        }

        if (CollectionUtils.isNotEmpty(incomingNos)) {
            sysDeviceChannelMapper.delete(
                    Wrappers.<SysDeviceChannel>lambdaQuery()
                            .eq(SysDeviceChannel::getSysDeviceSerialNumber, sysDevice.getSysDeviceSerialNumber())
                            .notIn(SysDeviceChannel::getSysDeviceChannelNo, incomingNos)
            );
        }
        sysDevice.setSysDeviceChannelCount(incomingNos.size());
        baseMapper.updateById(sysDevice);
    }

    private List<com.chua.cloud.support.security.entity.Device> queryAllDevices(SecurityClient client) {
        List<com.chua.cloud.support.security.entity.Device> result = new ArrayList<>();
        for (int pageNo = 1; pageNo <= MAX_SYNC_PAGES; pageNo++) {
            List<com.chua.cloud.support.security.entity.Device> pageList = client.device().queryDevices(pageNo, DEVICE_SYNC_PAGE_SIZE);
            if (CollectionUtils.isEmpty(pageList)) {
                break;
            }
            result.addAll(pageList);
            if (pageList.size() < DEVICE_SYNC_PAGE_SIZE) {
                break;
            }
        }
        return result;
    }

    private List<Channel> queryAllChannels(SecurityClient client, com.chua.cloud.support.security.entity.Device remoteDevice) {
        try {
            List<Channel> result = new ArrayList<>();
            String deviceId = firstNonBlank(remoteDevice.getDeviceId(), remoteDevice.getDeviceSerial());
            for (int pageNo = 1; pageNo <= MAX_SYNC_PAGES; pageNo++) {
                List<Channel> pageList = client.channel().queryChannels(deviceId, pageNo, CHANNEL_SYNC_PAGE_SIZE);
                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                result.addAll(pageList);
                if (pageList.size() < CHANNEL_SYNC_PAGE_SIZE) {
                    break;
                }
            }
            return result;
        } catch (UnsupportedOperationException e) {
            log.debug("当前厂家未实现通道查询, deviceSerial={}", remoteDevice.getDeviceSerial(), e);
            return List.of();
        } catch (Exception e) {
            log.warn("查询设备通道失败, deviceSerial={}", remoteDevice.getDeviceSerial(), e);
            return List.of();
        }
    }

    private List<AccessRecord> queryAllAccessRecords(SecurityClient client, String deviceSerial, long startTime, long endTime) {
        try {
            List<AccessRecord> result = new ArrayList<>();
            for (int pageNo = 1; pageNo <= MAX_SYNC_PAGES; pageNo++) {
                List<AccessRecord> pageList = client.accessControl()
                        .queryRecordsByDevice(deviceSerial, startTime, endTime, pageNo, EVENT_SYNC_PAGE_SIZE);
                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                result.addAll(pageList);
                if (pageList.size() < EVENT_SYNC_PAGE_SIZE) {
                    break;
                }
            }
            return result;
        } catch (UnsupportedOperationException e) {
            log.debug("当前厂家未实现按设备查询门禁记录, deviceSerial={}", deviceSerial, e);
            return List.of();
        } catch (Exception e) {
            log.warn("查询门禁记录失败, deviceSerial={}", deviceSerial, e);
            return List.of();
        }
    }

    private void saveAccessRecords(SysDevice sysDevice, List<AccessRecord> records) {
        if (sysDevice == null || CollectionUtils.isEmpty(records)) {
            return;
        }
        for (AccessRecord record : records) {
            String recordId = firstNonBlank(record.getRecordId(), record.getDeviceSerial() + ":" + record.getEventTime());

            SysDeviceCardEvent cardEvent = sysDeviceCardEventMapper.selectOne(
                    Wrappers.<SysDeviceCardEvent>lambdaQuery()
                            .eq(SysDeviceCardEvent::getSysDeviceCardEventMessageId, recordId),
                    false
            );
            if (cardEvent == null) {
                cardEvent = new SysDeviceCardEvent();
            }
            cardEvent.setSysDeviceCardEventMessageId(recordId);
            cardEvent.setSysDeviceSerialNumber(sysDevice.getSysDeviceSerialNumber());
            cardEvent.setSysDeviceCardEventTime(toLocalDateTime(record.getEventTime()));
            cardEvent.setSysDeviceCardEventPersonInvolved(record.getPersonId());
            cardEvent.setSysDeviceCardEventPersonInvolvedName(record.getPersonName());
            cardEvent.setSysDeviceCardEventMajorName(record.getEventType() == null ? null : record.getEventType().getDesc());
            cardEvent.setSysDeviceCardEventMinorName(record.getDirection() == null ? null : record.getDirection().getDesc());
            if (cardEvent.getSysDeviceCardEventId() == null) {
                sysDeviceCardEventMapper.insert(cardEvent);
            } else {
                sysDeviceCardEventMapper.updateById(cardEvent);
            }

            SysDeviceMessageEvent messageEvent = sysDeviceMessageEventMapper.selectOne(
                    Wrappers.<SysDeviceMessageEvent>lambdaQuery()
                            .eq(SysDeviceMessageEvent::getSysDeviceMessageEventEventId, recordId),
                    false
            );
            if (messageEvent == null) {
                messageEvent = new SysDeviceMessageEvent();
            }
            messageEvent.setSysDeviceMessageEventType("ACCESS_RECORD");
            messageEvent.setSysDeviceMessageEventEventId(recordId);
            messageEvent.setSysDeviceMessageEventDataTime(toLocalDateTime(record.getEventTime()));
            messageEvent.setSysDeviceMessageEventEmployeeNo(record.getPersonId());
            messageEvent.setSysDeviceMessageEventEmployeeName(record.getPersonName());
            messageEvent.setSysDeviceMessageEventCardNo(record.getCardNo());
            messageEvent.setSysDeviceSerialNumber(sysDevice.getSysDeviceSerialNumber());
            messageEvent.setSysDeviceName(sysDevice.getSysDeviceName());
            messageEvent.setSysDeviceMessageEventChannelNo(record.getDoorNo() == null ? null : String.valueOf(record.getDoorNo()));
            messageEvent.setSysDeviceMessageEventLocation(record.getDoorName());
            messageEvent.setSysDeviceMessageEventChannelName(record.getDoorName());
            messageEvent.setSysDeviceMessageEventDoorNo(record.getDoorNo() == null ? null : String.valueOf(record.getDoorNo()));
            messageEvent.setSysDeviceMessageEventEventType(record.getEventType() == null ? null : record.getEventType().name());
            messageEvent.setSysDeviceMessageEventEventTypeName(record.getEventType() == null ? null : record.getEventType().getDesc());
            messageEvent.setSysDeviceMessageEventEventCode(record.getEventType() == null ? null : String.valueOf(record.getEventType().getCode()));
            messageEvent.setSysDeviceMessageEventEventDescription(record.getEventType() == null ? null : record.getEventType().getDesc());
            messageEvent.setSysDeviceMessageEventEventPictureUrl(record.getPictureUrl());
            if (messageEvent.getSysDeviceMessageEventId() == null) {
                sysDeviceMessageEventMapper.insert(messageEvent);
            } else {
                sysDeviceMessageEventMapper.updateById(messageEvent);
            }
        }
    }

    private List<Integer> parseProjectIds(String sysProjectId) {
        if (StringUtils.isBlank(sysProjectId)) {
            return List.of();
        }
        return java.util.Arrays.stream(sysProjectId.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Integer> parseDeviceIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return List.of();
        }
        return java.util.Arrays.stream(ids)
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Integer parseChannelNo(String value) {
        if (StringUtils.isBlank(value)) {
            return 1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private Integer normalizeStatus(Integer value) {
        if (value == null) {
            return 0;
        }
        return value > 0 ? 1 : 0;
    }

    private LocalDateTime toLocalDateTime(Long epochMilli) {
        if (epochMilli == null || epochMilli <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    private Long toEpochMilli(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
