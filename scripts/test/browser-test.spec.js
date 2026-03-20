/**
 * 浏览器自动化测试脚本
 * 使用 Playwright 进行浏览器自动化测试
 *
 * 安装: npm install -D @playwright/test
 * 运行: npx playwright test browser-test.js
 */

const { test, expect } = require('@playwright/test');

// 测试配置
const BASE_URL = 'http://127.0.0.1:8848';
const API_URL = 'http://127.0.0.1:18175';

test.describe('系统登录测试', () => {

  test('测试1: 页面加载和环境标识', async ({ page }) => {
    // 访问登录页面
    await page.goto(BASE_URL);

    // 等待页面加载
    await page.waitForLoadState('networkidle');

    // 截图
    await page.screenshot({ path: 'screenshots/01-login-page.png', fullPage: true });

    // 检查页面标题
    const title = await page.title();
    console.log('页面标题:', title);
    expect(title).toContain('登录');

    // 检查环境标识
    const envBadge = await page.locator('.env-badge').textContent();
    console.log('环境标识:', envBadge);
    expect(envBadge).toContain('开发环境');
  });

  test('测试2: 静态登录功能', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');

    // 查找静态登录选项
    const staticLoginCard = page.locator('.option-card--static');
    await expect(staticLoginCard).toBeVisible();

    // 截图
    await page.screenshot({ path: 'screenshots/02-before-static-login.png' });

    // 点击静态登录
    await staticLoginCard.click();

    // 等待登录完成（等待跳转）
    await page.waitForURL(/\/(home|dashboard)/, { timeout: 10000 });

    // 截图登录后页面
    await page.screenshot({ path: 'screenshots/03-after-static-login.png', fullPage: true });

    // 验证登录成功
    const url = page.url();
    console.log('登录后URL:', url);
    expect(url).not.toContain('login');
  });

  test('测试3: 账号登录功能', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');

    // 选择账号登录
    const baseLoginCard = page.locator('.option-card').filter({ hasText: '账号登录' });
    await baseLoginCard.click();

    // 等待表单显示
    await page.waitForSelector('input[type="text"]', { state: 'visible' });

    // 输入用户名
    await page.fill('input[type="text"]', 'admin');

    // 输入密码
    await page.fill('input[type="password"]', 'admin@123!456');

    // 截图
    await page.screenshot({ path: 'screenshots/04-before-login.png' });

    // 点击登录按钮
    await page.click('button:has-text("登录")');

    // 等待登录完成
    await page.waitForURL(/\/(home|dashboard)/, { timeout: 10000 });

    // 截图
    await page.screenshot({ path: 'screenshots/05-after-login.png', fullPage: true });

    console.log('账号登录成功');
  });

  test('测试4: 短信登录界面', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');

    // 选择短信登录
    const smsLoginCard = page.locator('.option-card').filter({ hasText: '短信登录' });

    if (await smsLoginCard.isVisible()) {
      await smsLoginCard.click();

      // 等待表单显示
      await page.waitForSelector('input[placeholder*="手机号"]', { state: 'visible' });

      // 截图
      await page.screenshot({ path: 'screenshots/06-sms-login-form.png' });

      // 输入手机号
      await page.fill('input[placeholder*="手机号"]', '13800138000');

      // 点击发送验证码
      await page.click('button:has-text("发送验证码")');

      // 等待响应
      await page.waitForTimeout(2000);

      // 截图
      await page.screenshot({ path: 'screenshots/07-sms-code-sent.png' });

      console.log('短信登录界面测试完成');
    } else {
      console.log('短信登录未启用');
    }
  });
});

test.describe('系统功能测试', () => {

  // 登录前置操作
  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');

    // 使用静态登录
    const staticLoginCard = page.locator('.option-card--static');
    if (await staticLoginCard.isVisible()) {
      await staticLoginCard.click();
      await page.waitForURL(/\/(home|dashboard)/, { timeout: 10000 });
    }
  });

  test('测试5: 用户管理页面', async ({ page }) => {
    // 查找并点击用户管理菜单
    await page.click('text=用户管理');

    // 等待页面加载
    await page.waitForLoadState('networkidle');

    // 截图
    await page.screenshot({ path: 'screenshots/08-user-management.png', fullPage: true });

    // 检查表格是否存在
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();

    console.log('用户管理页面加载成功');
  });

  test('测试6: 角色管理页面', async ({ page }) => {
    await page.click('text=角色管理');
    await page.waitForLoadState('networkidle');

    await page.screenshot({ path: 'screenshots/09-role-management.png', fullPage: true });

    console.log('角色管理页面加载成功');
  });

  test('测试7: 菜单管理页面', async ({ page }) => {
    await page.click('text=菜单管理');
    await page.waitForLoadState('networkidle');

    await page.screenshot({ path: 'screenshots/10-menu-management.png', fullPage: true });

    console.log('菜单管理页面加载成功');
  });
});

test.describe('UI/UX测试', () => {

  test('测试8: 主题切换', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');

    // 截图亮色主题
    await page.screenshot({ path: 'screenshots/11-light-theme.png', fullPage: true });

    // 切换到暗色主题
    await page.click('.modern-theme-switch');
    await page.waitForTimeout(500);

    // 截图暗色主题
    await page.screenshot({ path: 'screenshots/12-dark-theme.png', fullPage: true });

    console.log('主题切换测试完成');
  });

  test('测试9: 语言切换', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');

    // 点击语言切换
    await page.click('.lang-trigger');
    await page.waitForTimeout(500);

    // 截图语言菜单
    await page.screenshot({ path: 'screenshots/13-language-menu.png' });

    console.log('语言切换测试完成');
  });

  test('测试10: 响应式布局', async ({ page }) => {
    await page.goto(BASE_URL);

    // 桌面尺寸
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.screenshot({ path: 'screenshots/14-desktop-view.png', fullPage: true });

    // 平板尺寸
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.screenshot({ path: 'screenshots/15-tablet-view.png', fullPage: true });

    // 手机尺寸
    await page.setViewportSize({ width: 375, height: 667 });
    await page.screenshot({ path: 'screenshots/16-mobile-view.png', fullPage: true });

    console.log('响应式布局测试完成');
  });
});
