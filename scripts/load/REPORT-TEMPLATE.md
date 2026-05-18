# Sandwich k6 API 读链路压测报告

## 测试元信息

- 测试日期：
- 执行人：
- git commit：
- API 镜像：
- 压测脚本：
- 压测机：
- 被测环境：
- 数据规模：

## 环境说明

| 组件 | 部署方式 | CPU | 内存 | 备注 |
| --- | --- | --- | --- | --- |
| nginx |  |  |  |  |
| sandwish-admin-api |  |  |  |  |
| sandwish-front-api |  |  |  |  |
| sandwish-open-api |  |  |  |  |
| MySQL |  |  |  |  |
| Redis |  |  |  |  |
| MinIO |  |  |  |  |
| RocketMQ |  |  |  |  |

## 测试计划

| 阶段 | 命令 | 阶梯 | 持续时间 | 是否写入数据 |
| --- | --- | --- | --- | --- |
| smoke | `scripts/smoke/smoke-all.sh` | - | - | 否 |
| API surface 冒烟 | `scripts/smoke/smoke-api-surface.sh` | - | - | 否 |
| baseline | `scripts/load/run-k6-api-read.sh` |  |  | 否 |
| stress |  |  |  | 否 |
| soak |  |  |  | 否 |

## 测试结果

| 阶段 | QPS | 错误率 | Avg | P95 | P99 | Max | 结果 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| baseline |  |  |  |  |  |  |  |
| stress |  |  |  |  |  |  |  |
| soak |  |  |  |  |  |  |  |

## 资源指标

| 组件 | CPU 峰值 | 内存峰值 | 错误日志 | 备注 |
| --- | --- | --- | --- | --- |
| nginx |  |  |  |  |
| admin-api |  |  |  |  |
| front-api |  |  |  |  |
| open-api |  |  |  |  |
| MySQL |  |  |  |  |
| Redis |  |  |  |  |
| MinIO |  |  |  |  |
| RocketMQ |  |  |  |  |

## 瓶颈分析

- 现象：
- 证据：
- 影响接口：
- 根因判断：
- 处理建议：

## 结论

- 是否通过：
- 当前容量边界：
- 进入生产前必须完成事项：
