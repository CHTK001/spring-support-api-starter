package com.chua.starter.monitor.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本地演示页面，供爬虫模块直接抓取。
 */
@RestController
public class SpiderDemoPageController {

    @GetMapping(value = "/demo/page", produces = MediaType.TEXT_HTML_VALUE)
    public String page() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <title>Spider Demo Page</title>
                </head>
                <body>
                  <main id="content">
                    <section class="hero banner">
                      <h1>示例采集页面</h1>
                      <p data-role="summary">这个页面用于验证 preview / parse-html / xpath 回填。</p>
                    </section>
                    <article class="card featured">
                      <h2>文章标题</h2>
                      <ul class="items">
                        <li>第一条</li>
                        <li>第二条</li>
                        <li>第三条</li>
                      </ul>
                      <a class="nav item" href="/demo/detail?id=1">查看详情</a>
                    </article>
                  </main>
                </body>
                </html>
                """;
    }
}
