package com.chua.starter.collect.engine.executor;

import com.chua.starter.collect.dto.CollectExtractFieldDTO;
import com.chua.starter.collect.engine.CollectContext;
import com.chua.starter.collect.engine.CollectExpression;
import com.chua.starter.collect.engine.CollectFunctions;
import com.chua.starter.collect.engine.model.CollectNode;
import com.chua.starter.collect.engine.spi.CollectParamMeta;
import com.chua.starter.collect.engine.spi.CollectShapeExecutor;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.engine.spi.CollectShapeMetas;
import com.chua.starter.collect.support.CollectDom;
import com.chua.starter.collect.support.CollectJson;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 抽取节点：按字段声明从上游响应里取值。
 *
 * <p>只写变量不落库，落库交给输出节点，这样抽取结果既能被下游表达式引用，
 * 也能在入库前被去重、变量等节点加工。</p>
 *
 * <ul>
 *   <li>单条模式：每个字段写入同名变量，并把整行映射写入出参变量，默认 {@code row}</li>
 *   <li>列表模式：按行选择器切成多行，行集合写入出参变量，默认 {@code rows}，
 *       下游节点用循环参数 {@code ${rows}} 逐行处理</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
public class CollectExtractExecutor implements CollectShapeExecutor {

    /**
     * 抽取来源参数名。
     */
    public static final String SOURCE = "source";

    /**
     * 抽取模式参数名。
     */
    public static final String MODE = "mode";

    /**
     * 行选择器参数名。
     */
    public static final String ROW_EXPRESSION = "rowExpression";

    /**
     * 出参变量名参数名。
     */
    public static final String OUTPUT = "outputVariable";

    /**
     * 单条模式。
     */
    public static final String MODE_SINGLE = "single";

    /**
     * 列表模式。
     */
    public static final String MODE_LIST = "list";

    /**
     * 默认抽取来源。
     */
    public static final String DEFAULT_SOURCE = "respDoc";

    /**
     * 默认出参变量名。
     */
    public static final String DEFAULT_OUTPUT = "rows";

    /**
     * 默认单条出参变量名。
     */
    public static final String DEFAULT_SINGLE_OUTPUT = "row";

    @Override
    public String supportShape() {
        return "extract";
    }

    @Override
    public CollectShapeMeta meta() {
        CollectShapeMeta meta = CollectShapeMetas.of(supportShape(), "抽取", "解析", "filter",
                "按 CSS、正则、JSON 路径从上游响应抽取字段");
        meta.setFieldEditor(true);
        meta.setOutputVariable("单条模式写同名变量与 row，列表模式写 rows 集合");
        CollectParamMeta source = CollectShapeMetas.text(SOURCE, "抽取来源", "text",
                "默认 ${respDoc}；也可填 ${resp.html}、${respJson} 或任意 HTML 文本变量");
        source.setDefaultValue("${" + DEFAULT_SOURCE + "}");
        CollectParamMeta mode = CollectShapeMetas.option(MODE, "抽取模式", MODE_SINGLE,
                "单条取一页里的一个结果，列表把行选择器命中的每一项抽成一行", MODE_SINGLE, MODE_LIST);
        return CollectShapeMetas.with(meta,
                source,
                mode,
                CollectShapeMetas.text(ROW_EXPRESSION, "行选择器", "text",
                        "列表模式用：HTML 填 CSS 选择器如 li.item，JSON 填路径如 data.items"),
                CollectShapeMetas.text(OUTPUT, "出参变量名", "text",
                        "列表模式默认 rows，单条模式默认 row"));
    }

    @Override
    public void execute(CollectNode node, CollectContext context, Map<String, Object> variables) throws Exception {
        List<CollectExtractFieldDTO> fields = node.fields();
        if (fields.isEmpty()) {
            throw new IllegalStateException("抽取节点没有声明字段");
        }
        String sourceExpr = node.param(SOURCE).isEmpty() ? "${" + DEFAULT_SOURCE + "}" : node.param(SOURCE);
        Object source = CollectExpression.eval(sourceExpr, variables);
        if (source == null) {
            throw new IllegalStateException("抽取来源 " + sourceExpr + " 为空，请确认上游抓取节点已产出该变量");
        }
        boolean listMode = MODE_LIST.equals(node.param(MODE));
        String output = node.param(OUTPUT).isEmpty()
                ? (listMode ? DEFAULT_OUTPUT : DEFAULT_SINGLE_OUTPUT) : node.param(OUTPUT);
        if (!listMode) {
            Map<String, Object> row = extractRow(fields, source, variables);
            variables.putAll(row);
            variables.put(output, row);
            return;
        }
        List<Object> items = rowItems(source, node.param(ROW_EXPRESSION));
        List<Map<String, Object>> rows = new ArrayList<>(items.size());
        for (Object item : items) {
            rows.add(extractRow(fields, item, variables));
        }
        variables.put(output, rows);
    }

