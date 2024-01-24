package com.chua.starter.learning.service;

import java.util.List;

/**
 * 学习配置服务
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/24
 */
public interface LearningConfigService {

    /**
     * 支持那些引擎
     *
     * @return {@link List}<{@link String}>
     */
    List<String> engine();
}
