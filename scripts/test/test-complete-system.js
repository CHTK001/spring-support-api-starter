const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({
    headless: false,
    slowMo: 500  // 慢速执行，方便观察
  });
  const context = await browser.newContext();
  const page = await context.newPage();

  let testResults = {
    oauthLogin: false,
    staticLogin: false,
    systemFunctions: []
  };

  try {
    console.log('='.repeat(60));
    console.log('开始完整系统测试');
    console.log('='.repeat(60));

    // ========== 测试1: OAuth登录 ==========
    console.log('\n[测试1] OAuth登录测试 (sa/admin123)');
    console.log('-'.repeat(60));

    await page.goto('http://127.0.0.1:8848');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 查找登录表单
    const usernameInput = await page.locator('input[type="text"], input[placeholder*="用户"], input[placeholder*="账号"]').first();
    const passwordInput = await page.locator('input[type="password"]').first();

    if (await usernameInput.count() > 0) {
      await usernameInput.fill('sa');
      await passwordInput.fill('admin123');

      const loginButton = await page.locator('button:has-text("登录"), button:has-text("Login")').first();
      await loginButton.click();

      await page.waitForTimeout(3000);

      if (page.url().includes('home') || page.url().includes('dashboard')) {
        console.log('✅ OAuth登录成功！');
        console.log('   当前URL:', page.url());
        testResults.oauthLogin = true;
        await page.screenshot({ path: 'test-oauth-login-success.png' });
      } else {
        console.log('❌ OAuth登录失败');
        console.log('   当前URL:', page.url());
        await page.screenshot({ path: 'test-oauth-login-failed.png' });
      }
    }

    // ========== 测试2: 探索系统功能 ==========
    if (testResults.oauthLogin) {
      console.log('\n[测试2] 探索系统功能');
      console.log('-'.repeat(60));

      await page.waitForTimeout(2000);

      // 查找所有菜单项
      const menuItems = await page.locator('.el-menu-item, .sidebar-item, [class*="menu"], [class*="nav"]').all();
      console.log(`   发现 ${menuItems.length} 个可能的菜单项`);

      // 截图主页
      await page.screenshot({ path: 'test-home-page.png', fullPage: true });
      console.log('   ✅ 主页截图已保存');

      // 尝试点击一些常见的菜单
      const commonMenus = ['用户管理', '系统管理', '权限管理', '角色管理', '菜单管理'];

      for (const menuName of commonMenus) {
        try {
          const menuItem = page.locator(`text="${menuName}"`).first();
          if (await menuItem.count() > 0) {
            console.log(`\n   测试菜单: ${menuName}`);
            await menuItem.click();
            await page.waitForTimeout(1500);

            const currentUrl = page.url();
            console.log(`   ✅ 成功访问: ${currentUrl}`);
            await page.screenshot({ path: `test-menu-${menuName}.png`, fullPage: true });

            testResults.systemFunctions.push({
              name: menuName,
              success: true,
              url: currentUrl
            });
          }
        } catch (e) {
          console.log(`   ⚠️ ${menuName} 访问失败: ${e.message}`);
          testResults.systemFunctions.push({
            name: menuName,
            success: false,
            error: e.message
          });
        }
      }
    }

    // ========== 测试3: 静态登录 ==========
    console.log('\n[测试3] 静态登录测试');
    console.log('-'.repeat(60));

    // 退出登录
    try {
      await page.goto('http://127.0.0.1:8848/#/login');
      await page.waitForTimeout(2000);

      // 查找静态登录选项
      const staticLoginCard = await page.locator('.option-card--static, [class*="static"]').first();

      if (await staticLoginCard.count() > 0) {
        console.log('   找到静态登录选项，尝试点击...');
        await staticLoginCard.click();
        await page.waitForTimeout(3000);

        if (page.url().includes('home') || page.url().includes('dashboard')) {
          console.log('✅ 静态登录成功！');
          testResults.staticLogin = true;
          await page.screenshot({ path: 'test-static-login-success.png' });
        } else {
          console.log('❌ 静态登录失败（这在full模式下是正常的）');
          await page.screenshot({ path: 'test-static-login-failed.png' });
        }
      } else {
        console.log('   ⚠️ 未找到静态登录选项');
      }
    } catch (e) {
      console.log(`   ❌ 静态登录测试出错: ${e.message}`);
    }

    // ========== 测试总结 ==========
    console.log('\n' + '='.repeat(60));
    console.log('测试总结');
    console.log('='.repeat(60));
    console.log(`OAuth登录: ${testResults.oauthLogin ? '✅ 成功' : '❌ 失败'}`);
    console.log(`静态登录: ${testResults.staticLogin ? '✅ 成功' : '❌ 失败（预期）'}`);
    console.log(`系统功能测试: ${testResults.systemFunctions.length} 个功能`);

    testResults.systemFunctions.forEach(func => {
      console.log(`  - ${func.name}: ${func.success ? '✅' : '❌'}`);
    });

    console.log('\n所有截图已保存到 scripts/test/ 目录');
    console.log('='.repeat(60));

    // 保持浏览器打开10秒供查看
    console.log('\n浏览器将在10秒后关闭...');
    await page.waitForTimeout(10000);

  } catch (error) {
    console.error('\n❌ 测试过程出错:', error.message);
    await page.screenshot({ path: 'test-error.png', fullPage: true });
  } finally {
    await browser.close();
    console.log('\n测试完成！');
  }
})();
