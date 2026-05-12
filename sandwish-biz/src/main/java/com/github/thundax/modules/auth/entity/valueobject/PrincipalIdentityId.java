package com.github.thundax.modules.auth.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class PrincipalIdentityId extends BaseLongId {

    private PrincipalIdentityId(Long value) {
        super(value);
    }

    public static PrincipalIdentityId of(Long value) {
        return new PrincipalIdentityId(value);
    }

    public static PrincipalIdentityId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
