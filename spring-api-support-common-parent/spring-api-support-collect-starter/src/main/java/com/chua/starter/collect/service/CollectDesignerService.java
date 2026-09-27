package com.chua.starter.collect.service;

import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.vo.CollectRuntimeVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设计器与运行时概览服务。
 *
 * <p>设计器的工具箱、属性面板全部由 {@link CollectShapeMeta} 渲染，第三方以
 * {@code @Spi} 追加节点类型后无需改前端。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface CollectDesignerService {

    /**
     * 全部节点类型元数据。
     *
     * @return 元数据列表
     */
    List<CollectShapeMeta> metas();

    /**
     * 运行时概览：开关、并发规模、计量与节点类型。
     *
     * @return 概览出参
     */
    CollectRuntimeVO runtime();

    /**
     * 预览定时表达式接下来几次的触发时间。
     *
     * @param cron  定时表达式，六位 Quartz 风格
     * @param times 预览次数，最多 10 次
     * @return 触发时间列表
     */
    List<LocalDateTime> cronPreview(String cron, int times);
}
