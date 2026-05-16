# Sandwich API Load Test Report

## Test Metadata

- 测试日期：
- 执行人：
- git commit：
- API 镜像：
- 压测脚本：
- 压测机：
- 被测环境：
- 数据规模：

## Environment

| Component | Deployment | CPU | Memory | Notes |
| --- | --- | --- | --- | --- |
| nginx |  |  |  |  |
| sandwish-admin-api |  |  |  |  |
| sandwish-front-api |  |  |  |  |
| sandwish-open-api |  |  |  |  |
| MySQL |  |  |  |  |
| Redis |  |  |  |  |
| MinIO |  |  |  |  |
| RocketMQ |  |  |  |  |

## Test Plan

| Phase | Command | Stages | Duration | Write Data |
| --- | --- | --- | --- | --- |
| smoke | `scripts/smoke/smoke-all.sh` | - | - | no |
| surface | `scripts/smoke/smoke-api-surface.sh` | - | - | no |
| baseline | `scripts/load/run-k6-api-read.sh` |  |  | no |
| stress |  |  |  | no |
| soak |  |  |  | no |

## Results

| Phase | QPS | Error Rate | Avg | P95 | P99 | Max | Result |
| --- | --- | --- | --- | --- | --- | --- | --- |
| baseline |  |  |  |  |  |  |  |
| stress |  |  |  |  |  |  |  |
| soak |  |  |  |  |  |  |  |

## Resource Metrics

| Component | CPU Peak | Memory Peak | Error Log | Notes |
| --- | --- | --- | --- | --- |
| nginx |  |  |  |  |
| admin-api |  |  |  |  |
| front-api |  |  |  |  |
| open-api |  |  |  |  |
| MySQL |  |  |  |  |
| Redis |  |  |  |  |
| MinIO |  |  |  |  |
| RocketMQ |  |  |  |  |

## Bottleneck Analysis

- 现象：
- 证据：
- 影响接口：
- 根因判断：
- 处理建议：

## Conclusion

- 是否通过：
- 当前容量边界：
- 进入生产前必须完成事项：
