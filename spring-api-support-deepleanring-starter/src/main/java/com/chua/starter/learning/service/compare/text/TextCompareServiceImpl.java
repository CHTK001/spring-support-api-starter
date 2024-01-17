package com.chua.starter.learning.service.compare.text;

import com.chua.starter.learning.properties.LearningProperties;
import com.chua.starter.learning.properties.TextCompareProperties;
import com.chua.starter.learning.service.AbstractProxyEngineService;
import org.springframework.stereotype.Service;

/**
 * ocr
 *
 * @author CH
 */
@Service
public class TextCompareServiceImpl extends AbstractProxyEngineService<TextCompareService> {

    public TextCompareServiceImpl(TextCompareProperties configProperties, LearningProperties learningProperties) {
        super(configProperties.setType("compare").setSpi("TextCompare"), learningProperties);
    }

    @Override
    public Class<?> getObjectType() {
        return TextCompareService.class;
    }
}

