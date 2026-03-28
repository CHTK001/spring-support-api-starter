# spring-api-support-monitor-spider-demo

最小可运行爬虫宿主示例。

## 启动

```powershell
mvn -pl spring-api-support-monitor-spider-demo -am spring-boot:run
```

## 演示页面

- `http://127.0.0.1:19171/demo/page`

## 可直接验证的接口

- `GET /v1/spider/preview?url=http://127.0.0.1:19171/demo/page`
- `GET /v1/spider/parse-html?url=http://127.0.0.1:19171/demo/page`
- `POST /v1/spider/test-selector`
- `POST /v1/spider/inspect-selector`
- `POST /v1/spider/generate-xpath`
- `GET /v1/spider/table/status`
- `POST /v1/spider/table/initialize`
- `POST /v1/spider/output/table/preview-sql`
