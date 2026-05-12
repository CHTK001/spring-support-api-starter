---
name: payment-features-completed
description: 支付模块8项功能改进完成验证报告
type: project
---

## 支付模块功能改进 — 完成报告

### 已完成的 8 项改进

1. **createdAt 后端修复**
   - 移除了 `WalletAccount` 和 `WalletAccountLog` 的 `FieldFill.INSERT`
   - MySQL DEFAULT CURRENT_TIMESTAMP 生效
   - MySQL UPDATE 修复了已有的 NULL 记录

2. **钱包统计一行显示**
   - CSS `grid-template-columns: repeat(4, minmax(0, 1fr))` 生效
   - 显示：钱包账户数 2 / 可用余额总计 ¥1868.99 / 冻结余额总计 ¥0.00 / 启用账户 2

3. **交易列表渠道图标**
   - 微信支付：绿色 `#07c160` 背景 "微"
   - 支付宝：蓝色 `#1677ff` 背景 "支"
   - 钱包：紫色 `#8b5cf6` 背景 "钱"
   - 易支付：橙色 `#f59e0b` 背景 "易"

4. **CouponList 错误处理**
   - 后端优惠券 API 尚不存在
   - 添加 try-catch 到 loadStats/loadBase
   - fetchCouponList 检查响应码，非 00000 返回空数据

5. **WalletConsole 时间轴视图**
   - 表格视图 / 时间轴视图切换
   - createdAt 格式化显示
   - 业务类型、金额、余额变化、操作人等信息

6. **MerchantSettingDrawer 美化**
   - Tab 渐变 active bar、hover 动画
   - 能力开关卡片彩色图标背景
   - 钱包限额网格布局 + 彩色左边框

7. **PaymentGlobalConfigView 渠道前缀**
   - 微信/支付宝/钱包/易支付 4个渠道前缀卡片
   - 回调预览面板

8. **OrderConfigView 卡片优化**
   - ScCard 布局、ScSwitch 开关、状态指示器
   - 分表策略卡片

### 验证结果
- 后端编译：通过 (mvn compile)
- 前端编译：通过 (vite build)
- 页面测试：WalletConsole/TransactionList/CouponList 无错误
- HMR 缓存问题：偶发过时状态，新开标签页可解决
