package com.chua.starter.learning.pojo;

import com.chua.common.support.ml.type.ModuleType;
import lombok.Data;

/**
 * 请求参数
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/25
 */
@Data
public class LearningRequest {


    /**
     * 引擎
     */
    private String engine;

    /**
     * 文件
     */
    private Object file;

    /**
     * 文件2
     */
    private Object file2;

    /**
     * 模块类型
     */
    private ModuleType moduleType;


    /**
     * 实现方式
     */
    private String implName;

}
