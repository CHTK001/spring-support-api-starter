# Spider Module Test Report

Date: 2026-03-27
Updated: 2026-03-28
Workspace: `H:\workspace\2\spring-support-api-parent`

## Scope

- Verify the spider module in `spring-api-support-monitor-starter`
- Verify the spider module can attach to a plain Spring host through starter auto-configuration
- Verify the local standalone demo host in `spring-api-support-monitor-spider-demo`
- Verify the full spider API and console on the remote system host after deployment

## Implemented Changes

- Added embedded spider console/auth support in `spring-api-support-monitor-starter`
  - `SpiderConsoleController`
  - `SpiderAuthController`
  - `SpiderAuthenticationFilter`
  - `SpiderEmbeddedAuthConfig`
  - `SpiderOauthWhitelistConfiguration`
  - `SpiderAutoConfiguration`
- Published the Vue spider console assets into Spring static resources under `spring-api-support-monitor-starter/src/main/resources/static/spider-console`
- Added a generic demo host module `spring-api-support-monitor-spider-demo` for local standalone validation
- Relaxed spider's dependency on external datasource console services
  - builtin monitor datasource remains available when `SystemDataSettingService` is absent
  - output-table operations work on the current monitor datasource without `SystemDataConsoleService`
- Kept output-table DDL generation preserving empty-string defaults in preview SQL and sync SQL
- Fixed legacy MySQL spider schema compatibility in `SpiderSchemaService`
  - still runs the base schema SQL script
  - detects missing required tables and columns
  - backfills missing legacy columns with targeted `ALTER TABLE ... ADD COLUMN ...`
  - re-checks table readiness after initialization and fails fast if the schema is still incomplete
- Added/updated unit tests covering schema readiness, output table lifecycle, Spring host exposure, and legacy schema backfill

## Local Automated Test Execution

Command:

```powershell
mvn -pl spring-api-support-monitor-starter -am "-Dsurefire.failIfNoSpecifiedTests=false" "-Dtest=SpiderAutoConfigurationTest,MonitorSpiderControllerSpringHostTest,MonitorSpiderControllerHttpIntegrationTest,SpiderOutputTableServiceTest,SpiderSchemaServiceTest" test
```

Execution time:

- Finished at: `2026-03-28 16:03:04 +08:00`

Result:

- Total tests: 23
- Failures: 0
- Errors: 0
- Skipped: 0
- Build result: `BUILD SUCCESS`

Suite breakdown:

- `SpiderAutoConfigurationTest`: 2 passed
- `MonitorSpiderControllerHttpIntegrationTest`: 4 passed
- `MonitorSpiderControllerSpringHostTest`: 6 passed
- `SpiderOutputTableServiceTest`: 8 passed
- `SpiderSchemaServiceTest`: 3 passed

Legacy MySQL schema backfill verification:

- `SpiderSchemaServiceTest.initializeTablesBackfillsLegacyMissingColumns` validated that 8 missing columns are backfilled on old schemas:
  - `monitor_spider_task.spider_task_max_depth`
  - `monitor_spider_task.spider_task_enable_url_discover`
  - `monitor_spider_task.spider_task_page_pattern`
  - `monitor_spider_task.spider_task_max_pages`
  - `monitor_spider_task.spider_task_incremental_mode`
  - `monitor_spider_task.spider_task_rate_limit`
  - `monitor_spider_data.create_time`
  - `monitor_spider_log.create_time`

Surefire artifacts:

- `H:\workspace\2\spring-support-api-parent\spring-api-support-monitor-starter\target\surefire-reports`
- `H:\workspace\2\spring-support-api-parent\spring-api-support-monitor-starter\target\reports\surefire.html`

## Local Standalone Host Validation

Packaging command:

```powershell
mvn -pl spring-api-support-monitor-spider-demo -am -DskipTests package
```

Demo artifact:

- `H:\workspace\2\spring-support-api-parent\spring-api-support-monitor-spider-demo\target\spring-api-support-monitor-spider-demo-4.0.0.37.jar`

Validated behavior on the local demo host:

- spider console static resources render correctly
- preview, parse-html, inspect-selector, test-selector, and generate-xpath endpoints work
- output table DDL preview and structure sync work
- generic `SpiderAutoConfiguration` can expose spider APIs in a plain Spring servlet host

## Remote Deployment Validation

Deployment date:

- 2026-03-28

Remote target:

- SSH host: `8.139.4.229`
- LAN host: `172.16.0.40`
- App directory: `/opt/spring/spring-api-support-system-starter-4.0.0`
- Base API URL: `http://172.16.0.40:18170/system/api`

Build command:

```powershell
mvn -pl spring-api-support-system-starter -am -DskipTests package
```

Published state after redeploy:

- Current listening PID: `4090`
- Current spider auth status API: `GET http://127.0.0.1:18170/system/api/v1/spider/auth/status` returns `authEnabled=true`, `authenticated=false`
- Remote monitor jar SHA256:
  - `c444c8660e2cf034d0023204471f115cff070296b211a01d37a2f87966973f74`
  - file: `/opt/spring/spring-api-support-system-starter-4.0.0/lib/spring-api-support-monitor-starter-4.0.0.37.jar`

## Remote Full Spider Regression

Artifacts:

- `H:\workspace\2\spring-support-api-parent\.runtime\spider-full-remote-summary.json`
- `H:\workspace\2\spring-support-api-parent\.runtime\spider-full-remote-state.json`

Summary result:

- `REMOTE_SPIDER_REGRESSION_OK`

Completed check groups:

- console static routes
- anonymous auth guard and bad login
- login and auth info
- table initialize and datasource options
- preview parse-html selector xpath
- output table lifecycle
- spi metadata
- task crud and statistics
- data logs export
- design endpoints
- batch endpoints and cleanup api
- logout

Final remote cleanup verification:

- tasks = 0
- data = 0
- logs = 0
- nodes = 0
- connections = 0
- table = 0

Remote API coverage confirmed:

- auth lifecycle
- spider table initialization and readiness checks
- preview/parse-html/xpath toolchain
- output table create/exists/structure/sync
- SPI metadata endpoints
- task CRUD, run, stop, clone, status, and statistics
- spider data, logs, and export
- visual design nodes, positions, and connections
- batch run/stop/delete

## Browser Smoke Validation

Validated on the remote console after deployment:

- login page renders correctly
- login flow succeeds with embedded auth
- main console renders `爬虫任务`, `任务详情`, `最近日志`, `基础表`, `可视化编排`
- plugin panel and node property panel are visible
- interface settings modal opens successfully
- API root input renders as `../v1/spider/`

Browser screenshots:

- `H:\workspace\2\spring-support-api-parent\scripts\tmp-agent-browser-shots\spider-remote-login-check.png`
- `H:\workspace\2\spring-support-api-parent\scripts\tmp-agent-browser-shots\spider-remote-console-check.png`
- `H:\workspace\2\spring-support-api-parent\scripts\tmp-agent-browser-shots\spider-remote-settings-modal.png`
- `H:\workspace\2\spring-support-api-parent\scripts\tmp-agent-browser-shots\screenshot-1774684723289.png`

## Conclusion

- The spider module now works as a standalone attachable Spring starter
- The local demo host validates that the module does not need a full system host to run
- The remote system host deployment on `172.16.0.40:18170` passed full API regression and browser smoke validation
- The legacy MySQL missing-column problem was fixed by explicit schema backfill logic and is covered by automated tests
