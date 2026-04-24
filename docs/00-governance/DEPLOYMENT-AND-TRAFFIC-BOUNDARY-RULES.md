# Deployment And Traffic Boundary Rules

本文档定义部署、运行入口和流量边界的工程级规则。

## 1. Purpose

Sandwich 当前以两个 WAR 应用作为主要运行入口：

- `sandwish-admin-api`
- `sandwish-front-api`

本文档用于避免后台、前台、共享业务和部署配置之间的边界混乱。

## 2. Scope

当前范围：

- WAR 打包
- 配置文件
- 前后台入口边界
- vendor JAR 路径
- 上线准备文档路由

不在范围内：

- 不定义具体生产环境拓扑
- 不替代运维 Runbook
- 不替代业务需求文档

## 3. Deployment Units

- `sandwish-admin-api` 打包为后台 WAR
- `sandwish-front-api` 打包为前台 WAR
- `sandwish-biz` 和 `sandwish-common` 打包为 jar，由 WAR 模块依赖

## 4. Traffic Boundary

- 后台管理流量进入 `sandwish-admin-api`
- 前台用户流量进入 `sandwish-front-api`
- 前后台共享业务能力进入 `sandwish-biz`
- 通用技术能力进入 `sandwish-common`
- `sandwish-admin-api` 与 `sandwish-front-api` 不互相依赖

## 5. Configuration Rules

- 配置文件固定放在 `src/main/resources/config`
- 不提交环境私有密钥、账号、密码和生产连接串
- 环境差异通过 profile、部署参数或外部配置覆盖
- WAR 模块中的 `WEB-INF/lib` vendor JAR 路径保持稳定

## 6. Readiness Documents

上线准备、运维和发布任务默认读取：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. 本文件
3. `../../40-readiness/` 下对应文档

## 7. Open Items

无
