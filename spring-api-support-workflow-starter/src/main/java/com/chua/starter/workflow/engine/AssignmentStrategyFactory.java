package com.chua.starter.workflow.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分配策略工厂 — 注册 & 查找 AssignmentStrategy
 *
 * @author CH
 * @since 2026/5/28
 */
public class AssignmentStrategyFactory {

    private final Map<String, AssignmentStrategy> strategyMap = new ConcurrentHashMap<>();

    public AssignmentStrategyFactory(List<AssignmentStrategy> strategies) {
        if (strategies != null) {
            for (AssignmentStrategy strategy : strategies) {
                strategyMap.put(strategy.type().toUpperCase(), strategy);
            }
        }
    }

    /**
     * 获取指定类型的分配策略
     */
    public AssignmentStrategy getStrategy(String type) {
        AssignmentStrategy strategy = strategyMap.get(type.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("未知的分配策略类型: " + type);
        }
        return strategy;
    }

    /**
     * 注册自定义策略
     */
    public void register(AssignmentStrategy strategy) {
        strategyMap.put(strategy.type().toUpperCase(), strategy);
    }

    /**
     * 获取所有已注册策略类型
     */
    public List<String> getSupportedTypes() {
        return Collections.unmodifiableList(
                strategyMap.keySet().stream().sorted().toList());
    }
}
