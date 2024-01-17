package com.chua.starter.learning.service;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.engine.Engine;
import com.chua.common.support.lang.function.Compare;
import com.chua.common.support.lang.loader.BaseLazyLoader;
import com.chua.common.support.lang.loader.Loader;
import com.chua.starter.learning.model.ConfigProperties;
import com.chua.starter.learning.properties.LearningProperties;

import java.util.List;

/**
 * 比对引擎实现
 * @author CH
 */
@Spi("compare")
public class SpiCompareEngineService extends SpiEngineService{
    final Loader<Compare> lazyLoader = new BaseLazyLoader<Compare>() {
        @Override
        public Compare init() {
            return Engine.auto(configProperties.getConfig())
                    .get(configProperties.getSpi(), Compare.class);
        }
    };

    public SpiCompareEngineService(ConfigProperties configProperties, LearningProperties learningProperties) {
        super(configProperties, learningProperties);
    }


    @Override
    public List<PredictResult> recognize(Object... input) {
        try {
            return lazyLoader.get().calculateSimilar(input[0], input[1]);
        } catch (Exception e) {
            throw new RuntimeException("引擎不存在");
        }
    }
}
