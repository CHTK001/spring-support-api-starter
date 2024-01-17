package com.chua.starter.learning.service.image.feature;

import com.chua.starter.learning.properties.ImageFeatureProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * 图片特征值
 *
 * @author CH
 */
@Service
public class ImageFeatureServiceImpl extends AbstractProxyEngineService<ImageFeatureService> {

    public ImageFeatureServiceImpl(ImageFeatureProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setSpi("ImageFeature"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return ImageFeatureService.class;
    }
}
