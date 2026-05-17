# Rust Server (tokio+hyper + Panama FFI) 并发模型

## 架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                       JVM (JDK 25)                              │
│                                                                 │
│  ┌───────────────────────┐    ┌──────────────────────────────┐  │
│  │   Virtual Threads     │    │   Servlet API 处理链          │  │
│  │  (per-task, 轻量)     │───▶│  Filter → Servlet → Response  │  │
│  └───────────────────────┘    └──────────────────────────────┘  │
│           ▲                                    │                │
│           │ Panama FFI upcall                   │ downcall       │
│           ▼                                    ▼                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │               NativeBridge (MemorySegment)                │  │
│  └───────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ JNI / Panama downcall
┌───────────────────────────▼─────────────────────────────────────┐
│                     Rust (tokio + hyper)                        │
│                                                                 │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  tokio      │  │  PENDING     │  │  oneshot channel     │   │
│  │  reactor    │──│  Registry    │──│  (等待 Java 响应)     │   │
│  │  (evented)  │  │  (HashMap)   │  └──────────────────────┘   │
│  └─────────────┘  └──────────────┘                              │
│                                                                 │
│  Worker threads: min(CPU cores × 2, workerThreads config)      │
└─────────────────────────────────────────────────────────────────┘
```

## 线程模型对比

### Tomcat (传统 Servlet 容器)

| 属性 | Tomcat |
|------|--------|
| IO 模型 | NIO (非阻塞 IO, java.nio.channels) |
| 请求处理 | thread-per-request, 一个平台线程处理一个请求全过程 |
| 线程池 | `server.tomcat.threads.max` (默认 200) |
| 线程开销 | ~1MB/线程 (栈 + TLAB + 内核结构) |
| 上下文切换 | 高 (大量线程竞争 CPU) |
| 最大连接数 | `server.tomcat.max-connections` (默认 8192) |
| KeepAlive | 独立线程管理超时 |

**请求生命周期 (Tomcat)**:
```
Acceptor 线程 accept → NIO Poller 读请求 → 线程池 worker 处理 → 写响应
                           ↓
                     工作线程阻塞等待 IO / 数据库 / 业务逻辑
                           ↓
                     线程归还到池 (可能被回收或重新分配)
```

### Rust Server (tokio+hyper + Panama FFI)

| 属性 | Rust Server |
|------|-------------|
| IO 模型 | evented IO (io_uring / epoll via tokio) |
| 请求处理 | 异步事件驱动, 少量 tokio worker 处理海量连接 |
| 线程池 | `worker-threads` (默认 0 = 自动 = CPU cores × 2) |
| 线程开销 | ~2KB/连接 (tokio task) + 固定 worker 线程 |
| 上下文切换 | 低 (少量线程, 协作式调度) |
| 最大连接数 | 仅受系统 fd 限制 (通常 10万+) |
| KeepAlive | tokio 定时器, 零额外线程 |

**请求生命周期 (Rust Server)**:
```
tokio reactor 接收连接 → hyper 解析 HTTP → FFI upcall → Java VT 处理
                                                              ↓
                                                        业务逻辑 (可能阻塞)
                                                              ↓
                                                        FFI downcall 回 Rust
                                                              ↓
                                                        tokio 写响应
```

## Async FFI: PENDING Registry + Oneshot Channel

### 架构

```
Rust 端                              Java 端
┌──────────────┐                    ┌────────────────┐
│ 接收 HTTP     │─── FFI upcall ──▶ │  虚拟线程开始    │
│ 请求          │   (request_id)    │  处理请求        │
│              │                    │                  │
│ 存入 PENDING  │                    │  调用 Servlet    │
│ Registry     │                    │   API            │
│              │                    │                  │
│ await oneshot │◀── Java 完成 ────│  收集响应数据     │
│ channel      │   (downcall)      │                  │
│              │                    │  通知 Rust 发送   │
│ 发送 HTTP     │                    │                  │
│ 响应          │                    └────────────────┘
└──────────────┘
```

### 数据结构

**Rust (lib.rs)**:
```rust
static PENDING: LazyLock<Mutex<HashMap<i64, PendingRequest>>> = ...;

struct PendingRequest {
    sender: oneshot::Sender<Vec<u8>>,   // 发送响应体
    content_type: String,               // 原始 Content-Type
    // ...其他响应元数据
}
```

**Java (NativeBridge.java)**:
```java
// 虚拟线程池
private static final ExecutorService EXECUTOR =
    Executors.newVirtualThreadPerTaskExecutor();

