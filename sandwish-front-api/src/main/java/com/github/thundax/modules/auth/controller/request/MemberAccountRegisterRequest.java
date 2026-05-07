package com.github.thundax.modules.auth.controller.request;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberAccountRegisterRequest implements Serializable {

    @NotBlank
    private String loginToken;

    @NotBlank
    private String name;

    @NotBlank
    private String account;

    @NotBlank
    private String password;

    @NotBlank
    private String captcha;
}
