package com.github.thundax.modules.auth.config;

import com.github.thundax.common.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wdit
 */
@ConfigurationProperties(prefix = "vltava.auth")
public class AuthProperties {

    /*** 最大同时登录用户数 ***/
    private int maxLoginCount = 100;
    /*** 最大同时在线用户数 ***/
    private int maxOnlineCount = 200;
    /*** 登录超时时间（秒） ***/
    private int loginExpiredSeconds = 300;
    /*** 会话超时时间（秒） ***/
    private int tokenExpiredSeconds = 300;
    /*** 验证码白名单，用于压力测试 ***/
    private String whiteCaptcha = StringUtils.EMPTY;
    /*** 短信验证码模板 ***/
    private String validateCodeTemplate = StringUtils.EMPTY;

    public int getMaxLoginCount() {
        return maxLoginCount;
    }

    public void setMaxLoginCount(int maxLoginCount) {
        this.maxLoginCount = maxLoginCount;
    }

    public int getMaxOnlineCount() {
        return maxOnlineCount;
    }

    public void setMaxOnlineCount(int maxOnlineCount) {
        this.maxOnlineCount = maxOnlineCount;
    }

    public int getLoginExpiredSeconds() {
        return loginExpiredSeconds;
    }

    public void setLoginExpiredSeconds(int loginExpiredSeconds) {
        this.loginExpiredSeconds = loginExpiredSeconds;
    }

    public int getTokenExpiredSeconds() {
        return tokenExpiredSeconds;
    }

    public void setTokenExpiredSeconds(int tokenExpiredSeconds) {
        this.tokenExpiredSeconds = tokenExpiredSeconds;
    }

    public String getWhiteCaptcha() {
        return whiteCaptcha;
    }

    public void setWhiteCaptcha(String whiteCaptcha) {
        this.whiteCaptcha = whiteCaptcha;
    }

    public String getValidateCodeTemplate() {
        return validateCodeTemplate;
    }

    public void setValidateCodeTemplate(String validateCodeTemplate) {
        this.validateCodeTemplate = validateCodeTemplate;
    }
}
