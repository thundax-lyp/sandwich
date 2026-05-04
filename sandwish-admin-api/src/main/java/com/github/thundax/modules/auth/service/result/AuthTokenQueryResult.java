package com.github.thundax.modules.auth.service.result;

import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;
import com.github.thundax.modules.sys.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenQueryResult {
    private boolean active;
    private String token;
    private AuthSession session;
    private OAuthAccessToken oauthAccessToken;
    private User user;
    private String username;

    public static AuthTokenQueryResult inactive(String token) {
        AuthTokenQueryResult result = new AuthTokenQueryResult();
        result.setToken(token);
        return result;
    }

    public static AuthTokenQueryResult active(String token, AuthSession session, User user, String username) {
        AuthTokenQueryResult result = new AuthTokenQueryResult();
        result.setActive(true);
        result.setToken(token);
        result.setSession(session);
        result.setUser(user);
        result.setUsername(username);
        return result;
    }

    public static AuthTokenQueryResult active(
            String token, OAuthAccessToken oauthAccessToken, User user, String username) {
        AuthTokenQueryResult result = new AuthTokenQueryResult();
        result.setActive(true);
        result.setToken(token);
        result.setOauthAccessToken(oauthAccessToken);
        result.setUser(user);
        result.setUsername(username);
        return result;
    }
}
