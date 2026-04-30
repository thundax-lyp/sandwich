# Domain Value Rules

本文档定义 Sandwich 领域值对象和领域枚举的固定形状。

## Enum Shape

领域枚举固定使用业务语义命名，不使用 `0/1`、`Y/N`、`YES/NO` 作为领域值。

```java
public enum XxxStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static XxxStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown xxx status: " + value));
    }
}
```

固定规则：

- enum 常量使用 `UPPER_SNAKE_CASE` 业务语义名。
- `value()` 固定返回 `name()`。
- `from(String value)` 固定大小写不敏感解析。
- 解析失败统一抛 `BizException`。
- 不返回 `null`。
- 不静默 fallback 到默认值。
- 不使用 `IllegalArgumentException` 表达业务枚举解析失败。
- 新写入值必须使用 `value()`。

## Legacy Value Compatibility

旧系统中已存在的 `0/1` 等历史值只能作为迁移期兼容入口。

兼容规则：

- 兼容分支必须显式写在 `from(String value)` 中。
- 新写入不得继续写历史值。
- 领域对象内部不得继续比较历史字符串。
- 迁移完成后应删除历史值兼容分支。

## Layer Boundary

Controller、Request、Response、DO、Mapper 和数据库边界可以继续使用 `String` 承载外部值。

转换边界固定为：

- Controller / InterfaceAssembler：HTTP 字符串与领域枚举互转。
- PersistenceAssembler：DO 字符串与领域枚举互转。
- Domain Entity / Service：只使用领域枚举和值对象，不直接比较状态字符串。

## Storage Rule

枚举字段数据库统一使用 `varchar`。

持久化值固定写入 `enum.value()`，默认就是 enum 常量名。
