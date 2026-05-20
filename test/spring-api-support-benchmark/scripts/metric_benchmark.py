#!/usr/bin/env python3
"""
Rust vs Tomcat 资源指标采集压测脚本。

对比测试 1000 / 10000 并发下两者的：
- 吞吐量 (ops/s)
- 延迟 (p50/p90/p99 μs)
- 进程内存 (RSS MB)
- JVM 堆内存 (MB)
- 活跃线程数
- 活跃 TCP 连接数
- CPU 使用率 (%)
- 错误率
"""

import asyncio
import re
import subprocess
import sys
import time
from collections import defaultdict
from dataclasses import dataclass, field

import aiohttp
import psutil

RUST_PORT = 18081
TOMCAT_PORT = 18082

ENDPOINTS = [
    "/api/health",
    "/api/users",
]

WARMUP_SECONDS = 3
MEASURE_SECONDS = 10


@dataclass
class ProcSnapshot:
    timestamp: float
    rss_mb: float = 0
    heap_used_mb: float = 0
    heap_committed_mb: float = 0
    threads: int = 0
    cpu_percent: float = 0
    connections: int = 0


@dataclass
class BenchmarkResult:
    label: str
    endpoint: str
    concurrency: int
    total_requests: int = 0
    errors: int = 0
    throughput: float = 0
    latency_p50: float = 0
    latency_p90: float = 0
    latency_p99: float = 0
    latency_avg: float = 0
    latency_max: float = 0
    snapshots: list = field(default_factory=list)

    @property
    def avg_rss_mb(self):
        return sum(s.rss_mb for s in self.snapshots) / max(len(self.snapshots), 1)

    @property
    def avg_heap_mb(self):
        return sum(s.heap_used_mb for s in self.snapshots) / max(len(self.snapshots), 1)

    @property
    def avg_threads(self):
        return sum(s.threads for s in self.snapshots) / max(len(self.snapshots), 1)

    @property
    def avg_connections(self):
        return sum(s.connections for s in self.snapshots) / max(len(self.snapshots), 1)

    @property
    def avg_cpu(self):
        return sum(s.cpu_percent for s in self.snapshots) / max(len(self.snapshots), 1)

    @property
    def error_rate(self):
        return (self.errors / max(self.total_requests, 1)) * 100


def get_pids_by_port(port: int) -> list[int]:
    pids = set()
    for conn in psutil.net_connections():
        if conn.laddr and conn.laddr.port == port and conn.status == "LISTEN":
            pids.add(conn.pid)
    return list(pids)


def jcmd_heap(pid: int) -> dict:
    try:
        result = subprocess.run(
            ["jcmd", str(pid), "GC.heap_info"],
            capture_output=True, text=True, timeout=5
        )
        out = result.stdout
        heap_used = heap_committed = 0.0
        for line in out.split("\n"):
            m = re.search(r"used\s+(\d+)([KMG])", line)
            if m:
                val = int(m.group(1))
                unit = m.group(2)
                if unit == "K": heap_used = val / 1024
                elif unit == "M": heap_used = float(val)
                elif unit == "G": heap_used = val * 1024
            m = re.search(r"committed\s+(\d+)([KMG])", line)
            if m:
                val = int(m.group(1))
                unit = m.group(2)
                if unit == "K": heap_committed = val / 1024
                elif unit == "M": heap_committed = float(val)
                elif unit == "G": heap_committed = val * 1024
        return {"heap_used_mb": heap_used, "heap_committed_mb": heap_committed}
    except Exception:
        return {"heap_used_mb": 0, "heap_committed_mb": 0}


def count_server_connections(port: int) -> int:
    """Count ESTABLISHED connections accepted by server on given port (laddr.port == port)."""
    count = 0
    for conn in psutil.net_connections():
        if conn.status == "ESTABLISHED" and conn.laddr and conn.laddr.port == port:
            count += 1
    return count


def snapshot_process(pid: int, port: int) -> ProcSnapshot:
    snap = ProcSnapshot(timestamp=time.time())
    try:
        proc = psutil.Process(pid)
        with proc.oneshot():
            mem_info = proc.memory_info()
            snap.rss_mb = mem_info.rss / (1024 * 1024)
            snap.threads = proc.num_threads()
            snap.cpu_percent = proc.cpu_percent(interval=0.5)
        snap.connections = count_server_connections(port)
        heap = jcmd_heap(pid)
        snap.heap_used_mb = heap["heap_used_mb"]
        snap.heap_committed_mb = heap["heap_committed_mb"]
    except (psutil.NoSuchProcess, psutil.AccessDenied) as e:
        print(f"  [WARN] Cannot snapshot PID {pid}: {e}", file=sys.stderr)
    return snap


