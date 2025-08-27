package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bigmodel.generations.image.VincentDiagramSetting;
import com.chua.common.support.bigmodel.generations.video.*;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.project.mapper.SysAiVincentTaskMapper;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.project.service.VideoGenerationService;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysAiVincentTask;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1SearchTaskRequest;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * 文生图服务
 *
 * @author CH
 * @since 2024/9/10
 */
@Service
@RequiredArgsConstructor
public class VideoGenerationServiceImpl implements VideoGenerationService {

    static final String LOCATION_PATH = "./ai/location/video/generator";
    final SysAiModuleService sysAiModuleService;
    final SysProjectService sysProjectService;
    final SysAiVincentTaskMapper sysAiVincentTaskMapper;

    @Override
    public ReturnResult<VincentDiagramV1AddTaskResponse> addTask(VincentDiagramV1AddTaskRequest request) {
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
        VideoGenerationsClient videoGenerationsClient = ServiceProvider.of(VideoGenerationsClient.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        VincentDiagramSetting.builder()
                                .host(StringUtils.defaultString(sysAiModule.getSysAiModuleUrl(), sysProject.getSysProjectEndpoint()))
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == videoGenerationsClient) {
            throw new RuntimeException("暂不支持");
        }

        VideoGenerationsAddTaskV1Request taskV1Request = VideoGenerationsAddTaskV1Request.builder()
                .sysProjectId(sysProjectId)
                .model(request.getModel())
                .input(VideoGenerationsAddTaskV1Request.Input.builder()
                        .prompt(request.getInput().getPrompt())
                        .negativePrompt(request.getInput().getNegativePrompt())
                        .refImg(request.getInput().getRefImg()).build()
                )
                .parameters(VideoGenerationsAddTaskV1Request.Parameters.builder()
                        .number(request.getParameters().getNumber())
                        .refStrength(request.getParameters().getRefStrength())
                        .size(request.getParameters().getSize())
                        .withAudio(ObjectUtils.equals(request.getParameters().getWithAudio(), 1))
                        .quality(request.getParameters().getQuality())
                        .fps(request.getParameters().getFps())
                        .seed(request.getParameters().getSeed())
                        .style(request.getParameters().getStyle()).build()
                )
                .build();
        VideoGenerationsAddTaskV1Response vincentDiagramAddTaskV1Response = videoGenerationsClient.addTask(taskV1Request);
        SysAiVincentTask task = new SysAiVincentTask();
        task.setSysProjectId(sysProjectId);
        task.setSysAiModuleId(sysAiModule.getSysAiModuleId());
        task.setSysAiVincentTaskCondition(Json.toJson(taskV1Request));
        task.setSysAiVincentTaskUid(vincentDiagramAddTaskV1Response.getOutput().getTaskId());
        VideoGenerationsAddTaskV1Response.OutputDTO output = vincentDiagramAddTaskV1Response.getOutput();
        if ("FAILURE".equals(output.getTaskStatus())) {
            task.setSysAiVincentTaskStatus(2);
        } else {
            task.setSysAiVincentTaskStatus(0);
            if (null != output.getImages()) {
                task.setSysAiVincentTaskUrl(Joiner.on(",").skipNulls().join(output.getImages()));
                if (StringUtils.isNotBlank(task.getSysAiVincentTaskUrl())) {
                    task.setSysAiVincentTaskStatus(1);
                }
            }
        }
        if (task.getSysAiVincentTaskCondition().length() > 1000) {
            task.setSysAiVincentTaskCondition(Json.toJson(VideoGenerationsAddTaskV1Request.builder()
                    .sysProjectId(sysProjectId)
                    .model(request.getModel())
                    .input(VideoGenerationsAddTaskV1Request.Input.builder()
                            .prompt(request.getInput().getPrompt())
                            .negativePrompt(request.getInput().getNegativePrompt())
                            .build()
                    )
                    .parameters(VideoGenerationsAddTaskV1Request.Parameters.builder()
                            .number(request.getParameters().getNumber())
                            .refStrength(request.getParameters().getRefStrength())
                            .size(request.getParameters().getSize())
                            .withAudio(ObjectUtils.equals(request.getParameters().getWithAudio(), 1))
                            .quality(request.getParameters().getQuality())
                            .fps(request.getParameters().getFps())
                            .seed(request.getParameters().getSeed())
                            .style(request.getParameters().getStyle()).build()
                    )
                    .build()));
        }
        task.setSysAiVincentTaskType("VIDEO");
        sysAiVincentTaskMapper.insert(task);
        VincentDiagramV1AddTaskResponse response = new VincentDiagramV1AddTaskResponse();
        response.setRequestId(vincentDiagramAddTaskV1Response.getRequestId());
        response.setOutput(new VincentDiagramV1AddTaskResponse.OutputDTO());
        response.getOutput().setTaskStatus(vincentDiagramAddTaskV1Response.getOutput().getTaskStatus());
        response.getOutput().setTaskId(vincentDiagramAddTaskV1Response.getOutput().getTaskId());
        response.getOutput().setImages(vincentDiagramAddTaskV1Response.getOutput().getImages());
        return ReturnResult.success(response);
    }

