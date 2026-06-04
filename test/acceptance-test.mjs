import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';

const BASE_URL = 'http://localhost:8853';
const SCREENSHOT_DIR = 'h:/workspace/2/spring-support-api-parent';
const PREFIX = 'verify';

async function screenshot(page, name) {
  const filePath = path.join(SCREENSHOT_DIR, `${PREFIX}-${name}.png`);
  await page.screenshot({ path: filePath, fullPage: true });
  console.log(`  ✓ Screenshot saved: ${PREFIX}-${name}.png`);
}

async function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

async function main() {
  console.log('=== DataLake Dashboard Acceptance Test ===\n');

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 },
  });
  const page = await context.newPage();

  // Listen for console errors
  const consoleErrors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      consoleErrors.push({ text: msg.text(), location: msg.location() });
    }
  });

  try {
    // Step 1: Initial page load
    console.log('1. Loading page...');
    await page.goto(BASE_URL, { waitUntil: 'networkidle', timeout: 30000 });
    await sleep(2000);
    await screenshot(page, 'page-load');
    console.log('   Page loaded successfully.\n');

    // Step 2: Check stat cards
    console.log('2. Checking stat cards for real data...');
    // Try to find stat cards / value displays
    const statValues = await page.locator('.stat-value, .el-statistic__value, [class*="stat"]').all();
    if (statValues.length > 0) {
      for (const sv of statValues) {
        const text = await sv.textContent();
        console.log(`   Stat value: "${text}"`);
      }
    } else {
      console.log('   No stat cards found on initial view (expected if homepage is empty).');
    }

    // Check for any numeric data indicators
    const numericElements = await page.locator('.el-card, [class*="card"]').all();
    console.log(`   Found ${numericElements.length} card elements.\n`);

    // Step 3-4: Navigate each tab
    const tabs = [
      '流水线管理',
      '数据查询',
      '采集端',
      '订阅管理',
      '节点管理',
      '监控面板',
      '死信队列',
      '系统配置',
    ];

    console.log('3. Navigating all tabs...');
    for (const tab of tabs) {
      console.log(`   Clicking tab: ${tab}`);
      try {
        // Try clicking the tab link
        const tabLink = page.locator(`a, button, span, div`, { hasText: tab }).first();
        if (await tabLink.isVisible({ timeout: 3000 })) {
          await tabLink.click();
          await sleep(2000);
          await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});
          await screenshot(page, `tab-${tab}`);
          console.log(`   ✓ Tab "${tab}" screenshot captured.`);
        } else {
          console.log(`   ✗ Tab "${tab}" not visible.`);
        }
      } catch (e) {
        console.log(`   ✗ Failed to click tab "${tab}": ${e.message}`);
        // Take screenshot anyway
        await screenshot(page, `tab-${tab}-error`);
      }
    }
    console.log('');

    // Step 5: Navigate to settings page directly
    console.log('4. Navigating to settings page...');
    try {
      await page.goto(`${BASE_URL}/#/datalake/settings`, { waitUntil: 'networkidle', timeout: 15000 });
      await sleep(2000);
      await screenshot(page, 'settings-page');
      console.log('   Settings page screenshot captured.');
    } catch (e) {
      console.log(`   Settings page navigation failed: ${e.message}`);
    }
    console.log('');

    // Step 6: Check token count
    console.log('5. Checking token count badge...');
    const badgeElements = await page.locator('.el-badge__content, [class*="badge"]').all();
    if (badgeElements.length > 0) {
      for (const badge of badgeElements) {
        const text = await badge.textContent();
        console.log(`   Badge found: "${text}"`);
      }
    } else {
      console.log('   No badges found on page.');
    }
    console.log('');

    // Step 7: Report console errors
    console.log('6. Console errors:');
    if (consoleErrors.length === 0) {
      console.log('   ✓ No console errors detected.');
    } else {
      for (const err of consoleErrors) {
        console.log(`   ✗ [ERROR] ${err.text}`);
        if (err.location) {
          console.log(`       at ${err.location.url}:${err.location.lineNumber}`);
        }
      }
    }
    console.log('');

    // Final summary
    console.log('=== Test Complete ===');
    console.log(`Total console errors: ${consoleErrors.length}`);

  } catch (e) {
    console.error(`\nTest failed: ${e.message}`);
    await screenshot(page, 'failure').catch(() => {});
  } finally {
    await browser.close();
  }
}

main().catch(console.error);
