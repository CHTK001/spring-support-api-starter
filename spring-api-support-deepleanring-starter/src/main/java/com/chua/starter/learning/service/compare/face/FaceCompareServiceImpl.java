package com.chua.starter.learning.service.compare.face;

import com.chua.starter.learning.properties.FaceCompareProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * ocr
 *
 * @author CH
 */
@Service
public class FaceCompareServiceImpl extends AbstractProxyEngineService<FaceCompareService> {

    public FaceCompareServiceImpl(FaceCompareProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setType("compare").setSpi("FaceCompare"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return FaceCompareService.class;
    }
}
