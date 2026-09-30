package com.chua.starter.collect.engine.executor;

import com.chua.starter.collect.engine.CollectContext;
import com.chua.starter.collect.engine.CollectExpression;
import com.chua.starter.collect.engine.model.CollectNode;
import com.chua.starter.collect.engine.spi.CollectShapeExecutor;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.engine.spi.CollectShapeMetas;
import com.chua.starter.collect.support.CollectJdbcSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQL 节点：在流程里读写数据库。
 *
 * <p>语句整段支持 {@code ${}} 插值，一次只执行一条。SELECT 与 WITH/SHOW 开头的语句按查询处理，
 * 结果集合写入出参变量供下游循环节点使用；其余语句按更新处理，受影响行数写入
 * {@code _affected} 供摘要显示。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectSqlExecutor implements CollectShapeExecutor {

    /**
     * 数据源引用参数名。
     */
    public static final String DATASOURCE = "datasource";

    /**
     * 语句参数名。
     */
    public static final String STATEMENT = "statement";

    /**
     * 出参变量名参数名。
     */
    public static final String OUTPUT = "outputVariable";

    /**
     * 查询结果默认变量名。
     */
    public static final String DEFAULT_OUTPUT = "rows";

    /**
     * 受影响行数暂存变量名。
     */
    private static final String VAR_AFFECTED = "_affected";

    /**
     * JDBC 通道。
     */
    private final CollectJdbcSupport jdbc;

    @Override
    public String supportShape() {
        return "sql";
    }

    @Override
    public CollectShapeMeta meta() {
        CollectShapeMeta meta = CollectShapeMetas.of(supportShape(), "SQL", "输出", "database",
                "读写数据库，查询结果可作为下游节点的循环集合");
        meta.setOutputVariable("SELECT 结果写入出参变量，默认 rows");
        return CollectShapeMetas.with(meta,
                CollectShapeMetas.required(STATEMENT, "SQL 语句", "textarea",
                        "整段支持 ${}，例：select * from t where dt='${dateNow(\"yyyy-MM-dd\")}'；一次一条语句"),
                CollectShapeMetas.datasource(DATASOURCE, "数据源", CollectJdbcSupport.APP_DATASOURCE,
                        "默认应用主库；下拉里是 collect_datasource 已启用的数据源"),
                CollectShapeMetas.text(OUTPUT, "结果变量名", "text", "查询结果的行集合变量名，默认 rows"));
    }

    @Override
    public void execute(CollectNode node, CollectContext context, Map<String, Object> variables) throws Exception {
        if (node.param(STATEMENT).isEmpty()) {
            throw new IllegalStateException("SQL 节点没有填写语句");
        }
        String sql = CollectExpression.evalText(node.param(STATEMENT), variables);
        String datasource = node.param(DATASOURCE);
        if (isQuery(sql)) {
            String output = node.param(OUTPUT).isEmpty() ? DEFAULT_OUTPUT : node.param(OUTPUT);
            List<Map<String, Object>> rows = jdbc.query(datasource, sql);
            variables.put(output, rows);
            return;
        }
        // 非查询语句：先过写开关，再执行。
        // SQL 节点整段支持 ${} 插值且不做语句白名单，插值内容可能来自被抓取的页面 ——
        // 写语句（含 DDL/DELETE）的破坏面远大于查询，所以给它一个部署侧开关，
        // 需要写库时走 database 节点（PreparedStatement 绑定、无拼接、无注入面）。
        if (!context.getProperties().isSqlAllowWrite()) {
            throw new IllegalStateException(
                    "SQL 节点的写语句已被配置禁用（plugin.collect.sql-allow-write=false）。"
                            + "需要写库请改用 database 节点（字段映射、参数绑定），或由运维放开该配置。");
        }
        // 审计：写语句与执行脚本都留下可追溯的一条，注明流程/任务/节点与语句前 120 字
        log.warn("[采集审计] SQL 写语句 flowId={} taskNo={} node={} 语句: {}",
                context.getFlowId(), context.getTaskNo(), node.name(), abbreviate(sql));
        variables.put(VAR_AFFECTED, (long) jdbc.update(datasource, sql));
    }

    /**
     * 截断语句用于审计日志，避免整条 DDL/DML 刷屏。
     *
     * @param sql 已求值语句
     * @return 最多 120 字的单行摘要
     */
    private String abbreviate(String sql) {
        String flat = sql == null ? "" : sql.replaceAll("\\s+", " ").trim();
        return flat.length() > 120 ? flat.substring(0, 120) + "…" : flat;
    }

    @Override
    public String summary(CollectNode node, Map<String, Object> variables) {
        Object affected = variables.get(VAR_AFFECTED);
        if (affected != null) {
            return "影响 " + affected + " 行";
        }
        String output = node.param(OUTPUT).isEmpty() ? DEFAULT_OUTPUT : node.param(OUTPUT);
        Object rows = variables.get(output);
        if (rows instanceof Collection) {
            return "返回 " + ((Collection<?>) rows).size() + " 行";
        }
        return null;
    }

    /**
     * 是否查询语句。
     *
     * @param sql 已求值语句
     * @return 以 select/with/show 开头返回 true
     */
    private boolean isQuery(String sql) {
        String head = sql == null ? "" : sql.trim().toLowerCase(Locale.ROOT);
        return head.startsWith("select") || head.startsWith("with") || head.startsWith("show");
    }
}
