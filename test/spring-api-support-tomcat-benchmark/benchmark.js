/**
 * Rust vs Tomcat 并发压测脚本 (高并发稳定版)
 *
 * 端点: GET /api/health (轻), GET /api/users (中)
 * 用法: node benchmark.js
 */
const http = require('http');

const RUST_URL = 'http://localhost:18081';
const TOMCAT_URL = 'http://localhost:18082';
const CONCURRENCY_LEVELS = [1, 5, 10, 20, 50, 100, 500, 1000];
const DURATION_MS = 5000;
const HIGH_DURATION_MS = 4000;

/**
 * 对单个目标执行一轮压测 — agent 在结束后销毁
 */
function benchmark(baseUrl, endpoint, concurrency, durationMs) {
    return new Promise((resolve) => {
        const latencies = [];
        let completed = 0;
        let errors = 0;
        let running = true;
        const agent = new http.Agent({ keepAlive: true, maxSockets: concurrency + 50 });

        const { hostname, port } = parseUrl(baseUrl);
        const options = {
            hostname, port,
            path: endpoint,
            method: 'GET',
            agent,
            timeout: 30000
        };

        function worker() {
            if (!running) return;
            const start = process.hrtime.bigint();
            const req = http.request(options, (res) => {
                let body = '';
                res.on('data', chunk => body += chunk);
                res.on('end', () => {
                    if (!running) return;
                    const latencyMs = Number(process.hrtime.bigint() - start) / 1e6;
                    latencies.push(latencyMs);
                    completed++;
                    if (res.statusCode !== 200) errors++;
                    setImmediate(worker);
                });
            });
            req.on('error', () => {
                if (!running) return;
                errors++;
                completed++;
                setImmediate(worker);
            });
            req.on('timeout', () => {
                req.destroy();
                if (!running) return;
                errors++;
                completed++;
                setImmediate(worker);
            });
            req.end();
        }

        for (let i = 0; i < concurrency; i++) {
            worker();
        }

        setTimeout(() => { running = false; }, durationMs);

        setTimeout(() => {
            agent.destroy();
            if (latencies.length === 0) {
                resolve({ qps: 0, avg: 0, min: 0, max: 0, p50: 0, p95: 0, p99: 0, total: 0, errors: 0 });
                return;
            }
            latencies.sort((a, b) => a - b);
            const n = latencies.length;
            const avg = latencies.reduce((s, v) => s + v, 0) / n;
            const min = latencies[0];
            const max = latencies[n - 1];
            const qps = (completed / durationMs) * 1000;
            resolve({
                qps, avg, min, max,
                p50: percentile(latencies, 50),
                p95: percentile(latencies, 95),
                p99: percentile(latencies, 99),
                total: completed, errors
            });
        }, durationMs + 2000);
    });
}

function parseUrl(url) {
    const withoutProto = url.replace(/^http:\/\//, '');
    const colonIdx = withoutProto.lastIndexOf(':');
    return {
        hostname: withoutProto.substring(0, colonIdx),
        port: parseInt(withoutProto.substring(colonIdx + 1))
    };
}

function percentile(sorted, p) {
    const idx = Math.ceil((p / 100) * sorted.length) - 1;
    return sorted[Math.max(0, idx)];
}

async function precheck() {
    let allOk = true;
    for (const { name, url } of [{ name: 'Rust', url: RUST_URL }, { name: 'Tomcat', url: TOMCAT_URL }]) {
        try {
            const res = await fetch(url + '/api/health');
            const data = await res.json();
            console.log(`  ${name}: ${url} -> ${JSON.stringify(data)}`);
        } catch (e) {
            console.error(`  ${name}: ${url} -> FAILED - ${e.message}`);
            allOk = false;
        }
    }
    if (!allOk) {
        console.error('  服务未就绪，等待 10 秒后重试...');
        await new Promise(r => setTimeout(r, 10000));
        await precheck();
    }
}

async function runBenchmarkWithRetry(baseUrl, endpoint, concurrency, duration, label, maxRetries = 2) {
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
        const result = await benchmark(baseUrl, endpoint, concurrency, duration);
        if (result && result.total > 0) return result;
        console.log(`  [WARN] ${label} concurrency=${concurrency} attempt=${attempt} returned 0, retrying...`);
        await new Promise(r => setTimeout(r, 2000));
    }
    return null;
}

