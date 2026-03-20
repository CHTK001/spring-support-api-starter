const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();

  try {
    console.log('正在打开登录页面...');
    await page.goto('http://127.0.0.1:8848');
    await page.waitForLoadState('networkidle');

    console.log('等待静态登录卡片出现...');
    await page.waitForSelector('.option-card--static', { timeout: 10000 });

    console.log('点击静态登录卡片...');
    await page.click('.option-card--static');

    console.log('等待页面跳转...');
    await page.waitForURL(/\/(home|dashboard|index)/, { timeout: 15000 });

    console.log('✅ 静态登录成功！');
    console.log('当前URL:', page.url());

    // 截图
    await page.screenshot({ path: 'static-login-success.png', fullPage: true });
    console.log('截图已保存: static-login-success.png');

    // 等待5秒让用户看到结果
    await page.waitForTimeout(5000);

  } catch (error) {
    console.error('❌ 静态登录失败:', error.message);
    await page.screenshot({ path: 'static-login-error.png', fullPage: true });
    console.log('错误截图已保存: static-login-error.png');
  } finally {
    await browser.close();
  }
})();