// FFI callback → 虚拟线程
void onRustRequest(long requestId, MemorySegment data, ...) {
    EXECUTOR.submit(() -> {
        // 1. 解析请求
        HttpRequestData requestData = parse(data, ...);
        // 2. 调用 Servlet 链
        callback.onRequest(requestData, responseBuffer);
        // 3. 通知 Rust 发送响应
        rustSendResponse(requestId, ...);
    });
}
```

## 虚拟线程 (Virtual Threads)

JDK 25 虚拟线程的核心特性:
- **创建**: 纳秒级, 无池化 (每个请求创建新 VT)
- **栈**: 初始 ~10KB, 动态扩展 (vs 平台线程 ~1MB)
- **载体**: 在 ForkJoinPool (commonPool) 上调度
- **阻塞**: 遇到阻塞操作自动 unmount, 载体线程做其他工作

在 Rust Server 中的使用:
```java
ExecutorService vtPool = Executors.newVirtualThreadPerTaskExecutor();
// 每个 FFI upcall 提交一个 VT
vtPool.submit(() -> handleRequest(requestId, data, ...));
```

## Session 管理

```java
class SessionManager {
    ConcurrentHashMap<String, SimpleHttpSession> sessions;
    ScheduledExecutorService cleanup;
    
    // 过期 session 清理 (每 60s)
    cleanup.scheduleAtFixedRate(this::purgeExpired, 60, 60, SECONDS);
    
    // 虚拟线程安全 (ConcurrentHashMap)
    SimpleHttpSession getOrCreate(String id);
}
```

## 性能对比预估

> 实际运行结果请填写 [BENCHMARK_RESULTS_TEMPLATE.md](./BENCHMARK_RESULTS_TEMPLATE.md)。以下为理论预估。

### 无状态请求 (CPU-bound / 最小响应)

| 场景 |

### 无状态请求 (CPU-bound / 最小响应)

| 场景 | Tomcat (200 线程) | Rust (VT + evented) | 预期倍率 |
|------|-------------------|---------------------|----------|
| `GET /api/health` | ~15,000 ops/s | ~25,000 ops/s | ~1.7x |
| `GET /api/echo` | ~12,000 ops/s | ~20,000 ops/s | ~1.7x |
| `GET /api/users` | ~8,000 ops/s | ~15,000 ops/s | ~1.9x |

### IO 等待场景

| 场景 | Tomcat | Rust | 说明 |
|------|--------|------|------|
| `GET /api/delay/100` | ~1,800 ops/s (~200线程/100ms) | ~8,000 ops/s | VT 在 sleep 时 unmount, 不占线程 |
| 数据库查询 (50ms) | ~3,500 ops/s | ~18,000 ops/s | IO 等待不阻塞 worker |

### 高并发 (连接数 > 200)

| 连接数 | Tomcat | Rust | 说明 |
|--------|--------|------|------|
| 200 | 满吞吐 | 满吞吐 | 两者都能应对 |
| 1,000 | 线程耗尽, 排队 | tokio 事件驱动 | Rust 无需额外线程 |
| 10,000 | 需调优 max-connections | 仅受 fd 限制 | Rust 连接开销更低 |

## 关键区别总结

| 维度 | Tomcat | Rust Server |
|------|--------|-------------|
| IO 模型 | NIO (多路复用 Acceptor+Poller+Worker) | evented IO (tokio, io_uring/epoll) |
| 请求线程 | 平台线程池 (默认 200) | 虚拟线程 (per-task) + tokio worker (CPU×2) |
| 线程开销 | ~200MB 基线 (200 线程) | ~2MB 基线 (少量 worker + VT 动态) |
| 高连接扩展性 | 受线程池限制 | 仅受内存限制 |
| 上下文切换 | 高 (OS 级调度) | 低 (协作式 + VT unmount) |
| KeepAlive 管理 | 独立线程 | tokio 定时器 |
| Session 存储 | ConcurrentHashMap (内部) | ConcurrentHashMap (与 Tomcat 同) |
| 静态资源 | Tomcat DefaultServlet | StaticResourceServlet (同 Servlet API) |
| 错误页面 | Tomcat ErrorPage 机制 | DefaultErrorPages (自定义实现) |

## 参考

- [Project Loom: Virtual Threads (JEP 444)](https://openjdk.org/jeps/444)
- [tokio: Asynchronous runtime for Rust](https://tokio.rs/)
- [hyper: HTTP implementation for Rust](https://hyper.rs/)
- [Panama Foreign Function & Memory API (JEP 454)](https://openjdk.org/jeps/454)
