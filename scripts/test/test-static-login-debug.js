const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();

  // 监听控制台消息
  page.on('console', msg => {
    console.log(`[浏览器控制台 ${msg.type()}]:`, msg.text());
  });

  // 监听网络请求
  page.on('request', request => {
    if (request.url().includes('login')) {
      console.log(`[请求] ${request.method()} ${request.url()}`);
    }
  });

  page.on('response', async response => {
    if (response.url().includes('login')) {
      console.log(`[响应] ${response.status()} ${response.url()}`);
      try {
        const body = await response.text();
        console.log(`[响应内容]:`, body.substring(0, 500));
      } catch (e) {}
    }
  });

  try {
    console.log('正在打开登录页面...');
    await page.goto('http://127.0.0.1:8848');
    await page.waitForLoadState('networkidle');

    console.log('等待静态登录卡片出现...');
    const staticCard = await page.waitForSelector('.option-card--static', { timeout: 10000 });
    console.log('静态登录卡片已找到');

    console.log('点击静态登录卡片...');
    await staticCard.click();

    console.log('等待3秒观察反应...');
    await page.waitForTimeout(3000);

    console.log('当前URL:', page.url());

    // 检查是否有错误提示
    const errorMsg = await page.locator('.el-message--error').textContent().catch(() => null);
    if (errorMsg) {
      console.log('错误提示:', errorMsg);
    }

    // 截图
    await page.screenshot({ path: 'static-login-debug.png', fullPage: true });
    console.log('调试截图已保存');

    await page.waitForTimeout(5000);

  } catch (error) {
    console.error('❌ 测试失败:', error.message);
    await page.screenshot({ path: 'static-login-error2.png', fullPage: true });
  } finally {
    await browser.close();
  }
})();
