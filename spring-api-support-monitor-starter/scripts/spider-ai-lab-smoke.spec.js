const fs = require("node:fs/promises");
const path = require("node:path");
const { test, expect } = require("@playwright/test");

const labUrl = process.env.SPIDER_LAB_URL || "http://127.0.0.1:18170/spider-console/lab";
const authUser = process.env.SPIDER_AUTH_USER || "admin";
const authPassword = process.env.SPIDER_AUTH_PASSWORD || "admin123";
const targetUrl = process.env.SPIDER_TARGET_URL || "https://example.com/";
const expectAi = /^true$/i.test(process.env.SPIDER_EXPECT_AI || "false");

const screenshotDir = path.resolve("spring-api-support-monitor-starter/.runtime/spider-ai-lab-smoke");
const authBaseUrl = new URL("../v1/spider/auth/", labUrl);

test.describe("Spider AI Lab smoke", () => {
  test.setTimeout(120000);

  test("preview, parse, selector, ai", async ({ browser }) => {
    await fs.mkdir(screenshotDir, { recursive: true });

    const context = await browser.newContext({
      viewport: { width: 1600, height: 1200 }
    });
    const page = await context.newPage();

    try {
      const authStatus = await fetchAuthStatus(context);
      if (authStatus.authEnabled && !authStatus.authenticated) {
        await login(context);
      }

      await page.goto(labUrl, { waitUntil: "networkidle" });
      await expect(page.getByRole("heading", { name: "Spider Lab" })).toBeVisible();

      await page.locator("#targetUrl").fill(targetUrl);
      await page.locator("#previewBtn").click();
      await expect(page.locator("#previewMeta")).not.toHaveText(/No page loaded/);

      await page.locator("#parseBtn").click();
      await expect(page.locator("#nodeList .node-card").first()).toBeVisible({ timeout: 15000 });
      await page.screenshot({ path: path.join(screenshotDir, "spider-lab-parse.png"), fullPage: true });

      await page.locator("#selectorType").selectOption("CSS");
      await page.locator("#selectorInput").fill("a");
      await page.locator("#testSelectorBtn").click();
      await expect(page.locator("#selectorResult")).not.toHaveText(/No selector call yet/);

      await page.locator("#inspectSelectorBtn").click();
      await expect(page.locator("#inspectResult .result-card").first()).toBeVisible({ timeout: 15000 });

      await page.locator("#refreshAiBtn").click();
      await expect(page.locator("#aiMeta")).toBeVisible();
      const aiMeta = (await page.locator("#aiMeta").textContent()) || "";
      const aiStatus = (await page.locator("#aiStatusChips").textContent()) || "";

      if (expectAi) {
        expect(aiMeta + aiStatus).not.toMatch(/unavailable/i);
      }

      if (!/unavailable/i.test(aiMeta + aiStatus)) {
        await page.locator("#aiGoal").fill("提取页面标题、主要链接和页面用途，并推荐稳定选择器");
        await page.locator("#useSelectorForAiBtn").click();
        await page.locator("#analyzeAiBtn").click();
        await page.waitForTimeout(3000);
        await expect(page.locator("#aiAnalysisResult")).not.toHaveText(/No AI analysis yet/);
      }

      await page.screenshot({ path: path.join(screenshotDir, "spider-lab-ai.png"), fullPage: true });
      await fs.writeFile(
        path.join(screenshotDir, "summary.json"),
        JSON.stringify({
          labUrl,
          targetUrl,
          aiExpected: expectAi,
          timestamp: new Date().toISOString()
        }, null, 2),
        "utf8"
      );
    } finally {
      await context.close();
    }
  });
});

async function fetchAuthStatus(context) {
  const response = await context.request.get(new URL("status", authBaseUrl).toString());
  if (!response.ok()) {
    throw new Error(`Failed to fetch auth status: ${response.status()}`);
  }
  const payload = await response.json();
  return unwrap(payload);
}

async function login(context) {
  const response = await context.request.post(new URL("login", authBaseUrl).toString(), {
    data: {
      username: authUser,
      password: authPassword
    }
  });
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()}`);
  }
  unwrap(await response.json());
}

function unwrap(payload) {
  if (payload && typeof payload === "object" && "code" in payload) {
    const code = String(payload.code ?? "");
    if (!["00000", "200", "0"].includes(code)) {
      throw new Error(payload.message || payload.msg || payload.error || `Business request failed: ${code}`);
    }
    return "data" in payload ? payload.data : payload;
  }
  return payload;
}
