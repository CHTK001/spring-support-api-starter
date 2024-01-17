package com.chua.starter.learning.service;

import com.chua.common.support.constant.PredictResult;

import java.util.List;

/**
 * ocr
 *
 * @author CH
 */
public interface LearningService {
    /**
     * 識別
     *
     * @param input 輸入
     * @return 輸出
     */
    List<PredictResult> recognize(Object... input);
}
