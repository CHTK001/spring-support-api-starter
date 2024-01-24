package com.chua.starter.learning.face;


import com.chua.common.support.ml.PredictResult;
import com.chua.starter.learning.properties.LearningProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 人脸检测服务
 * @author CH
 */
@Service
public class FaceServiceImpl implements FaceService{


    @Resource
    private LearningProperties learningProperties;

    @Override
    public List<PredictResult> detect(Object file) {
        return null;
    }

    @Override
    public List<String> engine() {
       return null;
    }
}
