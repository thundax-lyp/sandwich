package com.github.thundax.modules.auth.controller.request;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRegisterEmailCodeRequest implements Serializable {

    @NotBlank
    private String loginToken;

    @NotBlank
    private String email;

    @NotBlank
    private String captcha;
}
