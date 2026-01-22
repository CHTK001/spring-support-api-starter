package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.deeplearning.support.core.function.model.ImageToResolutionGenerator;
import com.chua.deeplearning.support.core.request.ImageResolutionAddTaskV1Request;
import com.chua.deeplearning.support.core.request.ImageResolutionSearchTaskV1Request;
import com.chua.deeplearning.support.core.response.ImageResolutionAddTaskV1Response;
import com.chua.deeplearning.support.core.response.ImageResolutionSearchTaskV1Response;
import com.chua.deeplearning.support.ml.client.resolution.ResolutionSetting;
import com.chua.common.support.io.binary.ByteSourceMultiFile;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.project.mapper.SysAiVincentTaskMapper;
import com.chua.starter.project.service.ImageResolutionService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.entity.project.SysAiModule;
import com.chua.starter.entity.project.SysAiVincentTask;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.pojo.vincent.ImageResolutionAddTaskV2Request;
import com.chua.starter.project.pojo.vincent.ImageResolutionSearchTaskV2Request;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

/**
 * 超分辨率接口
 *
 * @author CH
 */
@Service
@SpringBootConfiguration
@RequiredArgsConstructor
public class ImageResolutionServiceImpl implements ImageResolutionService {
    static final String LOCATION_PATH = "./ai/location/resolution";
    final SysAiModuleService sysAiModuleService;
    final SysProjectService sysProjectService;
    final SysAiVincentTaskMapper sysAiVincentTaskMapper;

    @Override
    public ReturnResult<ImageResolutionAddTaskV1Response> addTask(ImageResolutionAddTaskV2Request request) {
        if (StringUtils.isBlank(request.getModel())) {
            throw new RuntimeException("模型不能为空");
        }

        Integer sysProjectId = request.getSysProjectId();
        SysAiModule sysAiModule = sysAiModuleService.getOneByCode(sysProjectId, request.getModel());
        if (null == sysAiModule) {
            throw new RuntimeException("模型不存在");
        }

        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (null == sysProject) {
            throw new RuntimeException("项目不存在");
        }
        ImageToResolutionGenerator imageResolutionClient = ServiceProvider.of(ImageToResolutionGenerator.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        com.chua.deeplearning.support.ml.client.resolution.ResolutionSetting.builder()
                                .host(StringUtils.defaultString(sysAiModule.getSysAiModuleUrl(), sysProject.getSysProjectEndpoint()))
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == imageResolutionClient) {
            throw new RuntimeException("暂不支持");
        }

        ImageResolutionAddTaskV1Request requestV1 = null;
        try {
            requestV1 = ImageResolutionAddTaskV1Request.builder()
                    .model(request.getModel())
                    .scaleFactor(request.getScaleFactor())
                    .byteSource(new ByteSourceMultiFile(request.getUrl())).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImageResolutionAddTaskV1Response imageResolutionAddTaskV1Response = imageResolutionClient.addTask(requestV1);
        SysAiVincentTask task = new SysAiVincentTask();
        task.setSysProjectId(sysProjectId);
        task.setSysAiModuleId(sysAiModule.getSysAiModuleId());
        task.setSysAiVincentTaskCondition(Json.toJson(request));
        task.setSysAiVincentTaskUid(imageResolutionAddTaskV1Response.getTaskId());
        task.setSysAiVincentTaskStatus(0);
        task.setSysAiVincentTaskType("RESOLUTION");
        sysAiVincentTaskMapper.insert(task);
        return ReturnResult.success(imageResolutionAddTaskV1Response);
    }

    @Override
    public ReturnResult<ImageResolutionSearchTaskV1Response> searchTask(ImageResolutionSearchTaskV2Request request) {
        Integer sysProjectId = request.getSysProjectId();
        if (null == sysProjectId) {
            throw new RuntimeException("项目不存在");
        }

        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (null == sysProject) {
            throw new RuntimeException("项目不存在");
        }
        ImageToResolutionGenerator imageResolutionClient = ServiceProvider.of(ImageToResolutionGenerator.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        com.chua.deeplearning.support.ml.client.resolution.ResolutionSetting.builder()
                                .host(sysProject.getSysProjectEndpoint())
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == imageResolutionClient) {
            throw new RuntimeException("暂不支持");
        }
        ImageResolutionSearchTaskV1Response imageResolutionSearchTaskV1Response = imageResolutionClient.searchTask(
                ImageResolutionSearchTaskV1Request.builder()
                        .taskId(request.getTaskId())
                        .model(request.getModel())
                        .region(request.getRegion())
                        .build()
        );
        Integer progress = imageResolutionSearchTaskV1Response.getProgress();
        String taskStatus = progress != null && progress >= 100 ? "SUCCESS" : (progress != null && progress > 0 ? "PROCESSING" : "PENDING");
        String images = imageResolutionSearchTaskV1Response.getImage();
        String taskLocalUrl = ImageGenerationServiceImpl.createTaskLocalUrl(StringUtils.defaultString(sysProject.getSysProjectLocationTempPath(), LOCATION_PATH), Collections.singletonList(images));
        sysAiVincentTaskMapper.update(Wrappers.<SysAiVincentTask>lambdaUpdate()
                .set(SysAiVincentTask::getSysAiVincentTaskStatus, ImageGenerationServiceImpl.getStatus(taskStatus))
                .set(SysAiVincentTask::getSysAiVincentTaskRequestId, progress != null ? String.valueOf(progress) : null)
                .set(SysAiVincentTask::getSysAiVincentTaskUrl, images)
                .set(SysAiVincentTask::getSysAiVincentTaskLocalUrl, taskLocalUrl)
                .eq(SysAiVincentTask::getSysAiVincentTaskUid, request.getTaskId())
        );
        return ReturnResult.ok(imageResolutionSearchTaskV1Response);
    }
}
