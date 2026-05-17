/**
 * 极高并发压测 (单服务器顺序执行, 避免 Node.js 客户端过载)
 */
const http = require('http');

function parseUrl(url) {
    const s = url.replace(/^http:\/\//, '');
    const i = s.lastIndexOf(':');
    return { hostname: s.substring(0, i), port: parseInt(s.substring(i + 1)) };
}

function benchmark(baseUrl, endpoint, concurrency, durationMs) {
    return new Promise((resolve) => {
        const latencies = [];
        let completed = 0, errors = 0, running = true;
        const agent = new http.Agent({ keepAlive: true, maxSockets: Math.min(concurrency, 3000) });
        const { hostname, port } = parseUrl(baseUrl);
        const opts = { hostname, port, path: endpoint, method: 'GET', agent, timeout: 120000 };

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
        const actualWorkers = Math.min(concurrency, 3000);
        for (let i = 0; i < actualWorkers; i++) worker();
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
                total: completed, errors,
                actualWorkers
            });
        }, durationMs + 8000);
    });
}

async function testOne(name, url, ep, concurrency) {
    console.log(`\n测试 ${name}  concurrency=${concurrency}  ${ep}`);
    const r = await benchmark(url, ep, concurrency, 5000);
    if (!r) {
        console.log(`  ${name}: 失败 (无响应)`);
        return null;
    }
    console.log(`  ${name}: QPS=${Math.round(r.qps)}  total=${r.total}  err=${r.errors}`);
    console.log(`    延迟: avg=${r.avg.toFixed(1)}  P50=${r.p50.toFixed(1)}  P95=${r.p95.toFixed(1)}  P99=${r.p99.toFixed(1)}ms`);
    return r;
}

async function main() {
    const levels = [3000, 5000, 8000];
    const ep = '/api/health';

    console.log('═══ 极高并发压测 (逐服务器顺序执行) ═══\n');

    for (const concurrency of levels) {
        console.log(`\n══════════ 并发=${concurrency} ══════════`);

        const rust = await testOne('Rust', 'http://localhost:18081', ep, concurrency);
        await new Promise(r => setTimeout(r, 3000));
        const tomcat = await testOne('Tomcat', 'http://localhost:18082', ep, concurrency);

        if (rust && tomcat && tomcat.qps > 0) {
            const diff = ((rust.qps / tomcat.qps) - 1) * 100;
            console.log(`  >>> Rust vs Tomcat: ${diff >= 0 ? '+' : ''}${diff.toFixed(1)}%`);
        } else if (rust && !tomcat) {
            console.log(`  >>> Tomcat 无响应, Rust QPS=${Math.round(rust.qps)}`);
        }

        await new Promise(r => setTimeout(r, 5000));
    }

    console.log('\n测试完成.');
}

main().catch(console.error);
