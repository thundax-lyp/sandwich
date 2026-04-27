# Request 注解规约基线

## 适用范围

Request 注解规约适用于同时满足以下条件的 Java 类：

- 文件名以 `Request.java` 结尾
- 文件路径位于 API 请求包下，当前为 `sandwish-admin-api/src/main/java/com/github/thundax/modules/*/request/`

当前扫描结果：

- Request 类总数：43
- Admin API Request 类：43
- Front API Request 类：0
- Shared、business、infra Request 类：0

如果后续在前台 API 或共享包中新增 API 请求模型，默认纳入同一规约；除非单独 TODO 明确收窄扫描范围。

## 必需类级注解

每个 Request 类应且仅应声明以下类级注解：

- `@Getter`
- `@Setter`
- `@ApiModel`
- `@JsonInclude(JsonInclude.Include.NON_NULL)`
- `@JsonIgnoreProperties(ignoreUnknown = true)`

字段级注解不属于本基线范围，例如 `@ApiModelProperty` 不应由 Request 类级注解规则检查。

## 当前缺口

`sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/PersonalAvatarUploadRequest.java`

- 已有 `@Getter`
- 已有 `@Setter`
- 已有 `@ApiModel`
- 缺少 `@JsonInclude(JsonInclude.Include.NON_NULL)`
- 缺少 `@JsonIgnoreProperties(ignoreUnknown = true)`

当前没有 Request 类声明必需集合外的额外类级注解。
