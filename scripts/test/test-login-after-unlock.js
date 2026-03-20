const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();

  try {
    console.log('测试登录（锁定已解除）...');

    await page.goto('http://127.0.0.1:8848');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // OAuth登录
    const usernameInput = await page.locator('input[type="text"]').first();
    const passwordInput = await page.locator('input[type="password"]').first();

    await usernameInput.fill('sa');
    await passwordInput.fill('admin123');

    const loginButton = await page.locator('button:has-text("登录")').first();
    await loginButton.click();

    await page.waitForTimeout(5000);

    if (page.url().includes('home')) {
      console.log('✅ OAuth登录成功！');
      await page.screenshot({ path: 'test-login-after-unlock.png' });
    } else {
      console.log('❌ 登录失败');
      const errorMsg = await page.locator('.el-message').textContent().catch(() => '');
      console.log('错误信息:', errorMsg);
      await page.screenshot({ path: 'test-login-failed.png' });
    }

    await page.waitForTimeout(5000);

  } catch (error) {
    console.error('测试出错:', error.message);
  } finally {
    await browser.close();
  }
})();
