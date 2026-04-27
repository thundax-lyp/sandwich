package com.github.thundax.modules.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/** access token实体。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken implements Serializable {

    public static final int REFRESH_TOKEN_SIZE = 5;

    /** * 登录令牌 ** */
    private String token;
    /** * 用户id ** */
    private String userId;
    /** * 校验码，由 AuthUtils.currentCheckCode 生成 ** */
    private String checkCode;

    public AccessToken() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }
}
