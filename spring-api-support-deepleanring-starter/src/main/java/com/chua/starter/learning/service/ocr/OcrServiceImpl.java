package com.chua.starter.learning.service.ocr;

import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.properties.OcrProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * ocr
 *
 * @author CH
 */
@Service
public class OcrServiceImpl extends AbstractProxyEngineService<OcrService> {

    public OcrServiceImpl(OcrProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("ocr"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return OcrService.class;
    }
}
