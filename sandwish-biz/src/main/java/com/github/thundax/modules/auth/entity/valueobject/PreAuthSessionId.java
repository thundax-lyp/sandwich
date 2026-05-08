package com.github.thundax.modules.auth.entity.valueobject;

import com.github.thundax.common.id.BaseStringId;

public final class PreAuthSessionId extends BaseStringId {

    private PreAuthSessionId(String value) {
        super(value);
    }

    public static PreAuthSessionId of(String value) {
        return new PreAuthSessionId(value);
    }

    public static PreAuthSessionId ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
