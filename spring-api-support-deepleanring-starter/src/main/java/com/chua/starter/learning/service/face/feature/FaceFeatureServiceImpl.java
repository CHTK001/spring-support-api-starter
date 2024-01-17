package com.chua.starter.learning.service.face.feature;

import com.chua.starter.learning.properties.FaceFeatureProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * 人脸特征值服务
 *
 * @author CH
 */
@Service
public class FaceFeatureServiceImpl extends AbstractProxyEngineService<FaceFeatureService> {

    public FaceFeatureServiceImpl(FaceFeatureProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("FaceFeature"), learningProperties);
    }


    @Override
    public Class<?> getObjectType() {
        return FaceFeatureService.class;
    }

}
