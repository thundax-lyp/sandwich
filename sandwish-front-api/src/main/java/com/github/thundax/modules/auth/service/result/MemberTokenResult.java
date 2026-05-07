package com.github.thundax.modules.auth.service.result;

import com.github.thundax.common.id.EntityId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberTokenResult {
    private EntityId memberId;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
}
