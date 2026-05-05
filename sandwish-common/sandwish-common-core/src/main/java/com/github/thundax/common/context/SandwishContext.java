package com.github.thundax.common.context;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SandwishContext implements Serializable {

    private String requestId;
    private String userId;
    private String loginName;
    private String token;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    public SandwishContext() {}

    public SandwishContext(SandwishContext source) {
        if (source != null) {
            requestId = source.requestId;
            userId = source.userId;
            loginName = source.loginName;
            token = source.token;
            attributes.putAll(source.attributes);
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void putAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
