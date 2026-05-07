package com.github.thundax.common.id;

public final class EntityId extends BaseLongId {

    private EntityId(Long value) {
        super(value);
    }

    public static EntityId of(Long value) {
        return new EntityId(value);
    }

    public static EntityId of(String value) {
        return new EntityId(parse(value));
    }

    public static EntityId ofNullable(Long value) {
        return value == null ? null : of(value);
    }

    public static EntityId ofNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : of(value);
    }

    private static Long parse(String value) {
        if (value == null) {
            throw new NullPointerException("id value must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
        try {
            return Long.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("id must be a long value", ex);
        }
    }
}
