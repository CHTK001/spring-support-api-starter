package com.chua.starter.learning.service.animal;

import com.chua.starter.learning.properties.CocoProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * Animal
 *
 * @author CH
 */
@Service
public class AnimalServiceImpl extends AbstractProxyEngineService<AnimalService> {

    public AnimalServiceImpl(CocoProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("Animal"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return AnimalService.class;
    }
}
