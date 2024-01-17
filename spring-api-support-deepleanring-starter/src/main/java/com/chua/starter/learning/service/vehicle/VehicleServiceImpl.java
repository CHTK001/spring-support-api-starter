package com.chua.starter.learning.service.vehicle;

import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.properties.VehicleProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * ocr
 *
 * @author CH
 */
@Service
public class VehicleServiceImpl extends AbstractProxyEngineService<VehicleService> {

    public VehicleServiceImpl(VehicleProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("Vehicle"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return VehicleService.class;
    }
}
