# 文件上传队列监控和调优指南

## 监控指标

### 1. 核心性能指标

#### 1.1 队列状态指标
```java
// 内存队列大小
int memoryQueueSize = memoryQueue.size();

// 各服务器队列大小
Map<Integer, Integer> serverQueueSizes = serverQueues.entrySet().stream()
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> entry.getValue().size()
    ));

// 队列加载状态
boolean isQueueLoaded = queueLoaded.get();

// 队列处理状态
boolean isProcessingEnabled = queueProcessingEnabled.get();
```

#### 1.2 处理性能指标
```java
// 通过统计接口获取
ReturnResult<MonitorSysGenServerFileUploadQueueStatistics> statsResult = queueService.getQueueStatistics();
MonitorSysGenServerFileUploadQueueStatistics stats = statsResult.getData();

// 关键指标
long pendingCount = stats.getPendingCount();      // 待处理任务数
long processingCount = stats.getProcessingCount(); // 处理中任务数
long completedCount = stats.getCompletedCount();   // 已完成任务数
long failedCount = stats.getFailedCount();         // 失败任务数
double successRate = stats.getSuccessRate();       // 成功率
double throughputPerHour = stats.getThroughputPerHour(); // 每小时吞吐量
```

### 2. 系统资源指标

#### 2.1 内存使用监控
```java
// JVM内存使用
MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
long usedMemory = heapUsage.getUsed();
long maxMemory = heapUsage.getMax();
double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

// 队列内存占用估算
int totalQueueItems = memoryQueue.size() + 
    serverQueues.values().stream().mapToInt(Queue::size).sum();
// 假设每个队列项占用约1KB内存
long estimatedQueueMemory = totalQueueItems * 1024;
```

#### 2.2 线程和锁监控
```java
// 线程池状态（如果使用线程池处理）
ThreadPoolExecutor executor = (ThreadPoolExecutor) taskExecutor;
int activeThreads = executor.getActiveCount();
int poolSize = executor.getPoolSize();
long completedTasks = executor.getCompletedTaskCount();

// 锁竞争监控（需要JVM参数支持）
// -XX:+PrintGCDetails -XX:+PrintConcurrentLocks
```

## 告警配置

### 1. 关键告警指标

#### 1.1 队列积压告警
```yaml
# 示例配置
alerts:
  queue_backlog:
    # 内存队列积压超过1000个任务
    memory_queue_size: 1000
    # 单个服务器队列积压超过100个任务
    server_queue_size: 100
    # 待处理任务数超过5000个
    pending_tasks: 5000
```

#### 1.2 性能告警
```yaml
alerts:
  performance:
    # 成功率低于95%
    success_rate_threshold: 95.0
    # 每小时吞吐量低于预期
    throughput_threshold: 1000
    # 平均处理时间超过30秒
    avg_process_time_ms: 30000
```

#### 1.3 系统资源告警
```yaml
alerts:
  resources:
    # 内存使用率超过80%
    memory_usage_threshold: 80.0
    # 队列内存占用超过100MB
    queue_memory_threshold: 104857600
    # 锁等待时间超过1秒
    lock_wait_time_ms: 1000
```

### 2. 告警实现示例

#### 2.1 队列监控组件
```java
@Component
@Slf4j
public class QueueMonitor {
    
    @Autowired
    private MonitorSysGenServerFileUploadQueueService queueService;
    
    @Value("${queue.alert.memory-queue-size:1000}")
    private int memoryQueueSizeThreshold;
    
    @Value("${queue.alert.success-rate:95.0}")
    private double successRateThreshold;
    
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void monitorQueueHealth() {
        try {
            // 检查队列积压
            checkQueueBacklog();
            
            // 检查性能指标
            checkPerformanceMetrics();
            
            // 检查系统资源
            checkSystemResources();
            
        } catch (Exception e) {
            log.error("队列监控检查失败", e);
        }
    }
    
    private void checkQueueBacklog() {
        // 获取队列统计信息
        ReturnResult<MonitorSysGenServerFileUploadQueueStatistics> result = 
            queueService.getQueueStatistics();
            
        if (result.isSuccess() && result.getData() != null) {
            MonitorSysGenServerFileUploadQueueStatistics stats = result.getData();
            
            if (stats.getPendingCount() > memoryQueueSizeThreshold) {
                sendAlert("队列积压告警", 
                    String.format("待处理任务数: %d, 超过阈值: %d", 
                        stats.getPendingCount(), memoryQueueSizeThreshold));
            }
        }
    }
    
    private void checkPerformanceMetrics() {
        ReturnResult<MonitorSysGenServerFileUploadQueueStatistics> result = 
            queueService.getQueueStatistics();
            
        if (result.isSuccess() && result.getData() != null) {
            MonitorSysGenServerFileUploadQueueStatistics stats = result.getData();
            
            if (stats.getSuccessRate() < successRateThreshold) {
                sendAlert("性能告警", 
                    String.format("成功率: %.2f%%, 低于阈值: %.2f%%", 
                        stats.getSuccessRate(), successRateThreshold));
            }
        }
    }
    
    private void sendAlert(String title, String message) {
        // 实现告警发送逻辑（邮件、短信、钉钉等）
        log.warn("队列告警 - {}: {}", title, message);
    }
}
```

## 性能调优