    @Override
    public ReturnResult<VideoGenerationsSearchTaskV1Response> searchTask(VincentDiagramV1SearchTaskRequest request) {
        Integer sysProjectId = request.getSysProjectId();
        if (null == sysProjectId) {
            throw new RuntimeException("项目不存在");
        }

        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (null == sysProject) {
            throw new RuntimeException("项目不存在");
        }
        VideoGenerationsClient videoGenerationsClient = ServiceProvider.of(VideoGenerationsClient.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        VincentDiagramSetting.builder()
                                .host(sysProject.getSysProjectEndpoint())
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == videoGenerationsClient) {
            throw new RuntimeException("暂不支持");
        }
        VideoGenerationsSearchTaskV1Response videoGenerationsSearchTaskV1Response = videoGenerationsClient.searchTask(
                VideoGenerationsSearchTaskV1Request.builder()
                        .taskId(request.getTaskId())
                        .build()
        );
        VideoGenerationsSearchTaskV1Response.OutputDTO output = videoGenerationsSearchTaskV1Response.getOutput();
        //PENDING：任务排队中
        //RUNNING：任务处理中
        //SUSPENDED：任务挂起
        //SUCCEEDED：任务执行成功
        //FAILED：任务执行失败
        //UNKNOWN：任务不存在或状态未知
        String taskStatus = output.getTaskStatus();
        List<String> imageList = Optional.ofNullable(output.getResults()).orElse(Collections.emptyList())
                .stream().map(VideoGenerationsSearchTaskV1Response.OutputDTO.ResultsDTO::getUrl).toList();
        String images = String.join(",", imageList);
        String taskLocalUrl = ImageGenerationServiceImpl.createTaskLocalUrl(StringUtils.defaultString(sysProject.getSysProjectLocationTempPath(), LOCATION_PATH), imageList);
        sysAiVincentTaskMapper.update(Wrappers.<SysAiVincentTask>lambdaUpdate()
                .set(SysAiVincentTask::getSysAiVincentTaskStatus, ImageGenerationServiceImpl.getStatus(taskStatus))
                .set(SysAiVincentTask::getSysAiVincentTaskRequestId, videoGenerationsSearchTaskV1Response.getRequestId())
                .set(SysAiVincentTask::getSysAiVincentTaskUrl, images)
                .set(SysAiVincentTask::getSysAiVincentTaskLocalUrl, taskLocalUrl)
                .eq(SysAiVincentTask::getSysAiVincentTaskUid, request.getTaskId())
        );
        return ReturnResult.ok(videoGenerationsSearchTaskV1Response);
    }

}
