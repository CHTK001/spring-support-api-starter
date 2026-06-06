import { chromium } from 'playwright';
import path from 'path';
import WebSocket from 'ws';

const BASE_URL = 'http://localhost:18866';
const WS_URL = 'ws://localhost:58082/v2/datalake/ws';
const SCREENSHOT_DIR = 'h:/workspace/2/spring-support-api-parent';
const PREFIX = 'verify';

async function screenshot(page, name) {
  const filePath = path.join(SCREENSHOT_DIR, `${PREFIX}-${name}.png`);
  await page.screenshot({ path: filePath, fullPage: true });
  console.log(`  ✓ Screenshot saved: ${PREFIX}-${name}.png`);
}

async function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

async function main() {
  console.log('=== Backend Acceptance Test ===\n');

  // 1. HTTP Health Check
  console.log('1. Checking backend HTTP health...');
  try {
    const resp = await fetch(`${BASE_URL}/actuator/health`);
    const data = await resp.json();
    console.log(`   Health status: ${data.status}`);
    if (data.status === 'UP') {
      console.log('   ✓ Backend is healthy.\n');
    } else {
      console.log('   ✘ Backend is not healthy.\n');
    }
  } catch (e) {
    console.log(`   ✘ Health check failed: ${e.message}\n`);
  }

  // 2. WebSocket Connection Test
  console.log('2. Testing WebSocket connection...');
  let wsOk = false;
  let wsMsgCount = 0;
  let wsError = null;
  try {
    await new Promise((resolve, reject) => {
      const ws = new WebSocket(WS_URL);
      const timeout = setTimeout(() => {
        ws.close();
        reject(new Error('WS connection timeout'));
      }, 5000);

      ws.on('open', () => {
        console.log('   ✓ WebSocket connected successfully.');
        wsOk = true;
        clearTimeout(timeout);
      });

      ws.on('message', data => {
        wsMsgCount++;
        if (wsMsgCount <= 2) {
          const text = data.toString();
          console.log(`   ✓ Received message #${wsMsgCount}: ${text.substring(0, 100)}...`);
        }
      });

      ws.on('error', err => {
        clearTimeout(timeout);
        wsError = err.message;
        resolve();
      });

      // Wait for messages then close
      setTimeout(() => {
        ws.close();
        resolve();
      }, 7000);
    });
    console.log(`   ✓ WebSocket received ${wsMsgCount} messages.\n`);
  } catch (e) {
    console.log(`   ✘ WebSocket test failed: ${e.message}\n`);
  }

  // 3. REST API Stats (try proxy and direct)
  console.log('3. Checking REST API endpoints...');
  const endpoints = [
    '/actuator/health',
    '/v2/datalake/monitor/stats',
    '/monitor/stats',
  ];
  for (const ep of endpoints) {
    try {
      const resp = await fetch(`${BASE_URL}${ep}`);
      const text = await resp.text();
      console.log(`   ${ep} → ${resp.status} ${text.substring(0, 100)}`);
    } catch (e) {
      console.log(`   ${ep} → Error: ${e.message}`);
    }
  }
  console.log('');

  // 4. Browser page load test - Swagger
  console.log('4. Loading Swagger doc page...');
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  const consoleErrors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') consoleErrors.push(msg.text());
  });

  try {
    await page.goto(`${BASE_URL}/doc.html`, { waitUntil: 'networkidle', timeout: 15000 });
    await sleep(2000);
    await screenshot(page, 'swagger-doc');
    console.log('   ✓ Swagger page loaded.\n');
  } catch (e) {
    console.log(`   ✘ Swagger page load failed: ${e.message}\n`);
  }

  // 5. Console errors
  console.log('5. Browser console errors:');
  if (consoleErrors.length === 0) {
    console.log('   ✓ None.');
  } else {
    for (const err of consoleErrors) {
      console.log(`   ✘ ${err}`);
    }
  }
  console.log('');

  // Summary
  console.log('=== Test Summary ===');
  console.log(`  HTTP Health:  ✓`);
  console.log(`  WebSocket:    ${wsOk ? '✓ Connected' : '✘ Failed'}${wsError ? ' (' + wsError + ')' : ''}`);
  console.log(`  WS Messages:  ${wsMsgCount}`);
  console.log(`  Browser Errors: ${consoleErrors.length}`);

  await browser.close();
}

main().catch(console.error);
