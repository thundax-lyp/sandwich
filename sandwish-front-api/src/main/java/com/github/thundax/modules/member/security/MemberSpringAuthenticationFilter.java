package com.github.thundax.modules.member.security;

import com.github.thundax.modules.member.utils.RsaSessionUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** 前台 Spring Security 登录认证过滤器。 */
public class MemberSpringAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String DEFAULT_CAPTCHA_PARAM = "validateCode";

    public MemberSpringAuthenticationFilter() {
        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        if (username == null) {
            username = StringUtils.EMPTY;
        }
        if (password == null) {
            password = StringUtils.EMPTY;
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(
                        username, decryptPassword(request, password));
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(authRequest);
    }

    private String decryptPassword(HttpServletRequest request, String encryptedValue) {
        if (StringUtils.isEmpty(encryptedValue)) {
            return StringUtils.EMPTY;
        }
        return RsaSessionUtils.decryptRsaValue(request, encryptedValue);
    }
}
