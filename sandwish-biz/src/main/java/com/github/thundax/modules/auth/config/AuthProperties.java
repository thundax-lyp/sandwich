package com.github.thundax.modules.auth.config;

import com.github.thundax.common.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * auth配置。
 */
@ConfigurationProperties(prefix = "vltava.auth")
public class AuthProperties {

    private int maxLoginCount = 10;

    private int maxOnlineCount = 200;

    private int loginExpiredSeconds = 300;

    private String whiteCaptcha = StringUtils.EMPTY;

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
