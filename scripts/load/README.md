# Sandwich Load Scripts

本目录保存 API 压测脚本和报告模板。长期操作手册见 `docs/00-governance/how-to/HOW-TO-API-LOAD-TEST.md`。

## Files

- `k6-api-read.js`: admin/front/open 读链路 k6 压测脚本。
- `run-k6-api-read.sh`: k6 执行入口，优先使用本机 `k6`，没有本机 k6 时使用 `grafana/k6` Docker 镜像。
- `.env.example`: 压测环境变量样例。
- `REPORT-TEMPLATE.md`: 人工测试报告模板。

## Usage

```bash
cp scripts/load/.env.example .env.load
scripts/load/run-k6-api-read.sh
```

生成结果默认写入：

- `reports/load/k6-summary.json`
- `reports/load/k6-report.md`

默认脚本只压读链路和认证边界，不执行 create / update / delete / upload 写入压测。
