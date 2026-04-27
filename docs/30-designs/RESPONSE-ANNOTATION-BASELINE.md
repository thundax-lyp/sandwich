# Response 注解规约基线

## 适用范围

Response 注解规约适用于同时满足以下条件的 Java 类：

- 文件名以 `Response.java` 结尾
- 文件路径位于 API 响应包下，当前为：
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/*/response/`
  - `sandwish-front-api/src/main/java/com/github/thundax/modules/*/response/`

当前扫描结果：

- Response 类总数：29
- Admin API Response 类：27
- Front API Response 类：1
- Shared、infra Response 类：0
- Biz Response 类：1

`sandwish-biz/src/main/java/com/github/thundax/modules/assist/plugins/koal/sign/BaseResponse.java` 是签名插件协议对象，不位于 API 响应包内，当前不纳入 API Response 注解门禁。

如果后续在 API 响应包中新增响应模型，默认纳入同一规约；除非单独 TODO 明确收窄扫描范围。

## 必需类级注解

每个 API Response 类应且仅应声明以下类级注解：

- `@Getter`
- `@Setter`
- `@ApiModel`
- `@JsonInclude(JsonInclude.Include.NON_NULL)`
- `@JsonIgnoreProperties(ignoreUnknown = true)`

字段级注解不属于本基线范围，例如 `@ApiModelProperty` 不应由 Response 类级注解规则检查。

## 规则归属

Response 注解规约的测试支撑归属于 `sandwish-common/sandwish-common-test`，包名使用项目基线约定的 `com.github.thundax.common.test.architecture`。

## 当前缺口

当前 API Response 类没有缺失必需类级注解。

当前 API Response 类没有声明必需集合外的额外类级注解。