### 1. 定时任务频率调优

#### 1.1 根据业务场景调整
```java
// 高频场景：每2秒同步一次
@Scheduled(fixedRate = 2000)

// 中频场景：每5秒同步一次（默认）
@Scheduled(fixedRate = 5000)

// 低频场景：每30秒同步一次
@Scheduled(fixedRate = 30000)
```

#### 1.2 动态调整策略
```java
@Component
public class DynamicSchedulingConfig {
    
    private long currentRate = 5000; // 默认5秒
    
    @Scheduled(fixedRate = 60000) // 每分钟评估一次
    public void adjustSchedulingRate() {
        // 根据队列积压情况动态调整
        int queueSize = getQueueSize();
        
        if (queueSize > 1000) {
            currentRate = 2000; // 积压多时加快同步
        } else if (queueSize < 100) {
            currentRate = 10000; // 积压少时减慢同步
        } else {
            currentRate = 5000; // 正常情况
        }
    }
}
```

### 2. 内存队列优化

#### 2.1 队列容量限制
```java
// 使用有界队列防止内存溢出
private final BlockingQueue<MonitorSysGenServerFileUploadQueue> memoryQueue = 
    new LinkedBlockingQueue<>(10000); // 限制最大10000个任务

// 队列满时的处理策略
public boolean enqueueWithLimit(MonitorSysGenServerFileUploadQueue item) {
    if (!memoryQueue.offer(item)) {
        // 队列满时的处理策略
        log.warn("内存队列已满，丢弃最旧的任务");
        memoryQueue.poll(); // 移除最旧的任务
        return memoryQueue.offer(item); // 重新尝试添加
    }
    return true;
}
```

#### 2.2 队列清理策略
```java
@Scheduled(fixedRate = 300000) // 每5分钟清理一次
public void cleanupMemoryQueue() {
    try {
        queueLock.lock();
        
        // 清理过期的队列项（超过1小时未处理）
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        
        memoryQueue.removeIf(item -> 
            item.getMonitorSysGenServerFileUploadQueueCreateTime().isBefore(cutoffTime));
            
        // 清理服务器队列
        serverQueues.values().forEach(queue -> 
            queue.removeIf(item -> 
                item.getMonitorSysGenServerFileUploadQueueCreateTime().isBefore(cutoffTime)));
                
    } finally {
        queueLock.unlock();
    }
}
```

### 3. 锁优化

#### 3.1 读写锁优化
```java
// 使用读写锁提高并发性能
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
private final Lock readLock = rwLock.readLock();
private final Lock writeLock = rwLock.writeLock();

// 读操作使用读锁
public MonitorSysGenServerFileUploadQueue dequeue() {
    readLock.lock();
    try {
        return memoryQueue.poll();
    } finally {
        readLock.unlock();
    }
}

// 写操作使用写锁
public void loadPendingTasksToMemoryQueue() {
    writeLock.lock();
    try {
        // 加载逻辑
    } finally {
        writeLock.unlock();
    }
}
```

#### 3.2 无锁优化
```java
// 使用ConcurrentLinkedQueue实现无锁队列
private final ConcurrentLinkedQueue<MonitorSysGenServerFileUploadQueue> lockFreeQueue = 
    new ConcurrentLinkedQueue<>();

// 无锁操作
public MonitorSysGenServerFileUploadQueue dequeueLockFree() {
    return lockFreeQueue.poll();
}

public boolean enqueueLockFree(MonitorSysGenServerFileUploadQueue item) {
    return lockFreeQueue.offer(item);
}
```

## 故障排查

### 1. 常见问题诊断

#### 1.1 队列不工作
```bash
# 检查队列加载状态
curl http://localhost:8080/actuator/health/queue

# 检查定时任务是否正常执行
grep "已加载.*个待处理任务到内存队列" application.log

# 检查数据库连接
curl http://localhost:8080/actuator/health/db
```

#### 1.2 性能问题诊断
```bash
# 检查内存使用
jstat -gc <pid>

# 检查线程状态
jstack <pid> | grep -A 10 "loadPendingTasksToMemoryQueue"

# 检查锁竞争
java -XX:+PrintConcurrentLocks -jar app.jar
```

### 2. 应急处理

#### 2.1 队列重置
```java
@RestController
public class QueueManagementController {
    
    @PostMapping("/admin/queue/reset")
    public ResponseEntity<String> resetQueue() {
        try {
            // 清空内存队列
            memoryQueue.clear();
            serverQueues.clear();
            
            // 重新加载
            loadPendingTasksToMemoryQueue();
            
            return ResponseEntity.ok("队列重置成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("队列重置失败: " + e.getMessage());
        }
    }
}
```

#### 2.2 降级处理
```java
// 当内存队列出现问题时，临时切换回数据库查询模式
@Value("${queue.fallback.enabled:false}")
private boolean fallbackEnabled;

public ReturnResult<MonitorSysGenServerFileUploadQueue> dequeue() {
    if (fallbackEnabled || !queueLoaded.get()) {
        // 降级到数据库查询模式
        return dequeueFromDatabase();
    }
    
    // 正常的内存队列模式
    return dequeueFromMemory();
}
```

通过以上监控和调优措施，可以确保文件上传队列系统稳定高效地运行，并能及时发现和解决潜在问题。
