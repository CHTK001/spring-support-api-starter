package com.chua.starter.collect.engine;

import com.chua.starter.collect.dto.CollectDslDTO;
import com.chua.starter.collect.dto.CollectParamDTO;
import com.chua.starter.collect.engine.model.CollectEdge;
import com.chua.starter.collect.engine.model.CollectGraph;
import com.chua.starter.collect.engine.model.CollectNode;
import com.chua.starter.collect.engine.spi.CollectShapeExecutor;
import com.chua.starter.collect.properties.CollectProperties;
import com.chua.starter.collect.support.CollectTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 采集流程执行引擎。
 *
 * <p>采用令牌语义：一次节点执行是一个令牌，令牌跑完后按出边派发下游令牌，因此同一个节点
 * 可以被不同令牌多次执行，汇聚不需要到达计数。</p>
 * <ul>
 *   <li>入口为 start 节点，没有 start 时取入度为 0 的节点。</li>
 *   <li>节点的 {@code loopCount} 参数决定令牌展开几个迭代，可以是次数也可以是集合；每个迭代
 *       把自己的作用域沿下游继续传播，这就是"列表页抓 N 条、每条再抓详情"的天然表达。
 *       集合为空时本节点及其下游都不执行。</li>
 *   <li>{@code loopParallel} 打开后各迭代各自入池，由线程池大小限流；关闭时按顺序执行。</li>
 *   <li>连线按异常流向与 {@code condition} 放行；环路在建模期被拒绝，令牌不会无限增殖。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectEngine {

    /**
     * 循环次数参数名。
     */
    public static final String PARAM_LOOP_COUNT = "loopCount";

    /**
     * 循环下标变量名参数。
     */
    public static final String PARAM_LOOP_INDEX = "loopVariableName";

    /**
     * 循环元素变量名参数。
     */
    public static final String PARAM_LOOP_ITEM_VARIABLE = "loopItemVariableName";

    /**
     * 循环并行开关参数名。
     */
    public static final String PARAM_LOOP_PARALLEL = "loopParallel";

    /**
     * 上游异常文本的变量名。
     */
    public static final String VAR_EXCEPTION = "ex";

    /**
     * 令牌终止变量名，执行器写入 true 后本令牌不再向下游派发。
     */
    public static final String VAR_STOP = "_stop";

    /**
     * 引擎按流程身份注入的变量，跨子流程边界时不继承。
     */
    public static final Set<String> IDENTITY_VARIABLES = Set.of("flowId", "taskId", "taskNo");

    /**
     * 执行器注册表。
     */
    private final CollectExecutorRegistry registry;

    /**
     * 平台配置。
     */
    private final CollectProperties properties;

    /**
     * 运行中的任务上下文。
     */
    private final Map<Long, CollectContext> running = new ConcurrentHashMap<>();

    /**
     * 执行流程图直到结束、停止或超时。
     *
     * @param context 执行上下文
     * @param dsl     流程图 DSL
     * @return 执行结果
     */
    public CollectRunResult execute(CollectContext context, CollectDslDTO dsl) {
        long begin = System.currentTimeMillis();
        CollectGraph graph;
        try {
            graph = CollectGraph.build(dsl, registry.shapes());
        } catch (CollectGraph.CollectGraphException ex) {
            return finish(context, begin, CollectTaskStatus.FAILURE, ex.getMessage());
        }
        ExecutorService pool = createPool(context, dsl);
        context.attachPool(pool);
        running.put(context.getTaskId(), context);
        try {
            seedVariables(context, dsl);
            for (CollectNode node : graph.entryNodes()) {
                schedule(context, node, new LinkedHashMap<>(context.getRootVariables()));
            }
            return settle(context, begin, context.awaitFinish(properties.getTaskTimeoutMs()),
                    properties.getTaskTimeoutMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            context.cancel();
            return finish(context, begin, CollectTaskStatus.FAILURE, "执行被中断: " + ex.getMessage());
        } finally {
            running.remove(context.getTaskId());
            pool.shutdownNow();
        }
    }

    /**
     * 在父任务里同步执行一段子流程。
     *
     * <p>子流程不单独占任务行，因此不写 {@code running}：停止、并发与调度计数都归根任务。
     * 线程池必须单独建——父令牌正阻塞等子流程，若复用父池，池里线程全在等孩子、
     * 孩子的令牌却排在自己后面，直接锁死。</p>
     *
     * @param parent    父上下文
     * @param flowId    子流程ID
     * @param dsl       子流程图
     * @param seed      调用方传入的变量，覆盖子流程自带的同名变量
     * @param timeoutMs 子流程最长执行毫秒，非正数取单任务超时
     * @return 子流程执行结果，计量为本层小计
     * @throws InterruptedException 等待被中断
     */
    public CollectRunResult executeNested(CollectContext parent, Integer flowId, CollectDslDTO dsl,
                                          Map<String, Object> seed, long timeoutMs) throws InterruptedException {
        long begin = System.currentTimeMillis();
        CollectContext context = CollectContext.nested(parent, flowId);
        CollectGraph graph;
        try {
            graph = CollectGraph.build(dsl, registry.shapes());
        } catch (CollectGraph.CollectGraphException ex) {
            return finish(context, begin, CollectTaskStatus.FAILURE, "子流程图不合法: " + ex.getMessage());
        }
        ExecutorService pool = createPool(context, dsl);
        context.attachPool(pool);
        try {
            seedVariables(context, dsl);
            applySeed(context, seed);
            for (CollectNode node : graph.entryNodes()) {
                schedule(context, node, new LinkedHashMap<>(context.getRootVariables()));
            }
            long limit = timeoutMs > 0 ? timeoutMs : properties.getTaskTimeoutMs();
            return settle(context, begin, context.awaitFinish(limit), limit);
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * 等本次执行落定并汇总状态。
     *
     * @param context  执行上下文
     * @param begin    开始毫秒
     * @param settled  是否在超时前跑完
     * @param limit    本次实际生效的超时毫秒，用于超时说明
     * @return 执行结果
     * @throws InterruptedException 等待被中断
     */
    private CollectRunResult settle(CollectContext context, long begin, boolean settled, long limit)
            throws InterruptedException {
        String status = CollectTaskStatus.SUCCESS;
        String error = null;
        if (!settled) {
            status = CollectTaskStatus.FAILURE;
            error = "执行超时 " + limit + "ms，已强制结束";
            context.cancel();
        } else if (!context.isRunning()) {
            status = CollectTaskStatus.STOPPED;
            error = "任务被手动停止";
        } else if (context.getFailureMessage() != null) {
            status = CollectTaskStatus.FAILURE;
            error = context.getFailureMessage();
        }
        return finish(context, begin, status, error);
    }

    /**
     * 把调用方的实参盖在子流程自带变量之上。
     *
     * <p>引擎内部键与 {@code flowId}/{@code taskId}/{@code taskNo} 不覆盖：前两个是引擎记账用的，
     * 后三个必须指向子流程自己，否则子流程里的脚本会拿到父流程的身份。</p>
     *
     * @param context 子流程上下文
     * @param seed    调用方传入的变量
     */
    private void applySeed(CollectContext context, Map<String, Object> seed) {
        if (seed == null || seed.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : seed.entrySet()) {
            String name = entry.getKey();
            if (name == null || name.isBlank() || name.startsWith("_")
                    || VAR_EXCEPTION.equals(name) || IDENTITY_VARIABLES.contains(name)) {
                continue;
            }
            context.putRootVariable(name.trim(), entry.getValue());
        }
    }

    /**
     * 创建本次执行独占的线程池。
     *
     * @param context 执行上下文
     * @param dsl     流程图 DSL
     * @return 线程池
     */
    private ExecutorService createPool(CollectContext context, CollectDslDTO dsl) {
        int threads = dsl.getThreadCount() == null || dsl.getThreadCount() <= 0
                ? properties.getDefaultThreadCount() : dsl.getThreadCount();
        int size = Math.max(1, Math.min(threads, 64));
        return Executors.newFixedThreadPool(size, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("collect-" + context.getTaskNo() + "-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 注入全局变量与内置变量。
     *
     * @param context 执行上下文
     * @param dsl     流程图 DSL
     */
    private void seedVariables(CollectContext context, CollectDslDTO dsl) {
        Map<String, Object> root = context.getRootVariables();
        context.putRootVariable("taskNo", context.getTaskNo());
        context.putRootVariable("taskId", context.getTaskId());
        context.putRootVariable("flowId", context.getFlowId());
        if (dsl.getVariables() != null) {
            for (CollectParamDTO param : dsl.getVariables()) {
                context.putRootVariable(param.getName(), CollectExpression.eval(param.getValue(), root));
            }
        }
        context.getGlobalHeaders().clear();
        context.getGlobalCookies().clear();
        if (dsl.getHeaders() != null) {
            context.getGlobalHeaders().addAll(dsl.getHeaders());
        }
        if (dsl.getCookies() != null) {
            context.getGlobalCookies().addAll(dsl.getCookies());
        }
    }

    /**
     * 把一个令牌派发为线程池上的一次异步执行。
     *
     * @param context 执行上下文
     * @param node    节点
     * @param vars    令牌作用域
     */
    private void schedule(CollectContext context, CollectNode node, Map<String, Object> vars) {
        if (!context.isRunning()) {
            return;
        }
        context.beginWork();
        context.getPool().submit(() -> {
            try {
                runToken(context, node, vars);
            } catch (Exception ex) {
                log.warn("[collect] 令牌执行异常 task={} node={}", context.getTaskNo(), node.id(), ex);
            } finally {
                context.endWork();
            }
        });
    }

    /**
     * 展开一个节点的迭代，每个迭代是一个独立令牌。
     *
     * @param context 执行上下文
     * @param node    节点
     * @param vars    令牌入参作用域
     */
    private void runToken(CollectContext context, CollectNode node, Map<String, Object> vars) {
        CollectShapeExecutor executor = registry.get(node.shape());
        if (executor == null) {
            String message = "节点类型未注册: " + node.shape();
            Map<String, Object> scope = new LinkedHashMap<>(vars);
            scope.put(VAR_EXCEPTION, message);
            context.getSink().nodeLog(node, 0, CollectTaskStatus.FAILURE, 0L, message, null);
            dispatch(context, node, scope, true);
            return;
        }
        List<Object> items = resolveLoopItems(node, vars);
        if (items.isEmpty()) {
            context.getSink().nodeLog(node, 0, CollectTaskStatus.SKIPPED, 0L, "循环集合为空，本节点与下游不执行", null);
            return;
        }
        if (items.size() > 1 && node.boolParam(PARAM_LOOP_PARALLEL, false)) {
            for (int iteration = 0; iteration < items.size(); iteration++) {
                int round = iteration;
                scheduleIteration(context, executor, node, vars, items, round);
            }
            return;
        }
        for (int iteration = 0; iteration < items.size(); iteration++) {
            if (!context.isRunning()) {
                return;
            }
            runIteration(context, executor, node, vars, items, iteration);
        }
    }

    /**
     * 并行模式下把一次迭代作为独立令牌入池。
     *
     * @param context  执行上下文
     * @param executor 节点执行器
     * @param node     节点
     * @param vars     令牌入参作用域
     * @param items    迭代元素
     * @param iteration 轮次
     */
    private void scheduleIteration(CollectContext context, CollectShapeExecutor executor, CollectNode node,
                                   Map<String, Object> vars, List<Object> items, int iteration) {
        if (!context.isRunning()) {
            return;
        }
        context.beginWork();
        context.getPool().submit(() -> {
            try {
                runIteration(context, executor, node, vars, items, iteration);
            } catch (Exception ex) {
                log.warn("[collect] 迭代执行异常 task={} node={}", context.getTaskNo(), node.id(), ex);
            } finally {
                context.endWork();
            }
        });
    }

    /**
     * 执行一次迭代并向后派发。
     *
     * @param context   执行上下文
     * @param executor  节点执行器
     * @param node      节点
     * @param vars      令牌入参作用域
     * @param items     迭代元素
     * @param iteration 轮次
     */
    private void runIteration(CollectContext context, CollectShapeExecutor executor, CollectNode node,
                              Map<String, Object> vars, List<Object> items, int iteration) {
        Map<String, Object> scope = newScope(node, vars, items, iteration);
        long start = System.currentTimeMillis();
        boolean failed;
        try {
            executor.execute(node, context, scope);
            failed = false;
            scope.remove(VAR_EXCEPTION);
        } catch (Exception ex) {
            failed = true;
            String message = describe(ex);
            scope.put(VAR_EXCEPTION, message);
            log.warn("[collect] 节点执行失败 task={} node={} shape={} err={}",
                    context.getTaskNo(), node.id(), node.shape(), message);
        }
        context.countNode();
        long cost = System.currentTimeMillis() - start;
        if (failed) {
            context.getSink().nodeLog(node, iteration, CollectTaskStatus.FAILURE, cost,
                    String.valueOf(scope.get(VAR_EXCEPTION)), null);
            dispatch(context, node, scope, failed);
            return;
        }
        context.getSink().nodeLog(node, iteration, CollectTaskStatus.SUCCESS, cost,
                safeSummary(executor, node, scope), preview(context, vars, scope));
        if (CollectExpression.truthy(scope.get(VAR_STOP))) {
            context.getSink().nodeLog(node, iteration, CollectTaskStatus.SKIPPED, 0L,
                    "本节点已终止该分支，下游未执行", null);
            return;
        }
        dispatch(context, node, scope, false);
    }

    /**
     * 构造一次迭代的作用域，写入循环下标与循环元素。
     *
     * @param node      节点
     * @param vars      令牌入参作用域
     * @param items     迭代元素
     * @param iteration 轮次
     * @return 本次迭代独立作用域
     */
    private Map<String, Object> newScope(CollectNode node, Map<String, Object> vars,
                                         List<Object> items, int iteration) {
        Map<String, Object> scope = new LinkedHashMap<>(vars);
        if (node.param(PARAM_LOOP_COUNT).isEmpty()) {
            return scope;
        }
        scope.put(nonDefault(node.param(PARAM_LOOP_INDEX), "index"), (long) iteration);
        scope.put(nonDefault(node.param(PARAM_LOOP_ITEM_VARIABLE), "item"), items.get(iteration));
        return scope;
    }

    /**
     * 按边的异常流向与条件派发下游令牌。
     *
     * @param context 执行上下文
     * @param node    当前节点
     * @param vars    当前令牌作用域
     * @param failed  当前令牌是否失败
     */
    private void dispatch(CollectContext context, CollectNode node, Map<String, Object> vars, boolean failed) {
        boolean rescued = false;
        for (CollectEdge edge : node.getOutEdges()) {
            boolean traversed = failed ? edge.allowWhenException()
                    : edge.allowWhenNormal() && CollectExpression.evalCondition(edge.condition(), vars);
            if (!traversed) {
                context.getSink().nodeLog(edge.getTarget(), 0, CollectTaskStatus.SKIPPED, 0L,
                        node.name() + " → " + edge.getTarget().name()
                                + (failed ? " 上游异常且本边不允许异常流转" : " 连线条件不成立: " + edge.condition()),
                        null);
                rescued = rescued || failed && edge.allowWhenException();
                continue;
            }
            Map<String, Object> child = new LinkedHashMap<>(context.getRootVariables());
            if (edge.transmitVariable()) {
                child.putAll(vars);
            }
            schedule(context, edge.getTarget(), child);
            rescued = rescued || failed;
        }
        if (failed && !rescued) {
            context.markFailure(String.valueOf(vars.get(VAR_EXCEPTION)));
        }
    }

    /**
     * 解析循环集合：次数、集合、元素集、数组、多行文本或单次执行。
     *
     * @param node 节点
     * @param vars 令牌作用域
     * @return 迭代元素列表，未配置循环参数时返回单元素列表
     */
    private List<Object> resolveLoopItems(CollectNode node, Map<String, Object> vars) {
        String raw = node.param(PARAM_LOOP_COUNT);
        if (raw.isEmpty()) {
            return List.of("");
        }
        Object value = CollectExpression.eval(raw, vars);
        if (value == null) {
            return List.of();
        }
        if (value instanceof Number number) {
            return rounds(number.intValue());
        }
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        if (value instanceof org.jsoup.select.Elements elements) {
            List<Object> items = new ArrayList<>();
            for (org.jsoup.nodes.Element element : elements) {
                items.add(element);
            }
            return items;
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            List<Object> items = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                items.add(java.lang.reflect.Array.get(value, i));
            }
            return items;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return List.of();
        }
        try {
            return rounds(Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            return new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        }
    }

    /**
     * 生成次数循环的下标列表。
     *
     * @param count 次数
     * @return 次数不超过 0 时返回空列表，否则返回 0..count-1
     */
    private List<Object> rounds(int count) {
        if (count <= 0) {
            return List.of();
        }
        List<Object> rounds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rounds.add((long) i);
        }
        return rounds;
    }

    /**
     * 生成出参预览，只保留本令牌新增或改变的变量，避免整页 HTML 落库。
     *
     * @param context  执行上下文
     * @param incoming 令牌入参作用域
     * @param scope    本次迭代作用域
     * @return 预览文本
     */
    private String preview(CollectContext context, Map<String, Object> incoming, Map<String, Object> scope) {
        int limit = Math.max(32, context.getProperties().getPreviewLength());
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : scope.entrySet()) {
            if (entry.getKey().startsWith("_") || entry.getValue() == null) {
                continue;
            }
            Object before = incoming.get(entry.getKey());
            if (before != null && before.equals(entry.getValue())) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("; ");
            }
            Object value = entry.getValue();
            builder.append(entry.getKey()).append('=');
            if (value instanceof Collection || value instanceof org.jsoup.select.Elements) {
                builder.append('[').append(CollectFunctions.size(value)).append(" 项]");
            } else {
                String text = CollectFunctions.stringify(value).replaceAll("\\s+", " ");
                builder.append(text.length() > limit ? text.substring(0, limit) + "…" : text);
            }
            if (builder.length() > limit * 3) {
                break;
            }
        }
        return builder.length() > limit * 4 ? builder.substring(0, limit * 4) : builder.toString();
    }

    /**
     * 取执行器摘要，异常降级为空。
     *
     * @param executor 执行器
     * @param node     节点
     * @param scope    作用域
     * @return 摘要文本
     */
    private String safeSummary(CollectShapeExecutor executor, CollectNode node, Map<String, Object> scope) {
        try {
            return executor.summary(node, scope);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 异常说明压成单行。
     *
     * @param ex 异常
     * @return 单行说明
     */
    private String describe(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return message.replaceAll("\\s+", " ");
    }

    /**
     * 参数值兜底。
     *
     * @param value    参数值
     * @param fallback 兜底值
     * @return 非空参数值
     */
    private String nonDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * 汇总执行结果。
     *
     * @param context 执行上下文
     * @param begin   开始毫秒
     * @param status  状态
     * @param error   失败说明
     * @return 执行结果
     */
    private CollectRunResult finish(CollectContext context, long begin, String status, String error) {
        CollectRunResult result = new CollectRunResult();
        result.setTaskId(context.getTaskId());
        result.setTaskNo(context.getTaskNo());
        result.setStatus(status);
        result.setCostMs(System.currentTimeMillis() - begin);
        result.setNodeCount(context.getNodeCount().get());
        result.setRequestCount(context.getRequestCount().get());
        result.setRecordCount(context.getRecordCount().get());
        result.setErrorMessage(error);
        result.getPreview().addAll(context.getRecords());
        result.getVariables().putAll(context.getRootVariables());
        return result;
    }

    /**
     * 停止任务。
     *
     * @param taskId 任务ID
     * @return 命中运行中任务返回 true
     */
    public boolean cancel(Long taskId) {
        CollectContext context = running.get(taskId);
        if (context == null) {
            return false;
        }
        context.cancel();
        return true;
    }

    /**
     * 任务是否仍在执行。
     *
     * @param taskId 任务ID
     * @return 执行中返回 true
     */
    public boolean isRunning(Long taskId) {
        return running.containsKey(taskId);
    }

    /**
     * 当前执行中的任务数。
     *
     * @return 执行中任务数
     */
    public int runningCount() {
        return running.size();
    }

    /**
     * 本机某流程正在执行的任务数。
     *
     * @param flowId 流程ID
     * @return 执行中任务数
     */
    public int inflightOfFlow(Integer flowId) {
        if (flowId == null) {
            return 0;
        }
        int total = 0;
        for (CollectContext context : running.values()) {
            if (flowId.equals(context.getFlowId())) {
                total++;
            }
        }
        return total;
    }

    /**
     * 某流程是否正在执行。
     *
     * @param flowId 流程ID
     * @return 执行中返回 true
     */
    public boolean isFlowRunning(Integer flowId) {
        return inflightOfFlow(flowId) > 0;
    }
}
