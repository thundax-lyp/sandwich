# DO 注解规约基线

## 适用范围

DO 注解规约适用于同时满足以下条件的 Java 类：

- 文件名以 `DO.java` 或 `DataObject.java` 结尾
- 文件路径位于 `sandwish-infra/src/main/java/com/github/thundax/modules/*/persistence/dataobject/`
- 对象由 MyBatis Mapper 持久化到数据库表

当前扫描结果：

- DO 类总数：18
- 数据库表映射 DO：18
- Redis-only DO：0
- DataObject 类：0

数据库表映射 DO 当前为：

- `com.github.thundax.modules.auth.persistence.dataobject.OAuthAuthorizationDO`
- `com.github.thundax.modules.auth.persistence.dataobject.OAuthClientDO`
- `com.github.thundax.modules.auth.persistence.dataobject.PrincipalCredentialDO`
- `com.github.thundax.modules.auth.persistence.dataobject.PrincipalIdentityDO`
- `com.github.thundax.modules.auth.persistence.dataobject.PrincipalLoginEventDO`
- `com.github.thundax.modules.member.persistence.dataobject.MemberDO`
- `com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadPartDO`
- `com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadSessionDO`
- `com.github.thundax.modules.storage.persistence.dataobject.StoredObjectDO`
- `com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO`
- `com.github.thundax.modules.sys.persistence.dataobject.DepartmentDO`
- `com.github.thundax.modules.sys.persistence.dataobject.DictDO`
- `com.github.thundax.modules.sys.persistence.dataobject.LogDO`
- `com.github.thundax.modules.sys.persistence.dataobject.MenuDO`
- `com.github.thundax.modules.sys.persistence.dataobject.MenuRoleDO`
- `com.github.thundax.modules.sys.persistence.dataobject.RoleDO`
- `com.github.thundax.modules.sys.persistence.dataobject.UserDO`
- `com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO`

Redis-only DO 当前为：无。

缓存运行态对象使用 DAO implementation 内部 `CacheDTO`，不使用 `DO/DataObject` 命名，避免和数据库持久化对象混淆。

## 必需类级注解

每个数据库表映射 DO 类应且仅应声明以下类级注解：

- `@Getter`
- `@Setter`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@TableName`

字段级注解不属于本基线范围，例如 `@TableId`、`@TableField` 不应由 DO 类级注解规则检查。字段级主键规则归属 [`DATABASE-RULES.md`](../00-governance/DATABASE-RULES.md) 的 `Primary Key Rules`。

## 规则归属

DO 注解规约的测试支撑归属于 `sandwish-common/sandwish-common-test`，包名使用项目基线约定的 `com.github.thundax.common.test.architecture`。

规则名称为 `MODEL_DATA_OBJECT_REQUIRED_ANNOTATIONS`。

## 当前缺口

当前数据库表映射 DO 没有缺失必需类级注解。

当前数据库表映射 DO 没有声明必需集合外的额外类级注解。
