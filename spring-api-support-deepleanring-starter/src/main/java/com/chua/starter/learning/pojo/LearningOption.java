package com.chua.starter.learning.pojo;

import com.chua.common.support.spi.Option;
import lombok.Data;

import java.util.List;

/**
 * 基础配置
 *
 * @author CH
 */
@Data
public class LearningOption {

    private List<Option<String>> model;
}