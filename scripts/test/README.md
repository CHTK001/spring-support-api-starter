# 浏览器自动化测试指南

## 📋 概述

由于AI助手无法直接操作浏览器界面，我们使用 **Playwright** 来实现浏览器自动化测试。Playwright 可以：
- 自动打开浏览器
- 模拟用户点击、输入等操作
- 自动截图记录测试过程
- 验证页面元素和功能

---

## 🚀 快速开始

### 1. 安装 Playwright

```powershell
# 进入测试目录
cd H:/workspace/2/spring-support-api-parent/scripts/test

# 安装依赖
npm install

# 安装浏览器（首次运行需要）
npx playwright install
```

### 2. 运行测试

```powershell
# 运行所有测试（无头模式，后台运行）
npm test

# 运行测试并显示浏览器窗口（可以看到操作过程）
npm run test:headed

# 调试模式（逐步执行，可以暂停）
npm run test:debug

# 查看测试报告
npm run test:report
```

---

## 📸 测试截图

测试运行后，会在 `screenshots/` 目录生成截图：

```
screenshots/
├── 01-login-page.png          # 登录页面
├── 02-before-static-login.png # 静态登录前
├── 03-after-static-login.png  # 静态登录后
├── 04-before-login.png         # 账号登录前
├── 05-after-login.png          # 账号登录后
├── 06-sms-login-form.png       # 短信登录表单
├── 07-sms-code-sent.png        # 验证码发送后
├── 08-user-management.png      # 用户管理页面
├── 09-role-management.png      # 角色管理页面
├── 10-menu-management.png      # 菜单管理页面
├── 11-light-theme.png          # 亮色主题
├── 12-dark-theme.png           # 暗色主题
├── 13-language-menu.png        # 语言菜单
├── 14-desktop-view.png         # 桌面视图
├── 15-tablet-view.png          # 平板视图
└── 16-mobile-view.png          # 手机视图
```

---

## 🧪 测试用例说明

### 登录测试
- ✅ **测试1**: 页面加载和环境标识
- ✅ **测试2**: 静态登录功能
- ✅ **测试3**: 账号登录功能
- ✅ **测试4**: 短信登录界面

### 功能测试
- ✅ **测试5**: 用户管理页面
- ✅ **测试6**: 角色管理页面
- ✅ **测试7**: 菜单管理页面

### UI/UX测试
- ✅ **测试8**: 主题切换
- ✅ **测试9**: 语言切换
- ✅ **测试10**: 响应式布局

---

## 🎯 测试流程

```
1. 启动浏览器
   ↓
2. 访问登录页面
   ↓
3. 自动截图
   ↓
4. 执行测试操作（点击、输入等）
   ↓
5. 验证结果
   ↓
6. 生成测试报告
```

---

## 📊 测试报告

测试完成后，Playwright 会生成详细的HTML报告：

```powershell
# 查看测试报告
npm run test:report
```

报告包含：
- 测试用例执行结果
- 每个步骤的截图
- 执行时间统计
- 失败原因分析

---

## 🔧 自定义测试

你可以修改 `browser-test.js` 来添加更多测试：

```javascript
test('你的测试名称', async ({ page }) => {
  // 访问页面
  await page.goto('http://127.0.0.1:8848');

  // 点击元素
  await page.click('button:has-text("登录")');

  // 输入文本
  await page.fill('input[name="username"]', 'admin');

  // 等待元素
  await page.waitForSelector('.user-info');

  // 截图
  await page.screenshot({ path: 'my-test.png' });

  // 断言
  expect(await page.title()).toContain('系统');
});
```

---

## 🐛 常见问题

### Q1: 提示找不到浏览器
```powershell
# 重新安装浏览器
npx playwright install
```

### Q2: 测试超时
```javascript
// 增加超时时间
test.setTimeout(60000); // 60秒
```

### Q3: 元素找不到
```javascript
// 使用更精确的选择器
await page.locator('[data-testid="login-button"]').click();
```

### Q4: 想看到浏览器操作过程
```powershell
# 使用 headed 模式
npm run test:headed
```

---

## 🎬 视频录制

Playwright 还可以录制测试过程的视频：

```javascript
// 在 playwright.config.js 中配置
use: {
  video: 'on', // 录制视频
  trace: 'on', // 记录追踪信息
}
```

---

## 📝 完整配置文件

创建 `playwright.config.js`:

```javascript
module.exports = {
  testDir: './',
  timeout: 30000,
  use: {
    baseURL: 'http://127.0.0.1:8848',
    screenshot: 'on',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
    },
    {
      name: 'firefox',
      use: { browserName: 'firefox' },
    },
    {
      name: 'webkit',
      use: { browserName: 'webkit' },
    },
  ],
};
```

---

## 🎉 总结

通过 Playwright，我可以：
1. ✅ 自动打开浏览器
2. ✅ 模拟用户操作（点击、输入、滚动等）
3. ✅ 自动截图记录过程
4. ✅ 验证功能是否正常
5. ✅ 生成详细的测试报告

这样就实现了"让AI操作浏览器"的效果！

---

## 🚀 立即运行

```powershell
# 一键安装并运行测试
cd H:/workspace/2/spring-support-api-parent/scripts/test
npm install && npx playwright install && npm run test:headed
```

运行后，你会看到浏览器自动打开，自动执行所有测试操作！
