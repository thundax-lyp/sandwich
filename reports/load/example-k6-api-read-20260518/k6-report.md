# Sandwich k6 API 读链路压测报告

## 测试结论

- 结论：通过。
- 峰值阶梯：已爬升到 200 VUs，并在 7m00s 左右进入 200 VUs 峰值点，随后按计划 1 分钟降压到 0。
- 错误率：0，满足 `http_req_failed rate<0.01`。
- 延迟阈值：P95 为 25.85 ms，满足 `p(95)<1000`；P99 为 52.18 ms，满足 `p(99)<2000`。
- 服务端异常：压测窗口内未发现 `ERROR`、`Exception`、`Unknown column`、`business_type` 或 5xx 日志。
- 说明：`POST /open-api/api/submission/submission/page` 的 401 是未签名 Open API 访问的预期结果，k6 已将 401 纳入 expected status，不计为失败。

## 测试元信息

- 测试时间：2026-05-18 11:30:15 - 2026-05-18 11:38:38 CST
- target: `http://example.test:18080`
- adminBaseUrl: `http://example.test:18080/admin-api`
- frontBaseUrl: `http://example.test:18080/front-api`
- openBaseUrl: `http://example.test:18080/open-api`
- stages: `1m:50,3m:100,3m:200,1m:0`
- includePreAuth: `false`
- k6 image: `sandwish/k6:dev`
- k6 network: `host`
- 报告目录：`reports/load/example-k6-api-read-20260518`

## 被压测 URL

| 名称 | 方法 | URL | 预期状态 |
| --- | --- | --- | --- |
| front auth check-login | POST | `/front-api/api/auth/session/check-login` | 200 |
| open unsigned submission page | POST | `/open-api/api/submission/submission/page` | 401 |
| admin current user info | POST | `/admin-api/api/sys/current-user/info` | 200 |
| admin current user menus | POST | `/admin-api/api/sys/current-user/menus` | 200 |
| admin current user permissions | POST | `/admin-api/api/sys/current-user/perms` | 200 |
| admin dictionary page | POST | `/admin-api/api/sys/dict/page` | 200 |
| admin storage object tree | POST | `/admin-api/api/storage/object/tree` | 200 |

## 结果摘要

| 指标 | 值 | 阈值 | 结果 |
| --- | ---: | --- | --- |
| http_reqs.count | 306502 | - | - |
| http_reqs.rate | 638.03 req/s | - | - |
| http_req_failed.rate | 0 | `< 0.01` | 通过 |
| http_req_duration.avg | 13.32 ms | - | - |
| http_req_duration.p90 | 21.70 ms | - | - |
| http_req_duration.p95 | 25.85 ms | `< 1000 ms` | 通过 |
| http_req_duration.p99 | 52.18 ms | `< 2000 ms` | 通过 |
| http_req_duration.max | 328.61 ms | - | - |
| iterations.count | 43786 | - | - |
| checks.rate | 1 | `100%` | 通过 |
| checks.passes | 613004 | - | - |
| checks.fails | 0 | - | 通过 |
| vus_max | 200 | - | - |

## 测试环境

- 服务器：`example.test`
- 操作系统：Ubuntu 20.04.6 LTS
- Kernel：Linux 5.4.0-216-generic
- CPU：64 vCPU，Intel Xeon E5-2683 v4 @ 2.10GHz
- 内存：62 GiB
- Docker：28.1.1
- Docker Compose：v2.35.1
- Compose 入口：nginx 暴露 `18080 -> 80`
- 数据库：MySQL 8.4 容器，业务库 `sandwish`
- 服务组件：nginx、admin-api、front-api、open-api、MySQL、Redis、MinIO、RocketMQ namesrv/broker

## 镜像版本

| 镜像 | ID |
| --- | --- |
| `sandwish/admin-api:dev` | `40acd15fe477` |
| `sandwish/front-api:dev` | `855cb45dd8c4` |
| `sandwish/open-api:dev` | `2f3be48048fc` |
| `sandwish/nginx:dev` | `ceda5777fa0c` |
| `sandwish/k6:dev` | `eab2b09f3b5c` |
| `sandwish/mysql:8.4` | `1487abffa4fa` |
| `sandwish/redis:7.4-alpine` | `487efc061638` |
| `sandwish/rocketmq:5.4.0` | `e6efcb24a53c` |
| `sandwish/minio:RELEASE.2025-02-28T09-55-16Z` | `377fe6127f60` |

## 资源观察

| 组件 | CPU 峰值 | 内存峰值 | 观察 |
| --- | ---: | ---: | --- |
| nginx | 28.14% | 67.62 MiB | 随流量递增，未见异常 |
| admin-api | 460.58% | 975.1 MiB | 升压早期出现 CPU 瞬时峰值，后续稳定在低位到中位 |
| front-api | 18.64% | 734.5 MiB | 低负载 |
| open-api | 5.06% | 680.2 MiB | 低负载 |
| MySQL | 55.15% | 500.1 MiB | 中低负载，后段回落 |
| Redis | 5.17% | 33.21 MiB | 低负载 |
| MinIO | 3.66% | 115.6 MiB | 低负载 |
| RocketMQ broker | 94.08% | 2.477 GiB | 采样中出现一次较高 CPU，未造成请求失败或延迟异常 |
| RocketMQ namesrv | 8.63% | 391.3 MiB | 低负载 |

## 瓶颈判断

- 当前 200 VUs 阶梯下未出现明确性能瓶颈：错误率为 0，P99 仅 52.18 ms，服务端日志没有 5xx 或 SQL 字段漂移异常。
- 从资源采样看，admin-api 和 RocketMQ broker 曾出现 CPU 瞬时高点；但请求延迟、错误率和后续资源曲线均未显示持续退化，因此暂不判定为有效瓶颈。
- MySQL 最高 CPU 55.15%，Redis、MinIO、nginx、front-api、open-api 均有明显余量。

## 前置验证

- 本地 Docker image/container 已重建。
- 服务器 Docker image/container 已重建并启动。
- 发布前全量冒烟已通过。
- k6 执行前，本地其他 Docker container 已关闭。
- 压测 token 通过后台登录流程获取，报告不记录 token 或密码。

## 附件

- 原始 k6 summary：`reports/load/example-k6-api-read-20260518/k6-summary.json`
- 服务器环境快照：`reports/load/example-k6-api-read-20260518/server-environment.txt`
- Docker stats 采样：`reports/load/example-k6-api-read-20260518/docker-stats.txt`
