/**
 * Playwright fetch helper - invoked by SpiderRuntimeEngine
 * Usage: node playwright-fetch.js <url> <timeout_seconds> [cookie_json] [header_json] [loginConfig_json] [username] [password]
 * Supports CDP connection to real Chrome (env CDP_PORT=9222) for captcha-protected sites.
 * Outputs JSON to stdout: { url, title, html, statusCode, screenshot (base64), cookies, error? }
 */
const args = process.argv.slice(2);
const url = args[0];
const timeoutSeconds = parseInt(args[1], 10) || 30;
const cookieJsonRaw = args[2] || '{}';
const headerJsonRaw = args[3] || '{}';
const loginConfigRaw = args[4] || '';
const rawUsername = args[5] || '';
const rawPassword = args[6] || '';

function safeParseJson(raw) {
  if (!raw || raw === '{}') return {};
  try { return JSON.parse(raw); } catch (_) { return {}; }
}

/**
 * 执行声明式登录步骤
 * loginConfig: { loginUrl, waitAfterLogin, steps: [{ action, selector, value }] }
 * 支持 action: click, fill, type, wait, select
 */
async function executeLoginSteps(page, loginConfig, username, password) {
  if (!loginConfig || !loginConfig.steps || loginConfig.steps.length === 0) return;

  // 导航到登录页
  if (loginConfig.loginUrl) {
    console.error('[playwright-fetch] login: navigating to ' + loginConfig.loginUrl);
    await page.goto(loginConfig.loginUrl, { waitUntil: 'domcontentloaded', timeout: timeoutSeconds * 1000 });
    try { await page.waitForLoadState('networkidle', { timeout: 10000 }); } catch (_) {}
  }

  // 逐步执行
  for (let i = 0; i < loginConfig.steps.length; i++) {
    const step = loginConfig.steps[i];
    let value = (step.value || '').replace(/\{username\}/g, username).replace(/\{password\}/g, password);
    console.error('[playwright-fetch] login step ' + (i + 1) + ': ' + step.action + (step.selector ? ' ' + step.selector : ''));

    switch (step.action) {
      case 'click':
        await page.waitForSelector(step.selector, { state: 'visible', timeout: 10000 });
        await page.click(step.selector);
        break;
      case 'fill':
        await page.waitForSelector(step.selector, { state: 'visible', timeout: 10000 });
        await page.fill(step.selector, value);
        break;
      case 'type':
        await page.waitForSelector(step.selector, { state: 'visible', timeout: 10000 });
        await page.type(step.selector, value, { delay: 50 });
        break;
      case 'wait':
        await page.waitForTimeout(parseInt(value, 10) || 1000);
        break;
      case 'select':
        await page.waitForSelector(step.selector, { state: 'visible', timeout: 10000 });
        await page.selectOption(step.selector, value);
        break;
      default:
        console.error('[playwright-fetch] login: unknown action "' + step.action + '"');
    }
    // 每步之间短暂等待，让页面响应
    await page.waitForTimeout(300);
  }

  // 登录后等待
  const waitMs = loginConfig.waitAfterLogin || 3000;
  console.error('[playwright-fetch] login: waiting ' + waitMs + 'ms after login');
  await page.waitForTimeout(waitMs);
}

