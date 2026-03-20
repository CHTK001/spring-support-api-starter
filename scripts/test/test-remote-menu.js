const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({
    headless: false,
    slowMo: 300
  });
  const context = await browser.newContext();
  const page = await context.newPage();

  // 监听网络请求
  page.on('request', request => {
    if (request.url().includes('menu') || request.url().includes('login')) {
      console.log(`[请求] ${request.method()} ${request.url()}`);
    }
  });

  page.on('response', async response => {
    if (response.url().includes('menu')) {
      console.log(`[响应] ${response.status()} ${response.url()}`);
      try {
        const body = await response.text();
        console.log(`[菜单数据]:`, body.substring(0, 300));
      } catch (e) {}
    }
  });

  try {
    console.log('='.repeat(60));
    console.log('测试远程菜单功能');
    console.log('='.repeat(60));

    // 1. 登录
    console.log('\n[步骤1] 登录系统...');
    await page.goto('http://127.0.0.1:8848');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const usernameInput = await page.locator('input[type="text"]').first();
    const passwordInput = await page.locator('input[type="password"]').first();

    await usernameInput.fill('sa');
    await passwordInput.fill('admin123');

    const loginButton = await page.locator('button:has-text("登录")').first();
    await loginButton.click();

    console.log('   等待登录响应...');
    await page.waitForTimeout(5000);

    if (page.url().includes('home') || page.url().includes('dashboard')) {
      console.log('   ✅ 登录成功！');
      await page.screenshot({ path: 'test-remote-menu-login.png' });

      // 2. 等待菜单加载
      console.log('\n[步骤2] 等待远程菜单加载...');
      await page.waitForTimeout(3000);

      // 3. 检查菜单
      console.log('\n[步骤3] 检查菜单结构...');
      const menuItems = await page.locator('.el-menu-item, .el-sub-menu').all();
      console.log(`   发现 ${menuItems.length} 个菜单项`);

      // 截图菜单
      await page.screenshot({ path: 'test-remote-menu-structure.png', fullPage: true });
      console.log('   ✅ 菜单截图已保存');

      // 4. 尝试点击几个菜单
      console.log('\n[步骤4] 测试菜单导航...');

      const testMenus = ['系统管理', '用户管理', '角色管理', '菜单管理'];
      for (const menuName of testMenus) {
        try {
          const menu = page.locator(`text="${menuName}"`).first();
          if (await menu.count() > 0 && await menu.isVisible()) {
            console.log(`   点击菜单: ${menuName}`);
            await menu.click();
            await page.waitForTimeout(1500);
            console.log(`   ✅ ${menuName} - URL: ${page.url()}`);
            await page.screenshot({ path: `test-menu-${menuName}.png` });
          }
        } catch (e) {
          console.log(`   ⚠️ ${menuName} - ${e.message}`);
        }
      }

      console.log('\n' + '='.repeat(60));
      console.log('✅ 远程菜单测试完成！');
      console.log('='.repeat(60));

    } else {
      console.log('   ❌ 登录失败');
      console.log('   当前URL:', page.url());
      await page.screenshot({ path: 'test-remote-menu-login-failed.png' });
    }

    await page.waitForTimeout(5000);

  } catch (error) {
    console.error('\n❌ 测试出错:', error.message);
    await page.screenshot({ path: 'test-remote-menu-error.png', fullPage: true });
  } finally {
    await browser.close();
    console.log('\n测试结束');
  }
})();