async def load_test_worker(session, target, results, errors, stop_event):
    """Single worker: sends requests until stop_event is set."""
    while not stop_event.is_set():
        try:
            t0 = time.perf_counter()
            async with session.get(target) as resp:
                await resp.read()
            elapsed_us = (time.perf_counter() - t0) * 1_000_000
            results.append(elapsed_us)
            if resp.status >= 400:
                errors.append(1)
        except Exception:
            errors.append(1)


async def load_test(url, label, endpoint, concurrency, duration_seconds,
                    pids, ports, warmup_seconds=WARMUP_SECONDS):
    target = f"{url.rstrip('/')}/{endpoint.lstrip('/')}"
    result = BenchmarkResult(label=label, endpoint=endpoint, concurrency=concurrency)

    stop_event = asyncio.Event()
    latencies = []
    errors = []

    # Metric collector
    stop_collect = asyncio.Event()

    async def collect_metrics():
        while not stop_collect.is_set():
            for pid, port_name, port in pids:
                port_num = ports[port_name]
                snap = snapshot_process(pid, port_num)
                result.snapshots.append(snap)
            await asyncio.sleep(2)

    connector = aiohttp.TCPConnector(
        limit=concurrency,
        limit_per_host=0,
        enable_cleanup_closed=True,
        force_close=False,
    )
    timeout = aiohttp.ClientTimeout(total=30)

    async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
        collect_task = asyncio.create_task(collect_metrics())

        # Warmup: run for warmup_seconds with same concurrency
        warmup_results = []
        warmup_errors = []
        warmup_stop = asyncio.Event()
        warmup_workers = [asyncio.create_task(
            load_test_worker(session, target, warmup_results, warmup_errors, warmup_stop)
        ) for _ in range(concurrency)]

        await asyncio.sleep(warmup_seconds)
        warmup_stop.set()
        await asyncio.wait(warmup_workers, timeout=5)

        # Measurement
        workers = [asyncio.create_task(
            load_test_worker(session, target, latencies, errors, stop_event)
        ) for _ in range(concurrency)]

        await asyncio.sleep(duration_seconds)
        stop_event.set()
        await asyncio.wait(workers, timeout=10)

        stop_collect.set()
        await collect_task

    latencies.sort()
    n = len(latencies)
    if n > 0:
        result.total_requests = n + len(errors)
        result.errors = len(errors)
        result.throughput = n / duration_seconds
        result.latency_avg = sum(latencies) / n
        result.latency_p50 = latencies[int(n * 0.50)]
        result.latency_p90 = latencies[int(n * 0.90)]
        result.latency_p99 = latencies[int(n * 0.99)]
        result.latency_max = latencies[-1]

    return result


