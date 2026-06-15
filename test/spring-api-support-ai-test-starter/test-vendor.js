const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage();
  await p.goto('http://localhost:5173/#/ai/manufacturer', { waitUntil: 'networkidle', timeout: 20000 });
  await p.waitForTimeout(3000);
  const text = await p.evaluate(() => document.body.innerText.substring(0, 1000));
  console.log('=== 厂家页面直接访问 ===');
  console.log('厂家与模型中心:' + text.includes('厂家与模型中心'));
  console.log('AI 工作台:' + text.includes('AI 工作台'));
  console.log('---');
  console.log(text.substring(0, 500).replace(/\n/g, ' | '));
  await p.screenshot({ path: 'H:\\workspace\\2\\spring-support-api-parent\\test\\spring-api-support-ai-test-starter\\17-vendor-direct.png', fullPage: true });
  await b.close();
})();
