package com.chua.starter.project.scheduler.device;

import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.project.mapper.SysProjectMapper;
import com.chua.starter.project.service.support.CameraService;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;
import com.chua.starter.entity.service.SysDictItem;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringBootConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.chua.starter.project.service.impl.SysDeviceServiceImpl.reload;


/**
 * 门禁设备
 *
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
@SpringBootConfiguration
public class CardDeviceScheduler implements InitializingBean,
        DisposableBean {

    final SysProjectMapper sysProjectMapper;
    List<ExecutorService> executors = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        listenYunMou();
    }

    private void listenYunMou() {
        Thread.ofVirtual()
                .start(() -> {
                    List<SysProject> haiKangYunMou = sysProjectMapper.selectList(
                            MPJWrappers.<SysProject>lambdaJoin()
                                    .selectAll(SysProject.class)
                                    .innerJoin(SysDictItem.class, SysDictItem::getSysDictItemId, SysProject::getSysProjectVender)
                                    .eq(SysDictItem::getSysDictItemCode, "HAI_KANG_YUN_MOU")
                                    .eq(SysProject::getSysProjectDelete, 0)
                                    .eq(SysProject::getSysProjectStatus, 1)
                    );
                    for (SysProject sysProject : haiKangYunMou) {
                        executors.add(createExecutorService(sysProject));
                    }
                });
    }

    private ExecutorService createExecutorService(SysProject sysProject) {
        ScheduledExecutorService executor = ThreadUtils.newScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(
                () -> {
                    CameraService cameraService = reload(sysProjectMapper, sysProject.getSysProjectId());
                    if (null == cameraService) {
                        return;
                    }
                    try {
                        log.info("开始同步设备[{}]的报警信息", sysProject.getSysProjectName());
                        CardDevicePageV1Request request = new CardDevicePageV1Request();
                        request.setSysProjectId(sysProject.getSysProjectId());
                        cameraService.syncEventMessage(request);
                    } catch (Exception ignored) {
                    }
                }
                , 0, 5, TimeUnit.MINUTES);
        return executor;
    }

    @Override
    public void destroy() throws Exception {
        for (ExecutorService executor : executors) {
            ThreadUtils.shutdownNow(executor);
        }
    }
}
