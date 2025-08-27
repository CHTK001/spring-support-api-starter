package com.chua.starter.project.service.support.hikvision;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.project.service.support.CameraService;
import com.chua.starter.project.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.project.service.support.hikvision.result.DeviceResourceResourcesResult;
import com.chua.starter.project.service.support.hikvision.result.HikDeviceChannel;
import com.chua.starter.project.service.support.hikvision.result.HikRegion;
import com.chua.starter.project.service.support.hikvision.result.OnOffline;
import com.chua.starter.system.entity.SysDevice;
import com.chua.starter.system.entity.SysDeviceCardEvent;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author CH
 */
@Spi({
        "HAI_KANG_AN_FANG",
        "HAI_KANG_YUN_YAO",
        "HAI_KANG_YUN_MOU",
})
public class Hikvision implements CameraService {

    private final Map<Integer, HikvisionClient> hikvisionClientMap = new ConcurrentHashMap<>();
    private final Map<Integer, SysProject> hikConfigurations = new ConcurrentHashMap<>();

    @AutoInject
    private SysProjectService sysProjectService;

    @Override
    public void reload() {
        IPage<SysProject> sysProjectIPage = sysProjectService.pageFor(new Query<SysProject>()
                .setPageSize(1000), new SysProject(), Collections.emptySet());
        sysProjectIPage.getRecords().forEach(it -> {
            hikConfigurations.put(it.getSysProjectId(), it);
        });
    }

    /**
     * //        Set<String> resources = Set.of(
     * //                "region",
     * //                "acsDevice",
     * //                "door",
     * //                "reader",
     * //                "encodeDevice",
     * //                "camera",
     * //                "monitoringPointDevice",
     * //                "monitoringPoint"
     * //        );
     *
     * @param sysProjectId 系统项目ID，用于标识需要同步摄像头配置的项目
     * @param resources    资源列表，用于标识需要同步的摄像头资源
     * @param consumer     消费者，用于处理摄像头同步结果
     * @return
     */
    @Override
    public boolean sync(Integer sysProjectId, Set<String> resources, Consumer<DeviceResourceResourcesResult> consumer) {
        HikvisionClient hikvisionClient = getHikvisionClient(sysProjectId);
        if (null == hikvisionClient) {
            return false;
        }

        syncForResources(hikvisionClient, resources, consumer);
        return true;
    }

    @Override
    public boolean syncOrg(Integer sysProjectId, Consumer<HikRegion> consumer) {
        HikvisionClient hikvisionClient = getHikvisionClient(sysProjectId);
        if (null == hikvisionClient) {
            return false;
        }

        HikRegion regions = hikvisionClient.getResourceFactory().getRegionInfoFactory()
                .regions(1, 1000);
        if (null == regions) {
            return true;
        }

        if (CollectionUtils.isEmpty(regions.getList())) {
            return true;
        }
        consumer.accept(regions);
        return true;
    }

    @Override
    public boolean syncChannel(Integer sysProjectId, SysDevice sysDevice, Consumer<HikDeviceChannel> consumer) {
        HikvisionClient hikvisionClient = getHikvisionClient(sysProjectId);
        if (null == hikvisionClient) {
            return false;
        }

        List<HikDeviceChannel> channel = hikvisionClient.getResourceFactory().getRegionInfoFactory()
                .channel(sysDevice.getSysDeviceSerialNumber(), 1, 1000);

        if (CollectionUtils.isEmpty(channel)) {
            return true;
        }
        for (HikDeviceChannel hikDeviceChannel : channel) {
            consumer.accept(hikDeviceChannel);
        }
        return true;
    }

    @Override
    public List<SysDeviceCameraOnOffline> timeline(Integer sysProjectId, String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType) {
        HikvisionClient hikvisionClient = getHikvisionClient(sysProjectId);
        if (null == hikvisionClient) {
            return Collections.emptyList();
        }

        return hikvisionClient.getResourceFactory().getOnOfflineTimeline(
                sysCameraTemplateSerialNumber, sysCameraTemplateResourceType
        );
    }

    @Override
    public List<OnOffline> checkOnline(Integer sysProjectId, String[] serialNumbers) {
        HikvisionClient hikvisionClient = getHikvisionClient(sysProjectId);
        if (null == hikvisionClient) {
            return Collections.emptyList();
        }
        return hikvisionClient.getResourceFactory().checkOnOffline(
                serialNumbers
        );
    }

    @Override
    public String previewUrl(Integer sysProjectId, String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        HikvisionClient hikvisionClient = getHikvisionClient(sysProjectId);
        if (null == hikvisionClient) {
            throw new RuntimeException("未找到对应的项目");
        }
        return hikvisionClient.getResourceFactory().previewUrl(
                sysDeviceSerialNumber,
                sysDeviceResourceType,
                request
        );
    }

