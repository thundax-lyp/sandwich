package com.github.thundax.common.security.context;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SandwishSubject implements Serializable {

    @Setter
    private String subjectId;

    private SandwishSubjectType subjectType = SandwishSubjectType.ANONYMOUS;

    @Setter
    private String displayName;

    @Setter
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

    public void setSubjectType(SandwishSubjectType subjectType) {
        this.subjectType = subjectType == null ? SandwishSubjectType.UNKNOWN : subjectType;
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
