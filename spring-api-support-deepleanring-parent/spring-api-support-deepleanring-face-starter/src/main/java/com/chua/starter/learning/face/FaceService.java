package com.chua.starter.learning.face;


import com.chua.common.support.ml.PredictResult;
import com.chua.starter.learning.service.LearningConfigService;

import java.util.List;

/**
 * 人脸检测服务
 * @author CH
 */
public interface FaceService extends LearningConfigService {

    /**
     * 检测
     *
     * @param file 文件
     * @return {@link List}<{@link PredictResult}>
     */
    List<PredictResult> detect(Object file);


}
