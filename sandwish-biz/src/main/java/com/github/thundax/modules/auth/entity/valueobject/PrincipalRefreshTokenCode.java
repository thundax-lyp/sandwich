package com.github.thundax.modules.auth.entity.valueobject;

import com.github.thundax.common.id.BaseStringId;

public final class PrincipalRefreshTokenCode extends BaseStringId {

    private PrincipalRefreshTokenCode(String value) {
        super(value);
    }

    public static PrincipalRefreshTokenCode of(String value) {
        return new PrincipalRefreshTokenCode(value);
    }

    public static PrincipalRefreshTokenCode ofNullable(String value) {
        return value == null ? null : of(value);
    }
}
