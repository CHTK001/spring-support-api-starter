package com.chua.starter.learning.service;

import com.chua.common.support.ml.PredictResult;
import com.chua.common.support.ml.PredictResultObject;
import com.chua.starter.learning.pojo.LearningRequest;

import java.util.List;

/**
 * 学习配置服务
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/24
 */
public interface LearningConfigService {
    /**
     * 检测
     *
     * @param request 文件
     * @return {@link List}<{@link PredictResult}>
     */
    PredictResultObject<PredictResult> detect(LearningRequest request);
    /**
     * 检测
     *
     * @param request 文件
     * @return {@link List}<{@link PredictResult}>
     */
    PredictResultObject<PredictResult> compare(LearningRequest request);
    /**
     * 检测
     *
     * @param request 文件
     * @return {@link List}<{@link PredictResult}>
     */
    PredictResultObject<PredictResult> recognitions(LearningRequest request);
    /**
     * 检测
     *
     * @param request 文件
     * @return {@link List}<{@link PredictResult}>
     */
    PredictResultObject<PredictResult>  feature(LearningRequest request);

}
