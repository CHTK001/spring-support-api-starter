package com.chua.starter.learning.service.compare.image;

import com.chua.starter.learning.properties.ImageCompareProperties;
import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * ocr
 *
 * @author CH
 */
@Service
public class ImageCompareServiceImpl extends AbstractProxyEngineService<ImageCompareService> {

    public ImageCompareServiceImpl(ImageCompareProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setType("compare").setSpi("ImageCompare"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return ImageCompareService.class;
    }
}
