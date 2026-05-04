package com.github.thundax.modules.auth.service.result;

import com.github.thundax.modules.auth.entity.AccessToken;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenRefreshResult {
    private AccessToken accessToken;
    private String refreshToken;

    public AuthTokenRefreshResult(AccessToken accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
