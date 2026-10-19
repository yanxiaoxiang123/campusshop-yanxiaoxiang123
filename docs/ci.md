# CI 使用说明

本仓库使用 GitHub Actions 在每次 push 到 `main` 和每个 Pull Request 上运行测试。

## 作业

| 作业 | 内容 | 失败时 |
|---|---|---|
| `unit-and-api` | 单元测试 + API 测试（`./mvnw -B test`） | 始终上传 Surefire 报告 |
| `e2e` | 打包 jar、后台启动网站、安装 Chromium、运行 Playwright E2E | 上传 `java/traces/` 和 `java/app.log` |

`e2e` 通过 `needs: unit-and-api` 依赖 `unit-and-api`，单元或 API 测试失败时不会运行。

## 本地运行

```powershell
cd java

# 单元 + API
.\mvnw.cmd "-Djacoco.skip=true" test

# E2E：先启动网站
.\mvnw.cmd spring-boot:run
# 另一个终端
.\mvnw.cmd "-Djacoco.skip=true" test -Pe2e
```

## 分支保护

`main` 分支要求：

- 通过 Pull Request 合并；
- `unit-and-api` 和 `e2e` 检查通过。

## 产物

- `surefire-reports`：单元/API 测试报告（始终上传）；
- `playwright-traces`：E2E 失败时的 Trace（失败时上传）；
- `app-log`：网站启动日志（失败时上传）。

## 相关文档

- `.github/workflows/ci.yml`
- `../README.md`
