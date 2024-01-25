package com.chua.starter.learning.service;

import com.chua.common.support.ml.PredictResult;
import com.chua.common.support.ml.PredictResultObject;
import com.chua.common.support.ml.compare.Compare;
import com.chua.common.support.ml.detector.Detector;
import com.chua.common.support.ml.feature.DetectionConfiguration;
import com.chua.common.support.ml.feature.Feature;
import com.chua.common.support.ml.identification.IdentificationEngine;
import com.chua.common.support.ml.recognizer.Recognizer;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.learning.pojo.LearningRequest;
import com.chua.starter.learning.properties.LearningProperties;
import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 学习配置服务
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/24
 */
@Service
public class LearningConfigServiceImpl implements LearningConfigService, DisposableBean {
    @Resource
    private LearningProperties learningProperties;

    static final Map<String, IdentificationEngine> ENGINE_MAP = new ConcurrentReferenceHashMap<>();

    @Override
    public PredictResultObject<PredictResult> detect(LearningRequest request) {
        IdentificationEngine identificationEngine = ENGINE_MAP.compute(request.getEngine(), (s, identificationEngine1) -> IdentificationEngine.of(s, DetectionConfiguration.builder()
                .cachePath(learningProperties.getModulePath())
                .build()
        ));

        Detector detector = identificationEngine.get(request.getModuleType(), request.getImplName(), Detector.class);
        return detector.predict(request.getFile());
    }

    @Override
    public PredictResultObject<PredictResult> compare(LearningRequest request) {
        IdentificationEngine identificationEngine = ENGINE_MAP.compute(request.getEngine(), (s, identificationEngine1) -> IdentificationEngine.of(s, DetectionConfiguration.builder()
                .cachePath(learningProperties.getModulePath())
                .build()
        ));

        Compare compare = identificationEngine.get(request.getModuleType(), request.getImplName(), Compare.class);
        return new PredictResultObject<>(compare.calculateSimilar(request.getFile(), request.getFile2()));
    }

    @Override
    public PredictResultObject<PredictResult> recognitions(LearningRequest request) {
        IdentificationEngine identificationEngine = ENGINE_MAP.compute(request.getEngine(), (s, identificationEngine1) -> IdentificationEngine.of(s, DetectionConfiguration.builder()
                .cachePath(learningProperties.getModulePath())
                .build()
        ));

        Recognizer recognizer = identificationEngine.get(request.getModuleType(), request.getImplName(), Recognizer.class);
        return recognizer.predict(request.getFile());
    }

    @Override
    public PredictResultObject<PredictResult> feature(LearningRequest request) {
        IdentificationEngine identificationEngine = ENGINE_MAP.compute(request.getEngine(), (s, identificationEngine1) -> IdentificationEngine.of(s, DetectionConfiguration.builder()
                .cachePath(learningProperties.getModulePath())
                .build()
        ));

        Feature feature = identificationEngine.get(request.getModuleType(), request.getImplName(), Feature.class);
        return feature.predict(request.getFile());
    }

    @Override
    public void destroy() throws Exception {
        for (IdentificationEngine identificationEngine : ENGINE_MAP.values()) {
            IoUtils.closeQuietly(identificationEngine);
        }
    }
}
