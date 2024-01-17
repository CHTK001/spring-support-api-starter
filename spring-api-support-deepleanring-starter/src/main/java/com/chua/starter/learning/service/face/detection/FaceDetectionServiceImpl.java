package com.chua.starter.learning.service.face.detection;

import com.chua.starter.learning.properties.FaceDetectionProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * FaceDetection
 *
 * @author CH
 */
@Service
public class FaceDetectionServiceImpl extends AbstractProxyEngineService<FaceDetectionService> {



    public FaceDetectionServiceImpl(FaceDetectionProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("FaceDetection"), learningProperties);
    }


    @Override
    public Class<?> getObjectType() {
        return FaceDetectionService.class;
    }


}
