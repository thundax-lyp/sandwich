package com.github.thundax.modules.auth.controller.request;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRegisterSmsCodeRequest implements Serializable {

    @NotBlank
    private String loginToken;

    @NotBlank
    private String mobile;

    @NotBlank
    private String captcha;
}
