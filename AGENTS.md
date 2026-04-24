# Repository Guidelines

## Read Order

- Read `docs/AGENT.md` first.
- For implementation work, read `docs/00-governance/ARCHITECTURE.md`.
- Do not treat root `README.md` as implementation authority.

## Working Rules

- Load the minimum docs needed for the task.
- Prefer the simplest workable solution.
- Do not add abstraction, config, directories, or helper layers without a concrete need.
- Keep the existing Maven multi-module structure and three-layer architecture.
- Do not migrate Bacon's DDD layer model into this project by default.

## Project Layout

- Root project: Maven multi-module, Java 8, root `pom.xml`
- Shared code: `sandwish-common/`
- Business code: `sandwish-biz/`
- Admin WAR application: `sandwish-admin-api/`
- Front WAR application: `sandwish-front-api/`
- AI routing docs: `docs/`

Main dependency direction:

- `sandwish-admin-api -> sandwish-biz -> sandwish-common`
- `sandwish-front-api -> sandwish-biz -> sandwish-common`

Main runtime chain:

- `HTTP/JSP/API -> Controller -> Service -> DAO/Mapper -> Database`

## Code Rules

- Java/XML/YAML use 4-space indentation.
- Base package: `com.github.thundax`
- Class: `PascalCase`
- Method/field: `camelCase`
- Constant: `UPPER_SNAKE_CASE`

Layer responsibilities:

- `Controller`: HTTP entry, request binding, session/security adaptation, response or JSP view selection
- `Service`: business flow, transaction boundary, validation, cross-DAO orchestration
- `DAO/Mapper`: persistence access and SQL mapping
- `JSP/static`: presentation and interaction, not core business rules

## Testing

- Tests live in `src/test/java`.
- Behavior changes require test updates.
- Shared framework or business changes should run `mvn test` when feasible.
- WAR-facing changes should run the relevant module package command when feasible.

## Commits

- Every file modification must be committed before ending the task.
- Commit format: `Type(domain): 中文说明`
- Split unrelated changes into separate commits.
- Commit message must state the concrete capability changed.
- Completed `TODO.md` items must be deleted, split, or narrowed in the same commit as the corresponding code, test, or document change.

Examples:

- `Feat(admin): 增加后台登录校验`
- `Fix(storage): 修复文件删除状态更新`
- `Docs(governance): 补充提交收口规则`
- `Test(sys): 补充用户查询测试`

## Security

- Do not commit credentials or environment-specific secrets in `application*.yml`.
- Keep database drivers and third-party binary updates deliberate, documented, and limited to the module that needs them.
