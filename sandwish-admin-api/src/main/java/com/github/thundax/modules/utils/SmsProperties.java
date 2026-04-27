package com.github.thundax.modules.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** @Auther: zhangrudong @Date: 2021/11/17 13:52 @Description: */
@Configuration
@ConfigurationProperties(prefix = "vltava.sms")
public class SmsProperties {

    private String url;
    private String username;
    private String password;
    private String fsyhm;
    private String fsyhxm;
    private String fsyhdw;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFsyhm() {
        return fsyhm;
    }

    public void setFsyhm(String fsyhm) {
        this.fsyhm = fsyhm;
    }

    public String getFsyhxm() {
        return fsyhxm;
    }

    public void setFsyhxm(String fsyhxm) {
        this.fsyhxm = fsyhxm;
    }

    public String getFsyhdw() {
        return fsyhdw;
    }

    public void setFsyhdw(String fsyhdw) {
        this.fsyhdw = fsyhdw;
    }
}
