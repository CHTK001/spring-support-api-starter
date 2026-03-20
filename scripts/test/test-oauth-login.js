const { chromium } = require('playwright');
const crypto = require('crypto');

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

    console.log('等待登录表单出现...');
    await page.waitForTimeout(2000);

    // 查找用户名和密码输入框
    const usernameInput = await page.locator('input[type="text"], input[placeholder*="用户"], input[placeholder*="账号"]').first();
    const passwordInput = await page.locator('input[type="password"]').first();

    if (await usernameInput.count() > 0 && await passwordInput.count() > 0) {
      console.log('找到登录表单，填写账号密码...');

      await usernameInput.fill('sa');
      await passwordInput.fill('admin123');

      console.log('点击登录按钮...');
      const loginButton = await page.locator('button:has-text("登录"), button:has-text("Login")').first();
      await loginButton.click();

      console.log('等待登录响应...');
      await page.waitForTimeout(5000);

      console.log('当前URL:', page.url());

      // 检查是否登录成功（URL变化或有成功提示）
      if (page.url().includes('home') || page.url().includes('dashboard') || page.url().includes('index')) {
        console.log('✅ OAuth登录成功！');
      } else {
        console.log('⚠️ 登录后URL未变化，检查是否有错误提示...');
        const errorMsg = await page.locator('.el-message--error').textContent().catch(() => null);
        if (errorMsg) {
          console.log('错误提示:', errorMsg);
        }
      }

      // 截图
      await page.screenshot({ path: 'oauth-login-result.png', fullPage: true });
      console.log('截图已保存: oauth-login-result.png');

      await page.waitForTimeout(3000);
    } else {
      console.log('❌ 未找到登录表单');
      await page.screenshot({ path: 'oauth-login-no-form.png', fullPage: true });
    }

  } catch (error) {
    console.error('❌ 测试失败:', error.message);
    await page.screenshot({ path: 'oauth-login-error.png', fullPage: true });
  } finally {
    await browser.close();
  }
})();
