package com.github.thundax.modules.member.controller.request;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberEmailRegisterRequest implements Serializable {

    @NotBlank
    private String loginToken;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String validateCode;
}
