# DO 注解规约基线

## 适用范围

DO 注解规约适用于同时满足以下条件的 Java 类：

- 文件名以 `DO.java` 或 `DataObject.java` 结尾
- 文件路径位于 `sandwish-infra/src/main/java/com/github/thundax/modules/*/persistence/dataobject/`
- 对象由 MyBatis Mapper 持久化到数据库表

当前扫描结果：

- DO 类总数：15
- 数据库表映射 DO：12
- Redis-only DO：3
- DataObject 类：0

数据库表映射 DO 当前为：

- `com.github.thundax.modules.assist.persistence.dataobject.SignatureDO`
- `com.github.thundax.modules.member.persistence.dataobject.MemberDO`
- `com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO`
- `com.github.thundax.modules.storage.persistence.dataobject.StorageDO`
- `com.github.thundax.modules.sys.persistence.dataobject.DictDO`
- `com.github.thundax.modules.sys.persistence.dataobject.LogDO`
- `com.github.thundax.modules.sys.persistence.dataobject.MenuDO`
- `com.github.thundax.modules.sys.persistence.dataobject.OfficeDO`
- `com.github.thundax.modules.sys.persistence.dataobject.RoleDO`
- `com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO`
- `com.github.thundax.modules.sys.persistence.dataobject.UserDO`
- `com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO`

Redis-only DO 当前为：

- `com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO`
- `com.github.thundax.modules.auth.persistence.dataobject.AccessTokenDO`
- `com.github.thundax.modules.auth.persistence.dataobject.LoginFormDO`

Redis-only DO 不纳入数据库表 DO 注解门禁，因为它们没有数据库表名，不应补 `@TableName`。这 3 个类保留当前命名，后续如要迁移出 DO 命名体系，应单独拆分重命名任务，避免和数据库表注解门禁混在同一次行为变更中。

## 必需类级注解

每个数据库表映射 DO 类应且仅应声明以下类级注解：

- `@Getter`
- `@Setter`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@TableName`

字段级注解不属于本基线范围，例如 `@TableId`、`@TableField` 不应由 DO 类级注解规则检查。

## 规则归属

DO 注解规约的测试支撑归属于 `sandwish-common/sandwish-common-test`，包名使用项目基线约定的 `com.github.thundax.common.test.architecture`。

规则名称为 `MODEL_DATA_OBJECT_REQUIRED_ANNOTATIONS`。

## 当前缺口

当前数据库表映射 DO 的类级注解缺口如下：

- 12 个数据库表映射 DO 均缺少 `@Getter`
- 12 个数据库表映射 DO 均缺少 `@Setter`
- 12 个数据库表映射 DO 均缺少 `@AllArgsConstructor`
- 除 `DictDO` 外，其他 11 个数据库表映射 DO 均缺少 `@TableName`

当前数据库表映射 DO 没有声明必需集合外的额外类级注解。
