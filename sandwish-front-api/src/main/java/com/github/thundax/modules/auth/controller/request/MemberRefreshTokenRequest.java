package com.github.thundax.modules.auth.controller.request;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRefreshTokenRequest implements Serializable {
    @NotBlank
    private String refreshToken;
}
