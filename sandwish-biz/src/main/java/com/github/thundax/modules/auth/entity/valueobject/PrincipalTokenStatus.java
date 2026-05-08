package com.github.thundax.modules.auth.entity.valueobject;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public final class PrincipalTokenStatus {

    public static final PrincipalTokenStatus ACTIVE = new PrincipalTokenStatus("ACTIVE");
    public static final PrincipalTokenStatus USED = new PrincipalTokenStatus("USED");
    public static final PrincipalTokenStatus REVOKED = new PrincipalTokenStatus("REVOKED");
    public static final PrincipalTokenStatus EXPIRED = new PrincipalTokenStatus("EXPIRED");

    private final String value;

    private PrincipalTokenStatus(String value) {
        this.value = value;
    }

    public static PrincipalTokenStatus of(String value) {
        if (StringUtils.equalsIgnoreCase(ACTIVE.value, value)) {
            return ACTIVE;
        }
        if (StringUtils.equalsIgnoreCase(USED.value, value)) {
            return USED;
        }
        if (StringUtils.equalsIgnoreCase(REVOKED.value, value)) {
            return REVOKED;
        }
        if (StringUtils.equalsIgnoreCase(EXPIRED.value, value)) {
            return EXPIRED;
        }
        throw new IllegalArgumentException("Unsupported principal token status: " + value);
    }

    public String value() {
        return value;
    }

    public boolean isActive() {
        return ACTIVE.equals(this);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof PrincipalTokenStatus)) {
            return false;
        }
        PrincipalTokenStatus status = (PrincipalTokenStatus) that;
        return Objects.equals(value, status.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
