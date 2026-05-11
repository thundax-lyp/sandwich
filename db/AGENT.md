# DB Agent

此文件只给 AI / harness 读，用于定义 `db/` 目录下数据库脚本的读取与执行规则。

## 1. Start Here

处理数据库脚本相关任务前，固定先读：

1. [`../docs/AGENT.md`](../docs/AGENT.md)
2. [`../docs/00-governance/DATABASE-RULES.md`](../docs/00-governance/DATABASE-RULES.md)
3. 对应业务域数据库设计文档，位于 [`../docs/20-database/`](../docs/20-database)

规则：

- 只读当前任务涉及的业务域数据库设计文档。
- 先读规则与设计，再改 `schema/` 或 `data/`。
- 不默认读取所有业务域数据库文档。

## 2. Directory Map

```text
db/
├── AGENT.md
├── schema/
│   ├── system.sql
│   ├── auth.sql
│   ├── storage.sql
│   ├── member.sql
│   └── audit.sql
└── data/
    ├── system.sql
    ├── auth.sql
    ├── storage.sql
    ├── member.sql
    └── audit.sql
```

- `schema/`: DDL scripts only.
- `data/`: seed and initialization scripts only.
- 文件名固定使用业务域名小写。
- 新增业务域时，优先保持 `schema/<domain>.sql` 与 `data/<domain>.sql` 成对出现。

## 3. Domain Mapping

- `System`: `schema/system.sql`, `data/system.sql`
- `Auth`: `schema/auth.sql`, `data/auth.sql`
- `Storage`: `schema/storage.sql`, `data/storage.sql`
- `Member`: `schema/member.sql`, `data/member.sql`
- `Audit`: `schema/audit.sql`, `data/audit.sql`
- `Assist`: 当前无 schema/data 脚本，异步任务只使用 Redis / JetCache 运行态缓存

## 4. Execution Order

固定按以下顺序执行：

1. `db/schema/system.sql`
2. `db/data/system.sql`
3. `db/schema/auth.sql`
4. `db/data/auth.sql`
5. `db/schema/storage.sql`
6. `db/data/storage.sql`
7. `db/schema/member.sql`
8. `db/data/member.sql`
9. `db/schema/audit.sql`
10. `db/data/audit.sql`

执行原则：

- 先执行 `schema/`，再执行 `data/`。
- `auth` 初始化依赖 `system` 的用户与登录标识主数据。
- `storage` 当前不依赖其他业务域初始化数据。
- `member` 当前不依赖其他业务域初始化数据。
- `assist` 当前只使用 Redis / JetCache 运行态缓存，不提供 schema 或 data 脚本；数据库边界见 `../docs/20-database/ASSIST-DATABASE-DESIGN.md`。

## 5. Change Policy

- 修改表结构前，先同步对应 `../docs/20-database/*-DATABASE-DESIGN.md`。
- 修改固定初始化数据前，先同步对应 `../docs/10-requirements/*-REQUIREMENTS.md`。
- 若规则层发生变化，先同步 [`../docs/00-governance/DATABASE-RULES.md`](../docs/00-governance/DATABASE-RULES.md)。
- 后续若接入数据库迁移工具，本目录默认作为基线来源，不直接删除已有脚本。

## 6. Seed Rules

- 初始化数据的数据库主键使用固定雪花 ID。
- 初始化数据的业务键使用固定业务值。
- 初始化脚本必须保持幂等，重复执行后收敛到固定状态。
- 密码和客户端密钥不在表中保存明文。
- 如脚本依赖其他业务域主数据，依赖关系必须写在数据库设计文档或本文件中。
