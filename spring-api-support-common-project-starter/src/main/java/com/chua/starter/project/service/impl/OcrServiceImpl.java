package com.chua.starter.project.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.core.result.PredictResult;
import com.chua.deeplearning.support.core.feature.DetectionConfiguration;
import com.chua.deeplearning.support.core.function.Detector;
import com.chua.deeplearning.support.ml.identification.IdentificationEngine;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.OcrService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.entity.project.SysAiModule;
import com.chua.starter.entity.project.SysProject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * ocr接口
 *
 * @author CH
 * @since 2025/9/8 22:01
 */
@Service
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {
    private final SysAiModuleService sysAiModuleService;
    private final SysProjectService sysProjectService;

    @Override
    public ReturnResult<PredictResult> detect(File file, ImageRequest request) {
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
        DetectionConfiguration detectionConfiguration = DetectionConfiguration.builder()
                .apiKey(sysProject.getSysProjectAppKey())
                .appId(sysProject.getSysProjectAppId())
                .secretKey(sysProject.getSysProjectAppSecret())
                .endpoint(sysProject.getSysProjectEndpoint())
                .loadModelPath(sysProject.getSysProjectLocalPath())
                .modelName(request.getModel())
                .build();
        IdentificationEngine identificationEngine = IdentificationEngine.of(sysProject.getSysProjectDictItemCode(), detectionConfiguration);
        if (null == identificationEngine) {
            throw new RuntimeException("暂不支持");
        }
        Detector detector = identificationEngine.get(request.getProvider(), Detector.class);
        if (null == detector) {
            throw new RuntimeException("暂不支持");
        }

        var result = detector.detect(file);
        return ReturnResult.of(result.isEmpty() ? PredictResult.empty() : result.top1());
    }
}
