const { chromium } = require('playwright');

/**
 * 模拟真人点击，联调前后端
 * 流程：首页 → 识别库下拉 → API设置打开/取消 → 厂家设置新标签 → 功能卡片导航 → API报告
 */
(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: { width: 1920, height: 1080 } });
  const page = await context.newPage();

  // ── 监听 API ──
  const apiCalls = [];
  page.on('request', req => {
    if (req.url().includes('/v2/ai/')) {
      apiCalls.push({ url: req.url(), method: req.method(), status: null });
    }
  });
  page.on('response', async res => {
    if (!res.url().includes('/v2/ai/')) return;
    const e = apiCalls.find(x => x.url === res.url() && x.status === null);
    if (e) {
      e.status = res.status();
      try { e.body = (await res.text()).substring(0, 300); } catch (_) {}
    }
  });

  let errors = [];
  page.on('pageerror', err => errors.push(err.message.substring(0, 200)));

  const log = m => console.log(m);
  const scr = async name => {
    await page.screenshot({ path: `H:\\workspace\\2\\spring-support-api-parent\\test\\spring-api-support-ai-test-starter\\${name}.png`, fullPage: true });
  };

  log('═══════════════════════════════════');
  log('  模拟真人点击 · 联调前后端');
  log('═══════════════════════════════════');

  // ═══ 1. 打开首页 ═══
  log('\n[1/5] 打开首页');
  await page.goto('http://localhost:5173/', { waitUntil: 'networkidle', timeout: 20000 });
  await page.waitForTimeout(2000);

  const t = await page.evaluate(() => document.body.innerText);
  ['AI 工作台', '聊天', '文生图', '图像上色', '视频生成', '识别库', 'API 设置', '厂家设置'].forEach(item =>
    log(`  ${item}: ${t.includes(item) ? '✓' : '✗'}`)
  );
  log(`  JS错误: ${errors.length === 0 ? '无' : errors.join(' | ')}`);
  await scr('01-home');

  // ═══ 2. 识别库下拉 ═══
  log('\n[2/5] 识别库下拉');
  await page.locator('button:has-text("识别库")').click({ force: true });
  await page.waitForTimeout(400);
  const items = await page.locator('.ai-home__dropdown-item').allTextContents();
  items.forEach(i => log(`  ▸ ${i.trim().replace(/\s+/g, ' ')}`));
  await page.locator('h1:has-text("AI 工作台")').click({ force: true });
  await page.waitForTimeout(300);

  // ═══ 3. API 设置 → 打开 → 验证 → 取消 ═══
  log('\n[3/5] API 设置');
  await page.locator('button:has-text("API 设置")').click({ force: true });
  await page.waitForTimeout(2500);

  const vis = await page.locator('.newapi-dialog').isVisible().catch(() => false);
  log(`  对话框可见: ${vis ? '✓' : '✗'}`);
  if (vis) {
    const sw = await page.locator('.newapi-dialog .el-switch').count();
    const host = await page.locator('.newapi-dialog input[placeholder="0.0.0.0"]').isVisible().catch(() => false);
    const port = await page.locator('.newapi-dialog .el-input-number').isVisible().catch(() => false);
    log(`  开关: ${sw}个, 监听地址: ${host ? '✓' : '✗'}, 端口: ${port ? '✓' : '✗'}`);

    await page.locator('.newapi-dialog .el-button:has-text("取消")').click({ timeout: 3000 }).catch(() => {});
    await page.waitForTimeout(800);
    for (let i = 0; i < 20 && await page.locator('.el-overlay').isVisible().catch(() => false); i++)
      await page.waitForTimeout(200);
    const still = await page.locator('.newapi-dialog').isVisible().catch(() => false);
    log(`  取消后关闭: ${!still ? '✓' : '✗'}`);
  }
  await scr('02-api-dialog');

  // ═══ 4. 厂家设置 → 新标签页 ═══
  log('\n[4/5] 厂家设置');
  let vendorPage = null;
  context.on('page', p => { vendorPage = p; });
  await page.locator('button:has-text("厂家设置")').click({ force: true });
  await page.waitForTimeout(3000);

  if (vendorPage) {
    await vendorPage.waitForLoadState('domcontentloaded', { timeout: 10000 }).catch(() => {});
    await vendorPage.waitForTimeout(2000);
    const url = vendorPage.url();
    const vt = await vendorPage.evaluate(() => document.body.innerText.substring(0, 400));
    log(`  新标签页URL: ${url}`);
    log(`  内容: ${vt.replace(/\n/g, ' | ')}`);
    const ok = vt.includes('厂家与模型中心') && !vt.includes('AI 工作台');
    log(`  厂家页渲染: ${ok ? '✓' : '✗ (可能还是首页)'}`);
    await vendorPage.screenshot({ path: 'H:\\workspace\\2\\spring-support-api-parent\\test\\spring-api-support-ai-test-starter\\03-vendor.png', fullPage: true });
    log('  截图: 03-vendor.png');
    await vendorPage.close();
  } else {
    log('  ✗ 未检测到新标签页');
  }

  // ═══ 5. 功能卡片 + API 报告 ═══
  log('\n[5/5] 功能卡片导航');
  const cards = await page.locator('.ai-home__card-body strong').allTextContents();
  log(`  卡片(${cards.length}): ${cards.join(', ')}`);

  // 点击聊天
  await page.locator('.ai-home__card').first().click({ force: true });
  await page.waitForTimeout(2000);
  log(`  点击"${cards[0]}" → ${page.url()}`);

  // 返回首页
  await page.goBack({ waitUntil: 'domcontentloaded', timeout: 10000 }).catch(() => {});
  await page.waitForTimeout(1000);
  await scr('04-final');

  // ── API 报告 ──
  log('\n══════ API 报告 ══════');
  if (apiCalls.length === 0) {
    log('  (无 /v2/ai/ 请求)');
  } else {
    let allOk = true;
    apiCalls.forEach((c, i) => {
      const ok = c.status && c.status < 400;
      if (!ok) allOk = false;
      log(`  [${i + 1}] ${c.method} ${c.url.replace(/http:\/\/[^/]+/, '')} → ${c.status} ${ok ? '✓' : '✗'}`);
    });
    log(`\n  全部成功: ${allOk ? '✓' : '✗'}`);
  }
  log(`  JS错误: ${errors.length}`);
  errors.forEach((e, i) => log(`    ${i + 1}: ${e.substring(0, 150)}`));

  // ── 后端 API 直连验证 ──
  log('\n══════ 后端直连验证 ══════');
  const { execSync } = require('child_process');
  const endpoints = [
    '/v2/ai/global-setting/list?sysAiGlobalSettingGroup=runtime',
    '/v2/ai/module/list',
    '/v2/ai/app-key/list',
    '/v2/ai/manufacturer/list',
  ];
  for (const ep of endpoints) {
    try {
      const out = execSync(`curl.exe -s "http://localhost:58084${ep}"`, { encoding: 'utf8', timeout: 5000 });
      const code = out.includes('"code" : "00000"') ? '00000' : 'FAIL';
      log(`  ${ep} → ${code}`);
    } catch (e) {
      log(`  ${ep} → ERROR: ${e.message.substring(0, 80)}`);
    }
  }

  log('\n══════ DONE ══════');
  await browser.close();
})();
