/**
 * 极高峰并发压测 (5000, 10000)
 * 用法: node benchmark-extreme.js
 */
const http = require('http');

const RUST_URL = 'http://localhost:18081';
const TOMCAT_URL = 'http://localhost:18082';

function parseUrl(url) {
    const s = url.replace(/^http:\/\//, '');
    const i = s.lastIndexOf(':');
    return { hostname: s.substring(0, i), port: parseInt(s.substring(i + 1)) };
}

function benchmark(baseUrl, endpoint, concurrency, durationMs) {
    return new Promise((resolve) => {
        const latencies = [];
        let completed = 0, errors = 0, running = true;
        const agent = new http.Agent({ keepAlive: true, maxSockets: concurrency + 100 });
        const { hostname, port } = parseUrl(baseUrl);
        const opts = { hostname, port, path: endpoint, method: 'GET', agent, timeout: 60000 };

        function worker() {
            if (!running) return;
            const start = process.hrtime.bigint();
            const req = http.request(opts, (res) => {
                let body = '';
                res.on('data', c => body += c);
                res.on('end', () => {
                    if (!running) return;
                    latencies.push(Number(process.hrtime.bigint() - start) / 1e6);
                    completed++;
                    if (res.statusCode !== 200) errors++;
                    setImmediate(worker);
                });
            });
            req.on('error', () => { if (!running) { return; } errors++; completed++; setImmediate(worker); });
            req.on('timeout', () => { req.destroy(); if (!running) { return; } errors++; completed++; setImmediate(worker); });
            req.end();
        }
        for (let i = 0; i < concurrency; i++) worker();
        setTimeout(() => { running = false; }, durationMs);
        setTimeout(() => {
            agent.destroy();
            if (latencies.length === 0) return resolve(null);
            latencies.sort((a, b) => a - b);
            const n = latencies.length;
            resolve({
                qps: (completed / durationMs) * 1000,
                avg: latencies.reduce((s, v) => s + v, 0) / n,
                min: latencies[0], max: latencies[n - 1],
                p50: latencies[Math.max(0, Math.ceil(0.5 * n) - 1)],
                p95: latencies[Math.max(0, Math.ceil(0.95 * n) - 1)],
                p99: latencies[Math.max(0, Math.ceil(0.99 * n) - 1)],
                total: completed, errors
            });
        }, durationMs + 5000);
    });
}

async function main() {
    const levels = [5000, 10000];

    console.log('═══ 极高峰并发压测 (Rust vs Tomcat) ═══');
    console.log('');

    for (const ep of ['/api/health', '/api/users']) {
        console.log(`--- ${ep} ---`);
        for (const concurrency of levels) {
            const dur = concurrency >= 5000 ? 3000 : 4000;
            console.log(`\n并发=${concurrency} (测试时长=${dur}ms)`);

            // Warmup
            await benchmark(RUST_URL, ep, concurrency, 1000).catch(() => {});
            await new Promise(r => setTimeout(r, 2000));

            const r = await benchmark(RUST_URL, ep, concurrency, dur);
            const t = await benchmark(TOMCAT_URL, ep, concurrency, dur);

            if (!r || !t) {
                console.log(`  结果无效: Rust=${r ? 'OK' : 'FAIL'} Tomcat=${t ? 'OK' : 'FAIL'}`);
                continue;
            }

            const diff = ((r.qps / t.qps) - 1) * 100;
            console.log(`  Rust QPS: ${Math.round(r.qps)} | Tomcat QPS: ${Math.round(t.qps)} | 性能差: ${diff >= 0 ? '+' : ''}${diff.toFixed(1)}%`);
            console.log(`  Rust P50/P95/P99: ${r.p50.toFixed(1)}/${r.p95.toFixed(1)}/${r.p99.toFixed(1)}ms`);
            console.log(`  Tomcat P50/P95/P99: ${t.p50.toFixed(1)}/${t.p95.toFixed(1)}/${t.p99.toFixed(1)}ms`);
            console.log(`  Rust 总请求: ${r.total} | 错误: ${r.errors}`);
            console.log(`  Tomcat 总请求: ${t.total} | 错误: ${t.errors}`);

            await new Promise(r => setTimeout(r, 5000));
        }
    }
    console.log('\n测试完成.');
}

main().catch(console.error);
