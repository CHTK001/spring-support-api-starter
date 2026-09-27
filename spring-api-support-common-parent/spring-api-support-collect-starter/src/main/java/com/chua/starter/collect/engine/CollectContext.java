package com.chua.starter.collect.engine;

import com.chua.starter.collect.dto.CollectParamDTO;
import com.chua.starter.collect.properties.CollectProperties;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一次流程执行的上下文。
 *
 * <p>承载全局变量、全局请求头与 Cookie 声明、计量计数与停止标记。每次执行独占一个实例与线程池，
 * 不跨任务共享可变状态。节点令牌（一次节点执行）只读写自己那份作用域，全局变量通过
 * {@link #getRootVariables()} 这个并发映射交换，因此汇聚节点无需到达计数。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Getter
public class CollectContext {

    /**
     * 流程ID。
     */
    private final Integer flowId;

    /**
     * 任务ID。
     */
    private final Long taskId;

    /**
     * 任务号。
     */
    private final String taskNo;

    /**
     * 平台配置。
     */
    private final CollectProperties properties;

    /**
     * 落库通道。
     */
    private final CollectSink sink;

    /**
     * 全局变量表，跨分支共享，变量节点与抓取节点写在这里。
     */
    private final Map<String, Object> rootVariables = new ConcurrentHashMap<>();

    /**
     * 节点执行次数计量。
     */
    private final AtomicInteger nodeCount = new AtomicInteger();

    /**
     * 抓取次数计量。
     */
    private final AtomicInteger requestCount = new AtomicInteger();

    /**
     * 产出记录计量。
     */
    private final AtomicInteger recordCount = new AtomicInteger();

    /**
     * 未完成的工作项数量。
     */
    private final AtomicInteger activeWork = new AtomicInteger();

    /**
     * 首个未被容错的节点失败说明，决定任务最终状态。
     */
    private volatile String failureMessage;

    /**
     * 结果预览缓冲，最多 maxRecordPerTask 条。
     */
    private final List<CollectRecordEntry> records = Collections.synchronizedList(new ArrayList<>());

    /**
     * 完成监视器。
     */
    private final Object finishMonitor = new Object();

    /**
     * 全局请求头。
     */
    private final List<CollectParamDTO> globalHeaders = new ArrayList<>();

    /**
     * 全局 Cookie 声明。
     */
    private final List<CollectParamDTO> globalCookies = new ArrayList<>();

    /**
     * 本次执行的线程池。
     */
    private ExecutorService pool;

    /**
     * 运行标记，置 false 后引擎不再派发新节点。
     */
    private volatile boolean running = true;

    /**
     * 父上下文，子流程节点里非空；计量、失败说明与停止标记都向它归并。
     */
    private final CollectContext parent;

    /**
     * 嵌套层数，根流程为 0。
     */
    private final int depth;

    /**
     * 构造根执行上下文。
     *
     * @param flowId     流程ID
     * @param taskId     任务ID
     * @param taskNo     任务号
     * @param properties 平台配置
     * @param sink       落库通道
     */
    public CollectContext(Integer flowId, Long taskId, String taskNo,
                          CollectProperties properties, CollectSink sink) {
        this(flowId, taskId, taskNo, properties, sink, null);
    }

    /**
     * 构造执行上下文。
     *
     * @param flowId     流程ID
     * @param taskId     任务ID
     * @param taskNo     任务号
     * @param properties 平台配置
     * @param sink       落库通道
     * @param parent     父上下文，根执行为 null
     */
    private CollectContext(Integer flowId, Long taskId, String taskNo,
                           CollectProperties properties, CollectSink sink, CollectContext parent) {
        this.flowId = flowId;
        this.taskId = taskId;
        this.taskNo = taskNo;
        this.properties = properties;
        this.sink = sink;
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;
    }

    /**
     * 以父上下文的任务身份构造子流程上下文。
     *
     * <p>子流程不单独占一行任务，采集结果与计量都记进父任务，因此任务号与落库通道沿用父级；
     * 只有流程ID与变量作用域是自己的。落库通道换成按子流程标注的视图，节点日志才看得出出自哪条
     * 流程，否则两条流程里同名的 n1、n2 会在画布着色时互相串台。</p>
     *
     * @param parent 父上下文
     * @param flowId 子流程ID
     * @return 子流程上下文
     */
    public static CollectContext nested(CollectContext parent, Integer flowId) {
        return new CollectContext(flowId, parent.getTaskId(), parent.getTaskNo(),
                parent.getProperties(), parent.getSink().forFlow(flowId), parent);
    }

    /**
     * 调用链上是否出现过某个流程，用于拒绝成环的子流程引用。
     *
     * @param flowId 流程ID
     * @return 链上已有该流程返回 true
     */
    public boolean referencesFlow(Integer flowId) {
        if (flowId == null) {
            return false;
        }
        for (CollectContext item = this; item != null; item = item.parent) {
            if (flowId.equals(item.getFlowId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 增加一次待完成工作。
     */
    public void beginWork() {
        activeWork.incrementAndGet();
    }

    /**
     * 完成一次工作，归零时唤醒等待线程。
     */
    public void endWork() {
        if (activeWork.decrementAndGet() <= 0) {
            synchronized (finishMonitor) {
                finishMonitor.notifyAll();
            }
        }
    }

    /**
     * 等待全部工作完成。
     *
     * @param timeoutMs 最长等待毫秒
     * @return 正常结束返回 true，超时返回 false
     * @throws InterruptedException 等待被中断
     */
    public boolean awaitFinish(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        synchronized (finishMonitor) {
            while (activeWork.get() > 0 && isRunning()) {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) {
                    return false;
                }
                finishMonitor.wait(Math.min(left, 500L));
            }
        }
        return true;
    }

    /**
     * 请求停止执行。
     */
    public void cancel() {
        running = false;
        synchronized (finishMonitor) {
            finishMonitor.notifyAll();
        }
    }

    /**
     * 线程池赋值。
     *
     * @param executor 线程池
     */
    public void attachPool(ExecutorService executor) {
        this.pool = executor;
    }

    /**
     * 写入全局变量。底层映射不接受 null，求值为空的变量落成空串，避免整次执行抛空指针。
     *
     * @param name  变量名
     * @param value 变量值，null 落成空串
     */
    public void putRootVariable(String name, Object value) {
        if (name == null || name.isBlank()) {
            return;
        }
        rootVariables.put(name.trim(), value == null ? "" : value);
    }

    /**
     * 登记首个未被容错的失败说明，后续失败不覆盖。
     *
     * <p>子流程的失败不冒泡到这里：process 节点会把它转成异常抛出，由父流程按边的异常流向
     * 决定容错还是让任务失败，提前冒泡会让本可被异常边救回的任务落成失败。</p>
     *
     * @param message 失败说明
     */
    public void markFailure(String message) {
        if (failureMessage == null) {
            failureMessage = message;
        }
    }

    /**
     * 是否仍在运行，父级被停止后子流程一并视为停止。
     *
     * @return 继续执行返回 true
     */
    public boolean isRunning() {
        return running && (parent == null || parent.isRunning());
    }

    /**
     * 产出一条采集记录，超过单任务上限时不落库。
     *
     * <p>限流与落库统一在根上下文完成，本任务的全部记录数因此包含子流程产出；
     * 子流程自己那份计数只用于回报本层产了多少条。</p>
     *
     * @param entry 结果条目
     * @return 已落库返回 true，被上限丢弃返回 false
     */
    public boolean offerRecord(CollectRecordEntry entry) {
        if (parent != null) {
            boolean kept = parent.offerRecord(entry);
            if (kept) {
                recordCount.incrementAndGet();
            }
            return kept;
        }
        long used = recordCount.incrementAndGet();
        int max = properties.getMaxRecordPerTask();
        if (max > 0 && used > max) {
            recordCount.decrementAndGet();
            return false;
        }
        sink.record(entry);
        records.add(entry);
        return true;
    }

    /**
     * 累加节点执行计量，子流程的节点计入父任务总数。
     */
    public void countNode() {
        nodeCount.incrementAndGet();
        if (parent != null) {
            parent.countNode();
        }
    }

    /**
     * 累加抓取计量，子流程的抓取计入父任务总数。
     */
    public void countRequest() {
        requestCount.incrementAndGet();
        if (parent != null) {
            parent.countRequest();
        }
    }
}
