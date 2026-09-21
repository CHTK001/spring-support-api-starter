package com.chua.starter.collect.engine.spi;

import com.chua.starter.collect.engine.CollectContext;
import com.chua.starter.collect.engine.model.CollectNode;

import java.util.List;
import java.util.Map;

/**
 * 节点执行器扩展点。
 *
 * <p>实现类以 Spring Bean 或 {@code @Spi} 方式提供，注册后即出现在设计器工具箱，
 * 新增节点类型无需修改引擎与前端。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface CollectShapeExecutor {

    /**
     * 该执行器支持的节点类型标识。
     *
     * @return 节点类型，全流程唯一
     */
    String supportShape();

    /**
     * 节点元数据，驱动设计器工具箱与属性面板。
     *
     * @return 节点元数据
     */
    CollectShapeMeta meta();

    /**
     * 执行节点。
     *
     * <p>出参通过写回 {@code variables} 传递下游，抓取失败应抛出异常由引擎按边的异常流向处理。</p>
     *
     * @param node      当前节点
     * @param context   执行上下文
     * @param variables 当前变量作用域
     * @throws Exception 执行失败
     */
    void execute(CollectNode node, CollectContext context, Map<String, Object> variables) throws Exception;

    /**
     * 节点执行摘要，写入节点日志的 message 列。
     *
     * @param node      当前节点
     * @param variables 当前变量作用域
     * @return 摘要文本，返回空则使用默认文案
     */
    default String summary(CollectNode node, Map<String, Object> variables) {
        return null;
    }

    /**
     * 循环体候选集合参数名，供设计器提示哪些参数可填集合表达式。
     *
     * @return 参数名列表
     */
    default List<String> loopParams() {
        return List.of();
    }
}