async def main():
    rust_pids = get_pids_by_port(RUST_PORT)
    tomcat_pids = get_pids_by_port(TOMCAT_PORT)
    if not rust_pids:
        print("ERROR: Rust server not running on port 18081", file=sys.stderr)
        sys.exit(1)
    if not tomcat_pids:
        print("ERROR: Tomcat server not running on port 18082", file=sys.stderr)
        sys.exit(1)

    rust_pid = rust_pids[0]
    tomcat_pid = tomcat_pids[0]
    print(f"Found Rust PID={rust_pid}, Tomcat PID={tomcat_pid}")

    concurrency_levels = [1000, 10000]
    all_results = []

    for endpoint in ENDPOINTS:
        for conc in concurrency_levels:
            print(f"\n{'='*60}")
            print(f"[Test] {endpoint} @ concurrency={conc}")
            print(f"{'='*60}")

            # Rust
            print(f"\n--- Rust: {endpoint} @ {conc} ---")
            sys.stdout.flush()
            r = await load_test(
                url=f"http://localhost:{RUST_PORT}",
                label="Rust",
                endpoint=endpoint,
                concurrency=conc,
                duration_seconds=MEASURE_SECONDS,
                pids=[(rust_pid, "rust", RUST_PORT)],
                ports={"rust": RUST_PORT, "tomcat": TOMCAT_PORT},
            )
            all_results.append(r)
            print(f"  Throughput: {r.throughput:.0f} ops/s | Errors: {r.errors}/{r.total_requests} ({r.error_rate:.1f}%)")
            print(f"  RSS: {r.avg_rss_mb:.0f} MB | Heap: {r.avg_heap_mb:.0f} MB | Threads: {r.avg_threads:.0f} | Conns: {r.avg_connections:.0f} | CPU: {r.avg_cpu:.1f}%")
            print(f"  Latency: p50={r.latency_p50:.0f} p90={r.latency_p90:.0f} p99={r.latency_p99:.0f} us")
            sys.stdout.flush()

            await asyncio.sleep(2)

            # Tomcat
            print(f"\n--- Tomcat: {endpoint} @ {conc} ---")
            sys.stdout.flush()
            r = await load_test(
                url=f"http://localhost:{TOMCAT_PORT}",
                label="Tomcat",
                endpoint=endpoint,
                concurrency=conc,
                duration_seconds=MEASURE_SECONDS,
                pids=[(tomcat_pid, "tomcat", TOMCAT_PORT)],
                ports={"rust": RUST_PORT, "tomcat": TOMCAT_PORT},
            )
            all_results.append(r)
            print(f"  Throughput: {r.throughput:.0f} ops/s | Errors: {r.errors}/{r.total_requests} ({r.error_rate:.1f}%)")
            print(f"  RSS: {r.avg_rss_mb:.0f} MB | Heap: {r.avg_heap_mb:.0f} MB | Threads: {r.avg_threads:.0f} | Conns: {r.avg_connections:.0f} | CPU: {r.avg_cpu:.1f}%")
            print(f"  Latency: p50={r.latency_p50:.0f} p90={r.latency_p90:.0f} p99={r.latency_p99:.0f} us")
            sys.stdout.flush()

            await asyncio.sleep(2)

    # ---- Output tables ----
    print("\n\n" + "=" * 120)
    print("## Rust vs Tomcat 资源指标对比")
    print("=" * 120)

    # 1. Throughput + latency
    print("\n### 1. 吞吐量与延迟")
    print()
    print("| 端点 | 并发 | 服务器 | Throughput(ops/s) | p50(us) | p90(us) | p99(us) | 错误率 |")
    print("|------|------|--------|------------------|---------|---------|---------|--------|")
    for r in all_results:
        print(f"| {r.endpoint} | {r.concurrency} | {r.label} | {r.throughput:.0f} | {r.latency_p50:.0f} | {r.latency_p90:.0f} | {r.latency_p99:.0f} | {r.error_rate:.1f}% |")

    # 2. Resource consumption
    print("\n### 2. 资源消耗指标")
    print()
    print("| 端点 | 并发 | 服务器 | RSS(MB) | Heap(MB) | 线程数 | 连接数 | CPU(%) |")
    print("|------|------|--------|---------|----------|--------|--------|--------|")
    for r in all_results:
        print(f"| {r.endpoint} | {r.concurrency} | {r.label} | {r.avg_rss_mb:.0f} | {r.avg_heap_mb:.0f} | {r.avg_threads:.0f} | {r.avg_connections:.0f} | {r.avg_cpu:.1f} |")

    # 3. Summary
    print("\n### 3. 综合平均（跨端点）")
    print()
    by_server_conc = defaultdict(list)
    for r in all_results:
        by_server_conc[(r.label, r.concurrency)].append(r)
    print("| 并发 | 服务器 | Avg Throughput | Avg RSS(MB) | Avg Heap(MB) | Avg 线程 | Avg 连接 | Avg CPU(%) |")
    print("|------|--------|----------------|-------------|--------------|----------|----------|-------------|")
    for (label, conc), results in sorted(by_server_conc.items(), key=lambda x: (x[0][1], x[0][0])):
        avg_tp = sum(r.throughput for r in results) / len(results)
        avg_rss = sum(r.avg_rss_mb for r in results) / len(results)
        avg_heap = sum(r.avg_heap_mb for r in results) / len(results)
        avg_threads = sum(r.avg_threads for r in results) / len(results)
        avg_conn = sum(r.avg_connections for r in results) / len(results)
        avg_cpu = sum(r.avg_cpu for r in results) / len(results)
        print(f"| {conc} | {label} | {avg_tp:.0f} | {avg_rss:.0f} | {avg_heap:.0f} | {avg_threads:.0f} | {avg_conn:.0f} | {avg_cpu:.1f} |")

    # 4. Error rate
    print("\n### 4. 错误率汇总")
    print()
    print("| 端点 | 并发 | Rust 错误率 | Tomcat 错误率 |")
    print("|------|------|-------------|---------------|")
    for endpoint in ENDPOINTS:
        for conc in concurrency_levels:
            rust_err = next((r.error_rate for r in all_results
                           if r.label == "Rust" and r.endpoint == endpoint and r.concurrency == conc), None)
            tomcat_err = next((r.error_rate for r in all_results
                             if r.label == "Tomcat" and r.endpoint == endpoint and r.concurrency == conc), None)
            if rust_err is not None and tomcat_err is not None:
                print(f"| {endpoint} | {conc} | {rust_err:.1f}% | {tomcat_err:.1f}% |")

    # 5. Idle baseline
    print("\n### 5. 空闲基线（无负载时）")
    print()
    for pid, name, port in [(rust_pid, "Rust", RUST_PORT), (tomcat_pid, "Tomcat", TOMCAT_PORT)]:
        snap = snapshot_process(pid, port)
        print(f"| {name} | RSS: {snap.rss_mb:.0f} MB | Heap: {snap.heap_used_mb:.0f}/{snap.heap_committed_mb:.0f} MB | Threads: {snap.threads} | CPU: {snap.cpu_percent:.1f}% | Conns: {snap.connections} |")


if __name__ == "__main__":
    asyncio.run(main())
