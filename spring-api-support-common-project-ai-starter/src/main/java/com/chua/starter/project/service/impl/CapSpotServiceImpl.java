package com.chua.starter.project.service.impl;

import com.chua.common.support.ai.DetectionConfiguration;
import com.chua.common.support.ai.Detector;
import com.chua.common.support.ai.result.PredictResult;
import com.chua.common.support.ai.result.PredictResultObject;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.identification.IdentificationEngine;
import com.chua.starter.entity.project.SysAiModule;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.CapSpotService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CapSpot 模板点击定位实现。
 *
 * @author CH
 * @since 2026/4/7 10:00
 */
@Service
@ConditionalOnClass(name = "com.chua.deeplearning.support.onnx.OnnxIdentificationEngine")
public class CapSpotServiceImpl implements CapSpotService {
    private final SysAiModuleService sysAiModuleService;
    private final SysProjectService sysProjectService;

    public CapSpotServiceImpl(SysAiModuleService sysAiModuleService,
                              SysProjectService sysProjectService) {
        this.sysAiModuleService = sysAiModuleService;
        this.sysProjectService = sysProjectService;
    }

    @Override
    public ReturnResult<PredictResultObject<PredictResult>> detect(File templateFile, File targetFile, ImageRequest request) {
        SysProject sysProject = check(request, templateFile, targetFile);
        DetectionConfiguration detectionConfiguration = buildDetectionConfiguration(sysProject, request);
        IdentificationEngine identificationEngine = createIdentificationEngine(sysProject, detectionConfiguration);
        if (null == identificationEngine) {
            throw new RuntimeException("暂不支持");
        }
        Detector detector = identificationEngine.get(request.getProvider(), Detector.class);
        if (null == detector) {
            throw new RuntimeException("暂不支持");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("template", templateFile);
        payload.put("target", targetFile);

        PredictResultObject<PredictResult> result = detector.detect(payload);
        return ReturnResult.success(null == result ? PredictResultObject.empty() : result);
    }

    protected IdentificationEngine createIdentificationEngine(SysProject sysProject,
                                                              DetectionConfiguration detectionConfiguration) {
        return IdentificationEngine.of(String.valueOf(sysProject.getSysProjectVender()), detectionConfiguration);
    }

    private DetectionConfiguration buildDetectionConfiguration(SysProject sysProject, ImageRequest request) {
        String modelPath = StringUtils.isBlank(sysProject.getSysProjectLocalPath())
                ? sysProject.getSysProjectEndpoint()
                : sysProject.getSysProjectLocalPath();
        return DetectionConfiguration.builder()
                .apiKey(sysProject.getSysProjectAppKey())
                .appId(sysProject.getSysProjectAppId())
                .secretKey(sysProject.getSysProjectAppSecret())
                .endpoint(sysProject.getSysProjectEndpoint())
                .loadModelPath(modelPath)
                .modelName(request.getModel())
                .build();
    }

    private SysProject check(ImageRequest request, File templateFile, File targetFile) {
        if (null == request || StringUtils.isBlank(request.getRequestId())) {
            throw new RuntimeException("requestId不能为空");
        }

        if (StringUtils.isBlank(request.getModel())) {
            throw new RuntimeException("模型不能为空");
        }

        Integer sysProjectId = Integer.valueOf(request.getRequestId());
        SysAiModule sysAiModule = sysAiModuleService.getOneByCode(sysProjectId, request.getModel());
        if (null == sysAiModule) {
            throw new RuntimeException("模型不存在");
        }

        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (null == sysProject) {
            throw new RuntimeException("项目不存在");
        }

        if (null == templateFile) {
            throw new RuntimeException("模板文件不能为空");
        }

        if (null == targetFile) {
            throw new RuntimeException("目标文件不能为空");
        }
        return sysProject;
    }
}
