package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.system.entity.SysDeviceMessageEvent;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.mapper.SysDeviceMessageEventMapper;
import com.chua.starter.system.mapper.SysProjectMapper;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import com.chua.starter.system.service.SysDeviceMessageEventService;
import com.chua.starter.system.service.support.CameraService;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.chua.starter.system.service.impl.SysDeviceServiceImpl.reload;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysDeviceMessageEventServiceImpl extends ServiceImpl<SysDeviceMessageEventMapper, SysDeviceMessageEvent> implements
        SysDeviceMessageEventService,
        InitializingBean,
        DisposableBean {
    final SysProjectMapper sysProjectMapper;
    List<ExecutorService> executors = new LinkedList<>();

    @Override
    public ReturnResult<Boolean> syncEventMessage(CardDevicePageV1Request request) {
        CameraService cameraService = reload(sysProjectMapper, request.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        return ReturnResult.preconditioning(true).toResult();
    }

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
