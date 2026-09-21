package com.chua.starter.collect.engine.executor;

import com.chua.starter.collect.dto.CollectParamDTO;
import com.chua.starter.collect.engine.CollectContext;
import com.chua.starter.collect.engine.CollectExpression;
import com.chua.starter.collect.engine.model.CollectNode;
import com.chua.starter.collect.engine.spi.CollectParamMeta;
import com.chua.starter.collect.engine.spi.CollectShapeExecutor;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.engine.spi.CollectShapeMetas;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 变量节点：给变量赋值。
 *
 * <p>作用域二选一：当前分支（默认，只有本令牌及其下游可见）或全局（整次执行共享，
 * 用于累计计数、跨分支传值）。值按表达式求值，因此 {@code ${a} + 1}、{@code md5(x)} 都可以。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Component
public class CollectVariableExecutor implements CollectShapeExecutor {

    /**
     * 赋值对参数组名。
     */
    public static final String SET = "set";

    /**
     * 作用域参数名。
     */
    public static final String SCOPE = "scope";

    /**
     * 全局作用域取值。
     */
    public static final String SCOPE_GLOBAL = "global";

    @Override
    public String supportShape() {
        return "variable";
    }

    @Override
    public CollectShapeMeta meta() {
        CollectShapeMeta meta = CollectShapeMetas.of(supportShape(), "变量", "控制", "sigma",
                "按表达式给变量赋值");
        CollectParamMeta pairs = CollectParamMeta.of(SET + ":变量名", "赋值（变量名 = 表达式）", "pairs");
        pairs.setTip("左侧填变量名，右侧填值表达式；同一行会存成参数 set:变量名");
        return CollectShapeMetas.with(meta, pairs,
                CollectParamMeta.select(SCOPE, "作用域", "branch", SCOPE_GLOBAL));
    }

    @Override
    public void execute(CollectNode node, CollectContext context, Map<String, Object> variables) {
        List<CollectParamDTO> pairs = node.pairsOf(SET);
        if (pairs.isEmpty()) {
            throw new IllegalStateException("变量节点没有填写任何赋值");
        }
        boolean global = SCOPE_GLOBAL.equalsIgnoreCase(node.param(SCOPE));
        for (CollectParamDTO pair : pairs) {
            String name = pair.getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            Object value = CollectExpression.eval(pair.getValue(), variables);
            if (global) {
                context.putRootVariable(name, value);
            } else {
                variables.put(name.trim(), value);
            }
        }
    }

    @Override
    public String summary(CollectNode node, Map<String, Object> variables) {
        List<CollectParamDTO> pairs = node.pairsOf(SET);
        StringBuilder builder = new StringBuilder();
        for (CollectParamDTO pair : pairs) {
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(pair.getName()).append('=').append(pair.getValue());
        }
        return builder.length() == 0 ? null : builder.toString();
    }
}
