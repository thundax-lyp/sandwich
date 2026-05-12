package com.github.thundax.modules.auth.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class PrincipalCredentialId extends BaseLongId {

    private PrincipalCredentialId(Long value) {
        super(value);
    }

    public static PrincipalCredentialId of(Long value) {
        return new PrincipalCredentialId(value);
    }

    public static PrincipalCredentialId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
