package com.chua.starter.collect.engine.executor;

import com.chua.starter.collect.engine.CollectContext;
import com.chua.starter.collect.engine.CollectExpression;
import com.chua.starter.collect.engine.model.CollectNode;
import com.chua.starter.collect.engine.spi.CollectParamMeta;
import com.chua.starter.collect.engine.spi.CollectShapeExecutor;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.engine.spi.CollectShapeMetas;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 开始节点：声明种子 URL。
 *
 * <p>把多行 URL 求值后写成集合变量，下游抓取节点用 {@code ${urlList}} 作为循环集合即可展开成
 * 多条采集分支；同时把首条写入 {@code url}，方便只有一条种子的简单流程直接引用。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Component
public class CollectStartExecutor implements CollectShapeExecutor {

    /**
     * 种子 URL 参数名。
     */
    public static final String URLS = "urls";

    /**
     * 集合变量名参数名。
     */
    public static final String OUTPUT = "outputVariable";

    /**
     * 集合变量默认名。
     */
    public static final String DEFAULT_OUTPUT = "urlList";

    @Override
    public String supportShape() {
        return "start";
    }

    @Override
    public CollectShapeMeta meta() {
        CollectShapeMeta meta = CollectShapeMetas.of(supportShape(), "开始", "来源", "play",
                "声明种子 URL，每行一条，支持 ${} 表达式");
        meta.setOutputVariable("默认写入 urlList，循环集合用 ${urlList} 引用");
        return CollectShapeMetas.with(meta,
                CollectShapeMetas.required(URLS, "种子 URL", "textarea", "每行一个 URL，可用 ${变量} 拼接"),
                CollectParamMeta.of(OUTPUT, "集合变量名", "text"));
    }

    @Override
    public void execute(CollectNode node, CollectContext context, Map<String, Object> variables) {
        List<Object> urls = new ArrayList<>();
        for (String line : node.param(URLS).split("\\r?\\n")) {
            String url = CollectExpression.evalText(line, variables).trim();
            if (!url.isEmpty()) {
                urls.add(url);
            }
        }
        String output = node.param(OUTPUT).isEmpty() ? DEFAULT_OUTPUT : node.param(OUTPUT).trim();
        variables.put(output, urls);
        if (!urls.isEmpty()) {
            variables.put("url", urls.get(0));
        }
    }

    @Override
    public String summary(CollectNode node, Map<String, Object> variables) {
        Object output = variables.get(node.param(OUTPUT).isEmpty() ? DEFAULT_OUTPUT : node.param(OUTPUT).trim());
        int size = output instanceof List ? ((List<?>) output).size() : 0;
        return "种子 " + size + " 条";
    }
}
