package com.chua.starter.collect.engine.executor;

import com.chua.starter.collect.engine.CollectContext;
import com.chua.starter.collect.engine.CollectExpression;
import com.chua.starter.collect.engine.model.CollectNode;
import com.chua.starter.collect.engine.spi.CollectParamMeta;
import com.chua.starter.collect.engine.spi.CollectShapeExecutor;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.engine.spi.CollectShapeMetas;
import com.chua.starter.collect.support.CollectJson;
import com.chua.starter.collect.support.CollectMiddlewareSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据录入节点：手工录行，作为流程的行数据源头。
 *
 * <p>录入文本两种形态，解析口径写死在 {@link #parseRows}：</p>
 * <ul>
 *   <li>CSV / TSV：首行表头、其后每行一条，单元格支持双引号包裹（内嵌分隔符与
 *       转义双引号都按 CSV 规则走）；</li>
 *   <li>JSON 数组：{@code [{"列": 值}, ...]}，元素是标量时收进 {@code value} 列。</li>
 * </ul>
 *
 * <p>文本先过 {@link CollectExpression#evalText}，所以和开始节点一样支持 {@code ${}} 拼接。
 * 出参是 {@code List<Map>}（默认变量名 {@code rows}），下游数据库 / 输出节点按集合整批消费 ——
 * 与抽取节点 list 模式同一套语义，不需要引擎做任何改动。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Component
public class CollectDataEntryExecutor implements CollectShapeExecutor {

    /**
     * 录入数据参数名。
     */
    public static final String ROWS = "rows";

    /**
     * 出参集合变量名参数名。
     */
    public static final String OUTPUT = "outputVariable";

    /**
     * 「建表」按钮的目标数据源参数名。
     */
    public static final String DATASOURCE = "datasource";

    /**
     * 「建表」按钮的目标表参数名。
     */
    public static final String TABLE = "table";

    /**
     * 出参集合默认名。
     */
    public static final String DEFAULT_OUTPUT = "rows";

    @Override
    public String supportShape() {
        return "entry";
    }

    @Override
    public CollectShapeMeta meta() {
        CollectShapeMeta meta = CollectShapeMetas.of(supportShape(), "数据录入", "来源", "edit-pen",
                "手工录入数据行作为流程的行数据源头，下游接数据库 / 输出等节点；可按录入列一键给目标数据源建表");
        meta.setOutputVariable("默认 data，旧流程留空时按 ${rows} 兜底");
        return CollectShapeMetas.with(meta,
                CollectShapeMetas.required(ROWS, "录入数据", "textarea",
                        "第一行表头（逗号分隔列名），之后每行一条；也支持 JSON 数组 [{\"列\": 值}]；支持 ${} 表达式"),
                CollectShapeMetas.outputVariable("录入结果写入的变量名，默认 data"),
                CollectShapeMetas.datasource(DATASOURCE, "建表数据源", "",
                        "「建表」按钮的目标数据源，仅建表用，不影响本节点出参"),
                CollectShapeMetas.table(TABLE, "建表目标表", "",
                        "配合「建表」按钮把录入列落成实际表；下游数据库节点选同一数据源与表即可写入"));
    }

    @Override
    public void execute(CollectNode node, CollectContext context, Map<String, Object> variables) {
        String raw = CollectExpression.evalText(node.param(ROWS), variables);
        List<Map<String, Object>> rows = parseRows(raw);
        variables.put(outputOf(node), rows);
    }

    @Override
    public String summary(CollectNode node, Map<String, Object> variables) {
        Object value = variables.get(outputOf(node));
        int size = value instanceof List ? ((List<?>) value).size() : 0;
        return "录入 " + size + " 行";
    }

    @Override
    public String defaultOutput(CollectNode node) {
        return DEFAULT_OUTPUT;
    }

    /**
     * 解析出参变量名，空值回落默认名。
     *
     * @param node 节点
     * @return 出参变量名
     */
    private String outputOf(CollectNode node) {
        String output = node.param(OUTPUT).trim();
        return output.isEmpty() ? DEFAULT_OUTPUT : output;
    }

    /**
     * 解析录入文本：JSON 数组优先，其余按 CSV / TSV（首行表头）。
     *
     * @param raw 录入文本
     * @return 行列表
     * @throws IllegalArgumentException 结构不合法（非数组 JSON、缺表头、列数超出表头等）
     */
    private List<Map<String, Object>> parseRows(String raw) {
        String text = raw == null ? "" : raw.trim();
        List<Map<String, Object>> rows = new ArrayList<>();
        if (text.isEmpty()) {
            return rows;
        }
        if (text.startsWith("[")) {
            return parseJsonRows(text);
        }
        // 分隔符看首行：有制表符按 TSV，否则按 CSV
        String firstLine = text.lines().filter(line -> !line.isBlank()).findFirst().orElse("");
        char delimiter = firstLine.indexOf('\t') >= 0 ? '\t' : ',';
        List<List<String>> table = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            if (!line.isBlank()) {
                table.add(parseCsvLine(line, delimiter));
            }
        }
        if (table.size() <= 1) {
            return rows;
        }
        List<String> headers = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < table.get(0).size(); i++) {
            String header = table.get(0).get(i).trim();
            if (header.isEmpty()) {
                throw new IllegalArgumentException("录入数据第 1 行（表头）第 " + (i + 1) + " 列列名为空");
            }
            if (!seen.add(header)) {
                throw new IllegalArgumentException("录入数据表头列名重复: " + header);
            }
            headers.add(header);
        }
        for (int r = 1; r < table.size(); r++) {
            List<String> cells = table.get(r);
            if (cells.size() > headers.size()) {
                throw new IllegalArgumentException("录入数据第 " + (r + 1) + " 行有 " + cells.size()
                        + " 列，表头只有 " + headers.size() + " 列（检查分隔符或多余逗号）");
            }
            Map<String, Object> row = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                row.put(headers.get(c), c < cells.size() ? coerce(cells.get(c)) : null);
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 解析 JSON 数组录入。
     *
     * @param text JSON 数组文本
     * @return 行列表
     */
    private List<Map<String, Object>> parseJsonRows(String text) {
        Object parsed = CollectJson.parseAny(text);
        if (!(parsed instanceof List<?> list)) {
            throw new IllegalArgumentException("录入数据以 [ 开头但不是合法 JSON 数组");
        }
        List<Map<String, Object>> rows = new ArrayList<>(list.size());
        for (Object item : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            if (item instanceof Map<?, ?> map) {
                map.forEach((cellKey, cellValue) -> row.put(String.valueOf(cellKey), cellValue));
            } else {
                row.put(CollectMiddlewareSupport.VALUE_COLUMN, item);
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 解析一行 CSV / TSV，支持双引号包裹与双引号转义。
     *
     * @param line      行文本
     * @param delimiter 分隔符
     * @return 单元格列表
     */
    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                quoted = true;
            } else if (ch == delimiter) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    /**
     * 单元格按值域收型：空 → null、布尔、整数、小数，其余保留文本。
     *
     * <p>前导零的数字（如 {@code 007}）刻意不收成数字 —— 那是编号语义，收了会丢位。</p>
     *
     * @param cell 单元格原文
     * @return 收型后的值
     */
    private Object coerce(String cell) {
        String value = cell == null ? "" : cell.trim();
        if (value.isEmpty()) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value.toLowerCase(java.util.Locale.ROOT));
        }
        if (value.matches("-?(0|[1-9]\\d*)")) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException overflow) {
                return value;
            }
        }
        if (value.matches("-?(0|[1-9]\\d*)\\.\\d+")) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException overflow) {
                return value;
            }
        }
        return value;
    }
}