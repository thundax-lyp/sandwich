package com.github.thundax.modules.sys.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class MenuIdCodec {

    private MenuIdCodec() {}

    public static MenuId toDomain(Long value) {
        return MenuId.ofNullable(value);
    }

    public static Long toValue(MenuId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(MenuId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<MenuId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(MenuIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<MenuId> ids) {
        return ids == null ? null : ids.stream().map(MenuIdCodec::toValue).collect(Collectors.toList());
    }
}
