package com.chua.starter.project.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.core.result.PredictResult;
import com.chua.deeplearning.support.core.function.api.Similar;
import com.chua.deeplearning.support.core.function.api.Detector;
import com.chua.deeplearning.support.core.function.api.Feature;
import com.chua.deeplearning.support.core.feature.DetectionConfiguration;
import com.chua.deeplearning.support.core.composite.Recognizer;
import com.chua.deeplearning.support.ml.identification.IdentificationEngine;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.FaceService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.entity.project.SysAiModule;
import com.chua.starter.entity.project.SysProject;
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
        Detector faceDetector = identificationEngine.get(request.getProvider(), Detector.class);
        if (null == faceDetector) {
            throw new RuntimeException("暂不支持");
        }

        var result = faceDetector.detect(file);
        return ReturnResult.of(result.isEmpty() ? PredictResult.empty() : result.top1());
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
        Feature faceFeature = identificationEngine.get(request.getProvider(), Feature.class);
        if (null == faceFeature) {
            throw new RuntimeException("暂不支持");
        }

        return ReturnResult.of(faceFeature.extract(file));
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

        return ReturnResult.of(recognizer.recognize(file));
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
        Similar<float[]> similar = identificationEngine.get(request.getProvider(), Similar.class);
        if (null == similar) {
            throw new RuntimeException("暂不支持");
        }
        
        Feature feature = identificationEngine.get(request.getProvider(), Feature.class);
        if (null == feature) {
            throw new RuntimeException("暂不支持");
        }
        
        PredictResult feature1 = feature.extract(file);
        PredictResult feature2 = feature.extract(file2);
        
        if (feature1.getValue() instanceof float[] && feature2.getValue() instanceof float[]) {
            double similarity = similar.featureSimilarity((float[]) feature1.getValue(), (float[]) feature2.getValue());
            PredictResult result = new PredictResult();
            result.setValue(similarity);
            return ReturnResult.of(result);
        }
        
        throw new RuntimeException("特征提取失败");
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