async function main() {
    console.log('╔═══════════════════════════════════════════════════════════════╗');
    console.log('║  Rust (tokio+hyper) vs Tomcat 高并发压测对比                ║');
    console.log('╚═══════════════════════════════════════════════════════════════╝');
    console.log('');

    console.log('预检服务状态:');
    await precheck();
    console.log('');

    const endpoints = [
        { name: 'GET /api/health (轻)', path: '/api/health' },
        { name: 'GET /api/users   (中)', path: '/api/users' },
    ];

    for (const ep of endpoints) {
        console.log(`──────────────────────────────────────────────────────────────`);
        console.log(`  端点: ${ep.name}`);
        console.log(`──────────────────────────────────────────────────────────────`);
        console.log(`  并发\t| Rust QPS\t| Tomcat QPS\t| 性能差\t| Rust P50\t| Tomcat P50`);
        console.log(`  ────────┼──────────────┼──────────────┼────────┼──────────────┼──────────────`);

        for (const concurrency of CONCURRENCY_LEVELS) {
            const dur = concurrency >= 500 ? HIGH_DURATION_MS : DURATION_MS;

            // Warmup — agents auto-destroyed inside benchmark()
            await benchmark(RUST_URL, ep.path, concurrency, Math.min(500, dur / 4)).catch(() => {});
            await benchmark(TOMCAT_URL, ep.path, concurrency, Math.min(500, dur / 4)).catch(() => {});

            // Real test (parallel)
            const [rustResult, tomcatResult] = await Promise.all([
                runBenchmarkWithRetry(RUST_URL, ep.path, concurrency, dur, 'Rust'),
                runBenchmarkWithRetry(TOMCAT_URL, ep.path, concurrency, dur, 'Tomcat')
            ]);

            const rustQps = rustResult ? Math.round(rustResult.qps).toString().padStart(9) : '  ERROR';
            const tomcatQps = tomcatResult ? Math.round(tomcatResult.qps).toString().padStart(9) : '  ERROR';

            let ratioStr;
            if (!rustResult || !tomcatResult || tomcatResult.qps === 0) {
                ratioStr = '  N/A';
            } else {
                const diff = ((rustResult.qps / tomcatResult.qps) - 1) * 100;
                ratioStr = (diff >= 0 ? '+' : '') + diff.toFixed(1).padStart(6) + '%';
            }

            const rustP50 = rustResult ? rustResult.p50.toFixed(2) + 'ms' : '  N/A';
            const tomcatP50 = tomcatResult ? tomcatResult.p50.toFixed(2) + 'ms' : '  N/A';

            console.log(`  ${concurrency.toString().padStart(4)}\t| ${rustQps}\t| ${tomcatQps}\t| ${ratioStr}\t| ${rustP50}\t\t| ${tomcatP50}`);

            // Recovery delay
            const recoveryMs = concurrency >= 500 ? 3000 : 1000;
            await new Promise(r => setTimeout(r, recoveryMs));
        }
        console.log('');
    }

    // Detailed reports
    for (const concurrency of [500, 1000]) {
        console.log('');
        console.log('═══════════════════════════════════════════════════════════════');
        console.log(`  详细延迟报告 (并发=${concurrency}, /api/health)`);
        console.log('═══════════════════════════════════════════════════════════════');
        const report = await detailedReport(concurrency);
        console.log(report);
    }

    console.log('');
    console.log('测试完成.');
}

async function detailedReport(concurrency) {
    const dur = 5000;
    const [rust, tomcat] = await Promise.all([
        runBenchmarkWithRetry(RUST_URL, '/api/health', concurrency, dur, 'RustDetail'),
        runBenchmarkWithRetry(TOMCAT_URL, '/api/health', concurrency, dur, 'TomcatDetail')
    ]);

    if (!rust || !tomcat) {
        return `  (部分或全部服务无响应)`;
    }

    const fmt = (n) => (typeof n === 'number' ? n.toFixed(2) : '0.00');

    return [
        `  ┌─────────────────┬────────────┬────────────┐`,
        `  │ 指标            │ Rust       │ Tomcat     │`,
        `  ├─────────────────┼────────────┼────────────┤`,
        `  │ QPS (req/s)     │ ${fmt(rust.qps).padStart(10)} │ ${fmt(tomcat.qps).padStart(10)} │`,
        `  │ 总请求数        │ ${String(rust.total).padStart(10)} │ ${String(tomcat.total).padStart(10)} │`,
        `  │ 错误数          │ ${String(rust.errors).padStart(10)} │ ${String(tomcat.errors).padStart(10)} │`,
        `  │ 平均延迟 (ms)   │ ${fmt(rust.avg).padStart(10)} │ ${fmt(tomcat.avg).padStart(10)} │`,
        `  │ 最小延迟 (ms)   │ ${fmt(rust.min).padStart(10)} │ ${fmt(tomcat.min).padStart(10)} │`,
        `  │ 最大延迟 (ms)   │ ${fmt(rust.max).padStart(10)} │ ${fmt(tomcat.max).padStart(10)} │`,
        `  │ P50 延迟 (ms)   │ ${fmt(rust.p50).padStart(10)} │ ${fmt(tomcat.p50).padStart(10)} │`,
        `  │ P95 延迟 (ms)   │ ${fmt(rust.p95).padStart(10)} │ ${fmt(tomcat.p95).padStart(10)} │`,
        `  │ P99 延迟 (ms)   │ ${fmt(rust.p99).padStart(10)} │ ${fmt(tomcat.p99).padStart(10)} │`,
        `  └─────────────────┴────────────┴────────────┘`,
    ].join('\n');
}

main().catch(console.error);
