# Sandwich Load Scripts

本目录保存 k6 API 读链路压测脚本和报告模板。长期操作手册见 `docs/00-governance/how-to/HOW-TO-K6-API-READ-LOAD-TEST.md`。

## Files

- `k6-api-read.js`: admin/front/open 读链路 k6 压测脚本。
- `run-k6-api-read.sh`: k6 执行入口，默认使用 `sandwish/k6` Docker 镜像。
- `.env.example`: 压测环境变量样例。
- `REPORT-TEMPLATE.md`: 人工测试报告模板。

## Usage

```bash
cp .env.test.example .env.load
scripts/load/run-k6-api-read.sh
```

生成结果默认写入：

- `reports/load/k6-summary.json`
- `reports/load/k6-report.md`

默认脚本只压读链路和认证边界，不执行 create / update / delete / upload 写入压测。

`scripts/load/.env.example` 只保留 load 专项变量样例。部署后联动冒烟和压测时，固定优先使用根目录 `.env.test.example`。

默认运行镜像为 `SANDWICH_LOAD_K6_IMAGE=sandwish/k6:dev`。只有明确设置 `SANDWICH_LOAD_USE_LOCAL_K6=true` 时，脚本才使用本机 `k6` 命令。