(async () => {
  let chromium;
  try {
    chromium = require('playwright').chromium;
  } catch (e) {
    console.log(JSON.stringify({ error: 'playwright require failed: ' + e.message }));
    process.exit(1);
  }

  // Try CDP connection first (real Chrome browser, bypasses captcha)
  const cdpPort = process.env.CDP_PORT || '9222';
  let browser = null;
  try {
    browser = await Promise.race([
      chromium.connectOverCDP('http://127.0.0.1:' + cdpPort),
      new Promise((_, reject) => setTimeout(() => reject(new Error('CDP connect timeout')), 5000))
    ]);
    console.error('[playwright-fetch] Connected to real Chrome via CDP');
  } catch (cdpErr) {
    console.error('[playwright-fetch] CDP connect failed, launching headless: ' + cdpErr.message);
  }

  if (!browser) {
    browser = await Promise.race([
      chromium.launch({
        headless: true,
        args: [
          '--disable-blink-features=AutomationControlled',
          '--no-sandbox',
          '--disable-infobars',
          '--disable-dev-shm-usage',
          '--disable-gpu',
          '--window-size=1920,1080',
        ],
      }),
      new Promise((_, reject) => setTimeout(() => reject(new Error('headless launch timeout')), 30000))
    ]);
  }

  let context = null;
  let page = null;
  try {
    const isCdpMode = process.env.CDP_PORT && browser.contexts && browser.contexts().length > 0;
    if (isCdpMode) {
      context = browser.contexts()[0];
      const existingPages = context.pages();
      const targetHost = new URL(url).hostname;
      let reusePage = null;
      for (const p of existingPages) {
        try {
          const pUrl = p.url();
          if (pUrl && pUrl.includes(targetHost)) {
            reusePage = p;
            console.error('[playwright-fetch] CDP: found existing page at ' + pUrl);
            break;
          }
        } catch (_) { /* ignore tab-less pages */ }
      }
      if (reusePage) {
        page = reusePage;
        const currentUrl = page.url();
        if (currentUrl === url || decodeURIComponent(currentUrl) === decodeURIComponent(url)) {
          console.error('[playwright-fetch] CDP: page already at target URL, reusing without navigation');
        } else {
          console.error('[playwright-fetch] CDP: navigating from ' + currentUrl + ' to ' + url);
          await Promise.race([
            page.goto(url, { waitUntil: 'domcontentloaded', timeout: timeoutSeconds * 1000 }),
            new Promise((_, reject) => setTimeout(() => reject(new Error('goto timeout')), timeoutSeconds * 1000 + 5000))
          ]);
          try {
            await page.waitForLoadState('networkidle', { timeout: 10000 });
          } catch (e) { /* ignore */ }
        }
      } else {
        console.error('[playwright-fetch] CDP: no matching page, creating new page');
        page = await context.newPage();
        // CDP 模式：如有登录配置，先执行登录再导航到目标页
        const loginConfig = safeParseJson(loginConfigRaw);
        if (loginConfig.steps && loginConfig.steps.length > 0) {
          await executeLoginSteps(page, loginConfig, rawUsername, rawPassword);
        }
        await Promise.race([
          page.goto(url, { waitUntil: 'domcontentloaded', timeout: timeoutSeconds * 1000 }),
          new Promise((_, reject) => setTimeout(() => reject(new Error('goto timeout')), timeoutSeconds * 1000 + 5000))
        ]);
        try {
          await page.waitForLoadState('networkidle', { timeout: 10000 });
        } catch (e) { /* ignore */ }
      }
    } else {
      // Launched (headless) mode: create context with cookies and headers
      context = await browser.newContext({
        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36',
        viewport: { width: 1920, height: 1080 },
        locale: 'zh-CN',
        timezoneId: 'Asia/Shanghai',
        extraHTTPHeaders: safeParseJson(headerJsonRaw),
      });

      const cookies = safeParseJson(cookieJsonRaw);
      try {
        const urlObj = new URL(url);
        for (const [name, value] of Object.entries(cookies)) {
          await context.addCookies([{ name, value, domain: urlObj.hostname, path: '/' }]);
        }
      } catch (e) { /* ignore cookie errors */ }

      page = await context.newPage();
      page.setDefaultTimeout(timeoutSeconds * 1000);

      // Stealth: override automation detection
      await page.addInitScript(() => {
        Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
        Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
        Object.defineProperty(navigator, 'languages', { get: () => ['zh-CN', 'zh'] });
        window.chrome = { runtime: {} };
        const origQuery = window.navigator.permissions.query;
        window.navigator.permissions.query = (params) => (
          params.name === 'notifications' ? Promise.resolve({ state: 'prompt' }) : origQuery(params)
        );
      });

      // 如有登录配置，先执行登录再导航到目标页
      const loginConfig = safeParseJson(loginConfigRaw);
      if (loginConfig.steps && loginConfig.steps.length > 0) {
        await executeLoginSteps(page, loginConfig, rawUsername, rawPassword);
      }

      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: timeoutSeconds * 1000 });
      try {
        await page.waitForLoadState('networkidle', { timeout: 10000 });
      } catch (e) { /* ignore */ }
    }

    // --- Common: extract page content (runs for both CDP and launched modes) ---
    const resultUrl = page.url();
    const title = await page.title();
    const html = await page.content();
    let screenshotBase64 = '';
    try {
      screenshotBase64 = (await page.screenshot()).toString('base64');
    } catch (e) { /* ignore */ }

    // 提取 cookies 用于持久化登录态
    let cookies = [];
    try {
      if (context) {
        cookies = await context.cookies();
      }
    } catch (_) {}

    console.log(JSON.stringify({
      url: resultUrl,
      title: title,
      html: html,
      statusCode: 200,
      screenshot: screenshotBase64,
      cookies: cookies,
    }));
  } finally {
    // Cleanup: close page but NOT browser in CDP mode (shared Chrome instance)
    try { if (page) await page.close(); } catch (_) {}
    try { if (context && !process.env.CDP_PORT) await context.close(); } catch (_) {}
    if (browser && !process.env.CDP_PORT) {
      await browser.close();
    }
    if (process.env.CDP_PORT) {
      process.exit(0);
    }
  }
})().catch(err => {
  console.log(JSON.stringify({ error: err.message }));
  process.exit(1);
});
