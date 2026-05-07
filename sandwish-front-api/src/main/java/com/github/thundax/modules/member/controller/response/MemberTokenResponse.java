package com.github.thundax.modules.member.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberTokenResponse implements Serializable {
    private String memberId;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
}
