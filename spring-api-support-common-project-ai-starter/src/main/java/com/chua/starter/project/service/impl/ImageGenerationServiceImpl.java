package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.deeplearning.support.core.function.model.ImageToImageGenerator;
import com.chua.deeplearning.support.core.request.ImageGenerationsAddTaskV1Request;
import com.chua.deeplearning.support.core.request.ImageGenerationsSearchTaskV1Request;
import com.chua.deeplearning.support.core.response.ImageGenerationsAddTaskV1Response;
import com.chua.deeplearning.support.core.response.ImageGenerationsSearchTaskV1Response;
import com.chua.deeplearning.support.ml.client.generations.image.VincentDiagramSetting;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.common.support.core.utils.UrlUtils;
import com.chua.starter.project.mapper.SysAiVincentTaskMapper;
import com.chua.starter.project.service.ImageGenerationService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.entity.project.SysAiModule;
import com.chua.starter.entity.project.SysAiVincentTask;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1SearchTaskRequest;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;



/**
 * 文生图服务
 *
 * @author CH
 * @since 2024/9/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationServiceImpl implements ImageGenerationService {

    final SysAiModuleService sysAiModuleService;
    final SysProjectService sysProjectService;
    final SysAiVincentTaskMapper sysAiVincentTaskMapper;

    static final String LOCATION_PATH = "./ai/location/image/generator";
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
        ImageToImageGenerator vincentDiagramClient = ServiceProvider.of(ImageToImageGenerator.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        VincentDiagramSetting.builder()
                                .host(StringUtils.defaultString(sysAiModule.getSysAiModuleUrl(), sysProject.getSysProjectEndpoint()))
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == vincentDiagramClient) {
            throw new RuntimeException("暂不支持");
        }

        ImageGenerationsAddTaskV1Request taskV1Request = ImageGenerationsAddTaskV1Request.builder()
                .sysProjectId(sysProjectId)
                .model(request.getModel())
                .input(ImageGenerationsAddTaskV1Request.Input.builder()
                        .prompt(request.getInput().getPrompt())
                        .negativePrompt(request.getInput().getNegativePrompt())
                        .refImg(request.getInput().getRefImg()).build()
                )
                .parameters(ImageGenerationsAddTaskV1Request.Parameters.builder()
                        .number(request.getParameters().getNumber())
                        .refStrength(request.getParameters().getRefStrength())
                        .size(request.getParameters().getSize())
                        .seed(request.getParameters().getSeed())
                        .style(request.getParameters().getStyle()).build()
                )
                .build();
        log.info("当前任务模型:{}", taskV1Request.getModel());
        ImageGenerationsAddTaskV1Response vincentDiagramAddTaskV1Response = vincentDiagramClient.addTask(taskV1Request);
        SysAiVincentTask task = new SysAiVincentTask();
        task.setSysProjectId(sysProjectId);
        task.setSysAiModuleId(sysAiModule.getSysAiModuleId());
        task.setSysAiVincentTaskCondition(Json.toJson(taskV1Request));
        task.setSysAiVincentTaskUid(vincentDiagramAddTaskV1Response.getOutput().getTaskId());
        task.setSysAiVincentTaskStatus(0);
        ImageGenerationsAddTaskV1Response.OutputDTO output = vincentDiagramAddTaskV1Response.getOutput();
        if (null != output && null != output.getImages()) {
            task.setSysAiVincentTaskUrl(Joiner.on(",").skipNulls().join(vincentDiagramAddTaskV1Response.getOutput().getImages()));
            task.setSysAiVincentTaskLocalUrl(createTaskLocalUrl(StringUtils.defaultString(sysProject.getSysProjectLocationTempPath(), LOCATION_PATH), vincentDiagramAddTaskV1Response.getOutput().getImages()));
            if (task.getSysAiVincentTaskUrl().length() > 255) {
                task.setSysAiVincentTaskUrl(null);
            }
        }
        if (StringUtils.isNotBlank(task.getSysAiVincentTaskUrl())) {
            task.setSysAiVincentTaskStatus(1);
        }
        task.setSysAiVincentTaskType("IMAGE");
        sysAiVincentTaskMapper.insert(task);
        VincentDiagramV1AddTaskResponse response = new VincentDiagramV1AddTaskResponse();
        response.setRequestId(vincentDiagramAddTaskV1Response.getRequestId());
        response.setOutput(new VincentDiagramV1AddTaskResponse.OutputDTO());
        if (null != output) {
            response.getOutput().setTaskStatus(null != output.getTaskStatus() ? output.getTaskStatus() : "FAILURE");
            response.getOutput().setTaskId(vincentDiagramAddTaskV1Response.getOutput().getTaskId());
            response.getOutput().setImages(vincentDiagramAddTaskV1Response.getOutput().getImages());
        }
        return ReturnResult.success(response);
    }

    public static String createTaskLocalUrl(String sysProjectLocationTempPath, List<String> images) {
        FileUtils.createDirectory(new File(sysProjectLocationTempPath));
        List<String> rs = new LinkedList<>();
        for (String image : images) {
            rs.add(createLocationUrl(sysProjectLocationTempPath, image));
        }
        return Joiner.on(",").join(rs);
    }

    private static String createLocationUrl(String sysProjectLocationTempPath, String image) {
        try {
            if (image.startsWith("http")) {
                return createHttpFile(sysProjectLocationTempPath, image);
            }
            if (image.contains(";base64,")) {
                return createBase64File(sysProjectLocationTempPath, image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String createBase64File(String sysProjectLocationTempPath, String image) {
        //data:image/png;base64,
        String substring = image.substring(0, image.indexOf(";base64,")).replace("data:", "");
        String base64Url = image.substring(image.indexOf(";base64,") + 8);
        String suffix = "";
        if (substring.startsWith("image") || substring.startsWith("video") || substring.startsWith("audio")) {
            suffix = substring.substring(substring.indexOf("/") + 1);
        }
        File file = new File(sysProjectLocationTempPath, UUID.randomUUID() + "." + suffix);
        FileUtils.writeTo(Base64.getDecoder().decode(base64Url), file);
        return FileUtils.normalize(sysProjectLocationTempPath, file.getName());
    }

    private static String createHttpFile(String sysProjectLocationTempPath, String image) {
        URL url = null;
        try {
            url = new URI(image).toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (InputStream inputStream = url.openStream()) {
            String fileName = UrlUtils.getFileName(image);
            File file = new File(sysProjectLocationTempPath, fileName);
            FileUtils.writeTo(inputStream, file);
            return FileUtils.normalize(sysProjectLocationTempPath, file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getStatus(String taskStatus) {
        return switch (taskStatus) {
            case "SUSPENDED", "FAILED", "FAILURE", "UNKNOWN" -> 2;
            case "SUCCEEDED", "SUCCESS" -> 1;
            default -> 0;
        };
    }

    @Override
    public List<SysAiVincentTask> history(Integer userId, String sysAiVincentTaskType) {
        return sysAiVincentTaskMapper.history(new Page<>(1, 10), userId, sysAiVincentTaskType).getRecords();
    }

    @Override
    public ReturnResult<ImageGenerationsSearchTaskV1Response> searchTask(VincentDiagramV1SearchTaskRequest request) {
        Integer sysProjectId = request.getSysProjectId();
        if (null == sysProjectId) {
            throw new RuntimeException("项目不存在");
        }

        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (null == sysProject) {
            throw new RuntimeException("项目不存在");
        }
        ImageToImageGenerator vincentDiagramClient = ServiceProvider.of(ImageToImageGenerator.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        VincentDiagramSetting.builder()
                                .host(sysProject.getSysProjectEndpoint())
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == vincentDiagramClient) {
            throw new RuntimeException("暂不支持");
        }
        ImageGenerationsSearchTaskV1Response vincentDiagramSearchTaskV1Response = vincentDiagramClient.searchTask(
                ImageGenerationsSearchTaskV1Request.builder()
                        .taskId(request.getTaskId())
                        .model(request.getModel())
                        .region(request.getRegion())
                        .build()
        );
        ImageGenerationsSearchTaskV1Response.OutputDTO output = vincentDiagramSearchTaskV1Response.getOutput();
        //PENDING：任务排队中
        //RUNNING：任务处理中
        //SUSPENDED：任务挂起
        //SUCCEEDED：任务执行成功
        //FAILED：任务执行失败
        //UNKNOWN：任务不存在或状态未知
        String taskStatus = null == output ? "SUCCESS" : output.getTaskStatus();
        if (null != output) {
            List<String> imageList = Optional.ofNullable(vincentDiagramSearchTaskV1Response.getResults()).orElse(Collections.emptyList())
                    .stream().map(ImageGenerationsSearchTaskV1Response.ResultsDTO::getUrl).toList();
            String images = String.join(",", imageList);
            String taskLocalUrl = createTaskLocalUrl(StringUtils.defaultString(sysProject.getSysProjectLocationTempPath(), LOCATION_PATH), imageList);
            sysAiVincentTaskMapper.update(Wrappers.<SysAiVincentTask>lambdaUpdate()
                    .set(SysAiVincentTask::getSysAiVincentTaskStatus, getStatus(taskStatus))
                    .set(SysAiVincentTask::getSysAiVincentTaskRequestId, vincentDiagramSearchTaskV1Response.getRequestId())
                    .set(SysAiVincentTask::getSysAiVincentTaskUrl, images)
                    .set(SysAiVincentTask::getSysAiVincentTaskLocalUrl, taskLocalUrl)
                    .eq(SysAiVincentTask::getSysAiVincentTaskUid, request.getTaskId())
            );
        }
        return ReturnResult.ok(vincentDiagramSearchTaskV1Response);
    }
}
