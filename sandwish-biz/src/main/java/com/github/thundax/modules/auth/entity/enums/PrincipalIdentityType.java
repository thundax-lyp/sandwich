package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum PrincipalIdentityType {
    USER_ACCOUNT(PrincipalType.USER, "ACCOUNT"),
    USER_MOBILE(PrincipalType.USER, "MOBILE"),
    USER_EMAIL(PrincipalType.USER, "EMAIL"),
    USER_WECOM(PrincipalType.USER, "WECOM"),
    USER_GITHUB(PrincipalType.USER, "GITHUB"),
    MEMBER_ACCOUNT(PrincipalType.MEMBER, "ACCOUNT"),
    MEMBER_MOBILE(PrincipalType.MEMBER, "MOBILE"),
    MEMBER_EMAIL(PrincipalType.MEMBER, "EMAIL");

    private final PrincipalType principalType;
    private final String identityName;

    PrincipalIdentityType(PrincipalType principalType, String identityName) {
        this.principalType = principalType;
        this.identityName = identityName;
    }

    public String value() {
        return name();
    }

    public PrincipalType principalType() {
        return principalType;
    }

    public String identityName() {
        return identityName;
    }

    public boolean isUserPrincipal() {
        return PrincipalType.USER == principalType;
    }

    public boolean isMemberPrincipal() {
        return PrincipalType.MEMBER == principalType;
    }

    public boolean isAccount() {
        return "ACCOUNT".equals(identityName);
    }

    public boolean isMobile() {
        return "MOBILE".equals(identityName);
    }

    public boolean isEmail() {
        return "EMAIL".equals(identityName);
    }

    public static PrincipalIdentityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown principal identity type: " + value));
    }

    public static PrincipalIdentityType from(PrincipalType principalType, String identityName) {
        return Arrays.stream(values())
                .filter(item -> item.principalType == principalType && item.identityName.equalsIgnoreCase(identityName))
                .findFirst()
                .orElseThrow(() ->
                        new BizException("Unknown principal identity type: " + principalType + ":" + identityName));
    }
}
