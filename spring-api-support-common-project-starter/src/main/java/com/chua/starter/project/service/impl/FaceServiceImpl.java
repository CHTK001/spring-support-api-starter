package com.chua.starter.project.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.PredictResult;
import com.chua.deeplearning.support.ml.compare.Compare;
import com.chua.deeplearning.support.ml.feature.DetectionConfiguration;
import com.chua.deeplearning.support.ml.function.FaceDetector;
import com.chua.deeplearning.support.ml.function.FaceFeature;
import com.chua.deeplearning.support.ml.identification.IdentificationEngine;
import com.chua.deeplearning.support.ml.recognizer.Recognizer;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.FaceService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.project.entity.SysAiModule;
import com.chua.starter.project.entity.SysProject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 人脸接口实现
 *
 * @author CH
 * @since 2025/9/8 21:34
 */
@Service
@RequiredArgsConstructor
public class FaceServiceImpl implements FaceService {
    private final SysAiModuleService sysAiModuleService;
    private final SysProjectService sysProjectService;

    @Override
    public ReturnResult<PredictResult> detect(File file, ImageRequest request) {
        SysProject sysProject = check(request, file);
        DetectionConfiguration detectionConfiguration = DetectionConfiguration.builder()
                .apiKey(sysProject.getSysProjectAppKey())
                .appId(sysProject.getSysProjectAppId())
                .secretKey(sysProject.getSysProjectAppSecret())
                .endpoint(sysProject.getSysProjectEndpoint())
                .loadModelPath(sysProject.getSysProjectEndpoint())
                .modelName(request.getModel())
                .build();
        IdentificationEngine identificationEngine = IdentificationEngine.of(sysProject.getSysProjectDictItemCode(), detectionConfiguration);
        if (null == identificationEngine) {
            throw new RuntimeException("暂不支持");
        }
        FaceDetector faceDetector = identificationEngine.get(request.getProvider(), FaceDetector.class);
        if (null == faceDetector) {
            throw new RuntimeException("暂不支持");
        }

        return ReturnResult.of(faceDetector.predict(file));
    }

    @Override
    public ReturnResult<PredictResult> feature(File file, ImageRequest request) {
        SysProject sysProject = check(request, file);
        DetectionConfiguration detectionConfiguration = DetectionConfiguration.builder()
                .apiKey(sysProject.getSysProjectAppKey())
                .appId(sysProject.getSysProjectAppId())
                .secretKey(sysProject.getSysProjectAppSecret())
                .endpoint(sysProject.getSysProjectEndpoint())
                .loadModelPath(sysProject.getSysProjectEndpoint())
                .modelName(request.getModel())
                .build();
        IdentificationEngine identificationEngine = IdentificationEngine.of(sysProject.getSysProjectDictItemCode(), detectionConfiguration);
        if (null == identificationEngine) {
            throw new RuntimeException("暂不支持");
        }
        FaceFeature faceFeature = identificationEngine.get(request.getProvider(), FaceFeature.class);
        if (null == faceFeature) {
            throw new RuntimeException("暂不支持");
        }

        return ReturnResult.of(faceFeature.predict(file));
    }

    @Override
    public ReturnResult<PredictResult> recognizer(File file, ImageRequest request) {
        SysProject sysProject = check(request, file);
        DetectionConfiguration detectionConfiguration = DetectionConfiguration.builder()
                .apiKey(sysProject.getSysProjectAppKey())
                .appId(sysProject.getSysProjectAppId())
                .secretKey(sysProject.getSysProjectAppSecret())
                .endpoint(sysProject.getSysProjectEndpoint())
                .loadModelPath(sysProject.getSysProjectEndpoint())
                .modelName(request.getModel())
                .build();
        IdentificationEngine identificationEngine = IdentificationEngine.of(sysProject.getSysProjectDictItemCode(), detectionConfiguration);
        if (null == identificationEngine) {
            throw new RuntimeException("暂不支持");
        }
        Recognizer recognizer = identificationEngine.get(request.getProvider(), Recognizer.class);
        if (null == recognizer) {
            throw new RuntimeException("暂不支持");
        }

        return ReturnResult.of(recognizer.predict(file));
    }

    @Override
    public ReturnResult<PredictResult> compare(File file, File file2, ImageRequest request) {
        SysProject sysProject = check(request, file);
        DetectionConfiguration detectionConfiguration = DetectionConfiguration.builder()
                .apiKey(sysProject.getSysProjectAppKey())
                .appId(sysProject.getSysProjectAppId())
                .secretKey(sysProject.getSysProjectAppSecret())
                .endpoint(sysProject.getSysProjectEndpoint())
                .loadModelPath(sysProject.getSysProjectEndpoint())
                .modelName(request.getModel())
                .build();
        IdentificationEngine identificationEngine = IdentificationEngine.of(sysProject.getSysProjectDictItemCode(), detectionConfiguration);
        if (null == identificationEngine) {
            throw new RuntimeException("暂不支持");
        }
        Compare compare = identificationEngine.get(request.getProvider(), Compare.class);
        if (null == compare) {
            throw new RuntimeException("暂不支持");
        }

        return ReturnResult.of(compare.calculateSimilar(file, file2));
    }

    /**
     * 检查
     *
     * @param request request
     * @param file    file
     * @return SysProject
     */
    private SysProject check(ImageRequest request, File file) {
        if (StringUtils.isBlank(request.getRequestId())) {
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

        if (null == file) {
            throw new RuntimeException("文件不能为空");
        }
        return sysProject;
    }
}