    @Override
    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        HikvisionClient hikvisionClient = getHikvisionClient(request.getSysProjectId());
        if (null == hikvisionClient) {
            throw new RuntimeException("未找到对应的项目");
        }
        return hikvisionClient.getResourceFactory().cardHistoryPage(request);
    }

    @Override
    public boolean syncEventMessage(CardDevicePageV1Request request) {
        HikvisionClient hikvisionClient = getHikvisionClient(request.getSysProjectId());
        if (null == hikvisionClient) {
            throw new RuntimeException("未找到对应的项目");
        }
        return hikvisionClient.getResourceFactory().syncEventMessage(request);
    }


    private void syncForResources(HikvisionClient hikvisionClient, Set<String> resources, Consumer<DeviceResourceResourcesResult> consumer) {
        for (String resource : resources) {
            syncForResource(hikvisionClient, resource, 1, consumer);
        }
    }

    private void syncForResource(HikvisionClient hikvisionClient, String resource, int page, Consumer<DeviceResourceResourcesResult> consumer) {
        DeviceResourceResourcesResult deviceResourceResourcesResult = hikvisionClient.getResourceFactory()
                .getZyxxFactory()
                .deviceResourceResources(page, 1000, resource);
        if (null == deviceResourceResourcesResult) {
            return;
        }

        if (CollectionUtils.isEmpty(deviceResourceResourcesResult.getList())) {
            return;
        }

        if (deviceResourceResourcesResult.getTotal() == 0) {
            return;
        }
        try {
            consumer.accept(deviceResourceResourcesResult);
        } catch (Exception ignored) {
        }
        syncForResource(hikvisionClient, resource, page + 1, consumer);
    }

    public HikvisionClient getHikvisionClient(Integer sysProjectId) {
        SysProject sysProject = hikConfigurations.get(sysProjectId);

        if (null == sysProject) {
            return null;
        }

        return hikvisionClientMap.computeIfAbsent(sysProjectId, schoolId1 -> {
            String hikPlatform = sysProject.getSysProjectDictItemCode();
            String sysProjectEndpoint = sysProject.getSysProjectEndpoint();
            if (null != sysProjectEndpoint && sysProjectEndpoint.contains("://")) {
                sysProjectEndpoint = sysProjectEndpoint.split("://")[1];
            }
            if ("HAI_KANG_AN_FANG".equals(hikPlatform)) {
                return new HikvisionClient(new String[]{"https"},
                        sysProjectEndpoint,
                        sysProject.getSysProjectAppId(),
                        sysProject.getSysProjectAppSecret()
                );
            }
            if ("HAI_KANG_YUN_YAO".equals(hikPlatform)) {
                return new HiYunYaoClient(new String[]{"https"},
                        sysProjectEndpoint,
                        sysProject.getSysProjectAppId(),
                        sysProject.getSysProjectAppSecret(),
                        sysProject
                );
            }

            return new HiYunMouClient(new String[]{"https"},
                    StringUtils.defaultIfBlank(sysProjectEndpoint, "api2.hik-cloud.com"),
                    sysProject.getSysProjectAppId(),
                    sysProject.getSysProjectAppSecret(),
                    sysProject
            );
        });
    }

    public List<HikvisionClient> listDevice() {
        if (hikConfigurations.isEmpty()) {
            return Collections.emptyList();
        }

        List<HikvisionClient> rs = new LinkedList<>();
        for (Map.Entry<Integer, SysProject> entry : hikConfigurations.entrySet()) {
            HikvisionClient hikvisionClient = getHikvisionClient(entry.getKey());


            if (null == hikvisionClient) {
                continue;
            }
            String orgIndexCode = getOrgIndexCode(entry.getKey());
            String teacherOrgIndexCode = getTeacherOrgIndexCode(entry.getKey());
            hikvisionClient.setStudentOrgIndexCode(orgIndexCode);
            hikvisionClient.setTeacherOrgIndexCode(teacherOrgIndexCode);
            hikvisionClient.setSchoolId(entry.getKey());
            rs.add(hikvisionClient);
        }

        return rs;
    }

    public String getParkCode(Integer carSchoolId) {
        SysProject attendanceSchoolHikFactory = hikConfigurations.get(carSchoolId);
        if (null == attendanceSchoolHikFactory) {
            return null;
        }

        // return attendanceSchoolHikFactory.getHikParkCode();
        return null;
    }

    public String getOrgIndexCode(Integer eduId) {
        //"e9c74314-ddcc-427a-9b1a-f6c6ddcc375c"
        SysProject attendanceSchoolHikFactory = hikConfigurations.get(eduId);
        if (null == attendanceSchoolHikFactory) {
            return null;
        }

//        return attendanceSchoolHikFactory.getHikOrgIndexCode();
        return null;
    }

    public String getTeacherOrgIndexCode(Integer eduId) {
        //"e9c74314-ddcc-427a-9b1a-f6c6ddcc375c"
        SysProject attendanceSchoolHikFactory = hikConfigurations.get(eduId);
        if (null == attendanceSchoolHikFactory) {
            return null;
        }

        //return attendanceSchoolHikFactory.getHikTeacherOrgIndexCode();
        return null;
    }
}
