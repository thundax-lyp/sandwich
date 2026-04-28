package com.github.thundax.modules.auth.persistence.dataobject;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginFormDO implements Serializable {

    private String loginToken;
    private List<String> refreshTokenList;
    private String captcha;
    private String mobile;
    private String mobileValidateCode;
    private Integer expiredSeconds;
    private String checkCode;
    private String publicKey;
    private String privateKey;
}
