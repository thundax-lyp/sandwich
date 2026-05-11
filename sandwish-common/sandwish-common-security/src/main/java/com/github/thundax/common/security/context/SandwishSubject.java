package com.github.thundax.common.security.context;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SandwishSubject implements Serializable {

    private String subjectId;
    private SandwishSubjectType subjectType = SandwishSubjectType.ANONYMOUS;
    private String displayName;
    private String token;
    private final Set<String> authorities = new LinkedHashSet<>();

    public SandwishSubject() {}

    public SandwishSubject(
            String subjectId,
            SandwishSubjectType subjectType,
            String displayName,
            String token,
            Collection<String> authorities) {
        this.subjectId = subjectId;
        this.subjectType = subjectType == null ? SandwishSubjectType.UNKNOWN : subjectType;
        this.displayName = displayName;
        this.token = token;
        setAuthorities(authorities);
    }

    public static SandwishSubject anonymous() {
        return new SandwishSubject();
    }

    public boolean isAuthenticated() {
        return subjectId != null && !subjectId.trim().isEmpty() && subjectType != SandwishSubjectType.ANONYMOUS;
    }

    public boolean hasAuthority(String authority) {
        return authorities.contains(authority);
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public SandwishSubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SandwishSubjectType subjectType) {
        this.subjectType = subjectType == null ? SandwishSubjectType.UNKNOWN : subjectType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<String> getAuthorities() {
        return Collections.unmodifiableSet(authorities);
    }

    public void setAuthorities(Collection<String> authorities) {
        this.authorities.clear();
        if (authorities == null) {
            return;
        }
        for (String authority : authorities) {
            if (authority != null && !authority.trim().isEmpty()) {
                this.authorities.add(authority);
            }
        }
    }
}
