package com.github.thundax.common.security.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class CurrentUser implements Serializable {

    private String userId;
    private String loginName;
    private String displayName;
    private final Set<String> authorities = new LinkedHashSet<>();

    public CurrentUser() {}

    public CurrentUser(String userId, String loginName, String displayName, Collection<String> authorities) {
        this.userId = userId;
        this.loginName = loginName;
        this.displayName = displayName;
        setAuthorities(authorities);
    }

    public static CurrentUser anonymous() {
        return new CurrentUser();
    }

    public boolean isAuthenticated() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean hasAuthority(String authority) {
        return authorities.contains(authority);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
