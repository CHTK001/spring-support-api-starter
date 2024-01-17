package com.chua.starter.learning.service.coco;

import com.chua.starter.learning.properties.CocoProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * coco
 *
 * @author CH
 */
@Service
public class CocoServiceImpl extends AbstractProxyEngineService<CocoService> {

    public CocoServiceImpl(CocoProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("coco"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return CocoService.class;
    }
}
