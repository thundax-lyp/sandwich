# Deployment And Traffic Boundary Rules

本文档定义部署、运行入口和流量边界的工程级规则。

## 1. Purpose

Sandwich 当前以两个 jar API 应用作为主要运行入口：

- `sandwish-admin-api`
- `sandwish-front-api`

本文档用于避免后台、前台、共享业务和部署配置之间的边界混乱。

## 2. Scope

当前范围：

- jar 打包
- 配置文件
- 前后台入口边界
- API 流量归属
- 静态 API 支撑资源运行边界
- vendor JAR 路径
- 上线准备文档路由

不在范围内：

- 不定义具体生产环境拓扑
- 不替代运维 Runbook
- 不替代业务需求文档

## 3. Deployment Units

- `sandwish-admin-api` 打包为后台 API jar
- `sandwish-front-api` 打包为前台 API jar
- `sandwish-infra` 打包为 jar，由 API 入口模块依赖
- `sandwish-biz`、`sandwish-common-core` 和 `sandwish-common-mybatis` 打包为 jar，由上层模块依赖
- `sandwish-common` 是 common 聚合模块，不作为运行 jar 直接依赖
- API 应用固定通过 Maven module 打包，不新增 `war` 运行入口

## 4. Traffic Boundary

- 后台管理流量进入 `sandwish-admin-api`
- 前台用户流量进入 `sandwish-front-api`
- 后台专用 Controller、Filter、Interceptor、Swagger 配置和静态 API 支撑资源归属 `sandwish-admin-api`
- 前台专用 Controller、Filter、Interceptor 和访问适配归属 `sandwish-front-api`
- 前后台共享业务能力进入 `sandwish-biz`
- 前后台共享持久化实现进入 `sandwish-infra`
- 通用技术能力进入 `sandwish-common`
- `sandwish-admin-api` 与 `sandwish-front-api` 不互相依赖
- 外部流量不得绕过 API 入口模块直接访问 `sandwish-biz` 或 `sandwish-infra`

## 5. Configuration Rules

- 配置文件固定放在 `src/main/resources/config`
- 不提交环境私有密钥、账号、密码和生产连接串
- 环境差异通过 profile、部署参数或外部配置覆盖
- 不新增 `WEB-INF/lib`、服务端页面模板、标签库或页面装饰器运行入口
- vendor JAR 只在确有依赖且 Maven 仓库不可用时保留，并限制在需要它的模块

## 6. Readiness Documents

上线准备、运维和发布任务默认读取：

1. [`ARCHITECTURE.md`](./ARCHITECTURE.md)
2. 本文件
3. `../../40-readiness/` 下对应文档

## 7. Open Items

无