    @Override
    public String summary(CollectNode node, Map<String, Object> variables) {
        String output = node.param(OUTPUT).isEmpty()
                ? (MODE_LIST.equals(node.param(MODE)) ? DEFAULT_OUTPUT : DEFAULT_SINGLE_OUTPUT) : node.param(OUTPUT);
        Object value = variables.get(output);
        if (value instanceof Collection) {
            return "抽取 " + ((Collection<?>) value).size() + " 行";
        }
        if (value instanceof Map) {
            return "抽取 " + ((Map<?, ?>) value).size() + " 个字段";
        }
        return null;
    }

    /**
     * 抽取一行字段。
     *
     * @param fields    字段声明
     * @param item      本行来源，可为文档、元素、文本或已解析的 JSON 结构
     * @param variables 令牌作用域，供字段默认值表达式使用
     * @return 有序字段映射
     */
    private Map<String, Object> extractRow(List<CollectExtractFieldDTO> fields, Object item,
                                           Map<String, Object> variables) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (CollectExtractFieldDTO field : fields) {
            String name = field.getName() == null ? "" : field.getName().trim();
            if (name.isEmpty()) {
                continue;
            }
            Object value;
            try {
                value = extractField(field, item);
            } catch (Exception ex) {
                log.warn("[collect] 字段 {} 抽取失败: {}", name, ex.getMessage());
                value = null;
            }
            if (CollectFunctions.stringify(value).isEmpty() && field.getDefaultValue() != null
                    && !field.getDefaultValue().isBlank()) {
                value = CollectExpression.eval(field.getDefaultValue(), variables);
            }
            row.put(name, value);
        }
        return row;
    }

    /**
     * 按解析器抽取单个字段。
     *
     * @param field 字段声明
     * @param item  本行来源
     * @return 字段值，未命中返回 null
     */
    private Object extractField(CollectExtractFieldDTO field, Object item) {
        String type = field.getParserType() == null || field.getParserType().isBlank()
                ? "CSS" : field.getParserType().trim().toUpperCase(Locale.ROOT);
        String expression = field.getExpression() == null ? "" : field.getExpression().trim();
        switch (type) {
            case "REGEX":
                return CollectDom.regex(item, expression, field.getGroup());
            case "JSON":
                return CollectJson.path(CollectDom.json(item), expression);
            case "TEXT":
                return CollectDom.text(item).trim();
            case "HTML":
                return CollectDom.html(item);
            default:
                return cssValue(item, expression, field);
        }
    }

    /**
     * CSS 取值。
     *
     * @param item       本行来源
     * @param expression 选择器，留空取本行自身
     * @param field      字段声明
     * @return 字段值，未命中返回 null
     */
    private Object cssValue(Object item, String expression, CollectExtractFieldDTO field) {
        Elements elements = CollectDom.select(item, expression);
        if (elements.isEmpty()) {
            return null;
        }
        Element first = elements.first();
        String way = field.getAttribute() == null || field.getAttribute().isBlank()
                ? "text" : field.getAttribute().trim().toLowerCase(Locale.ROOT);
        switch (way) {
            case "attr":
                String attr = field.getAttrName() == null ? "" : field.getAttrName().trim();
                return first.hasAttr(attr) ? first.attr(attr) : null;
            case "html":
                return first.html();
            case "outer":
                return first.outerHtml();
            default:
                return first.text();
        }
    }

    /**
     * 取行集合。
     *
     * @param source       抽取来源
     * @param rowSelection 行选择器
     * @return 每行的来源对象
     */
    private List<Object> rowItems(Object source, String rowSelection) {
        List<Object> items = new ArrayList<>();
        if (rowSelection == null || rowSelection.isBlank()) {
            if (source instanceof Elements) {
                for (Element element : (Elements) source) {
                    items.add(element);
                }
            } else if (source instanceof Collection) {
                items.addAll((Collection<?>) source);
            } else {
                items.add(source);
            }
            return items;
        }
        String selector = rowSelection.trim();
        if (source instanceof Map || source instanceof List) {
            Object node = CollectJson.path(source, selector);
            if (node instanceof Collection) {
                items.addAll((Collection<?>) node);
            } else if (node != null) {
                items.add(node);
            }
            return items;
        }
        Element root = CollectDom.element(source);
        if (root == null) {
            throw new IllegalStateException("HTML 列表抽取需要 CSS 行选择器，且来源必须是文档或元素");
        }
        for (Element element : root.select(selector)) {
            items.add(element);
        }
        return items;
    }
}
