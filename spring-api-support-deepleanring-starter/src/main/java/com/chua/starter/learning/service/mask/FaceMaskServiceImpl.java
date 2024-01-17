package com.chua.starter.learning.service.mask;

import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.properties.MaskProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * 口罩检测
 *
 * @author CH
 */
@Service
public class FaceMaskServiceImpl extends AbstractProxyEngineService<FaceMaskService> {

    public FaceMaskServiceImpl(MaskProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("FaceMask"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return FaceMaskService.class;
    }
}
