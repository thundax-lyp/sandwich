package com.github.thundax.modules.member.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberLoginFormResponse implements Serializable {
    private String loginToken;
    private List<String> refreshTokenList;
    private String captcha;
    private Integer expiredSeconds;
    private String checkCode;
    private String publicKey;
}
