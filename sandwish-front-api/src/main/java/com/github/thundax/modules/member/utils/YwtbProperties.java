package com.github.thundax.modules.member.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** @Auther: zhangrudong @Date: 2021/10/20 15:19 @Description: */
@Component
@ConfigurationProperties(prefix = "vltava.ywtb")
public class YwtbProperties {

    private String apiId;
    private String appId;
    private String appKey;
    private String baseUrl;
    private String thirdLoginUrl;
    private String loginBackUrl;
    private String logoutUrl;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getThirdLoginUrl() {
        return thirdLoginUrl;
    }

    public void setThirdLoginUrl(String thirdLoginUrl) {
        this.thirdLoginUrl = thirdLoginUrl;
    }

    public String getLoginBackUrl() {
        return loginBackUrl;
    }

    public void setLoginBackUrl(String loginBackUrl) {
        this.loginBackUrl = loginBackUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
}
