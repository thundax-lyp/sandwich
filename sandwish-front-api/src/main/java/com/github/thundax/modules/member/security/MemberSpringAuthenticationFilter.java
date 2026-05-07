package com.github.thundax.modules.member.security;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.utils.RsaSessionUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class MemberSpringAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String DEFAULT_CAPTCHA_PARAM = "validateCode";
    public static final String DEFAULT_LOGIN_TOKEN_PARAM = "loginToken";

    private final RsaSessionUtils rsaSessionUtils;

    public MemberSpringAuthenticationFilter(RsaSessionUtils rsaSessionUtils) {
        this.rsaSessionUtils = rsaSessionUtils;
        setFilterProcessesUrl("/auth/login");
        setAuthenticationSuccessHandler(this::writeSuccess);
        setAuthenticationFailureHandler(this::writeFailure);
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

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, decryptPassword(request.getParameter(DEFAULT_LOGIN_TOKEN_PARAM), password));
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(authRequest);
    }

    private String decryptPassword(String loginToken, String encryptedValue) {
        if (StringUtils.isEmpty(encryptedValue)) {
            return StringUtils.EMPTY;
        }
        return rsaSessionUtils.decryptRsaValue(loginToken, encryptedValue);
    }

    private void writeSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        MemberSpringPrincipal principal = authentication.getPrincipal() instanceof MemberSpringPrincipal
                ? (MemberSpringPrincipal) authentication.getPrincipal()
                : null;
        writeJson(response, HttpStatus.OK, MemberLoginInterfaceAssembler.toLoginStatusResponse(principal));
    }

    private void writeFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException)
            throws IOException {
        writeJson(
                response,
                HttpStatus.UNAUTHORIZED,
                MemberLoginInterfaceAssembler.toLoginFailureResponse(authenticationException.getMessage()));
    }

    private void writeJson(HttpServletResponse response, HttpStatus status, MemberLoginStatusResponse body)
            throws IOException {
        String jsonString = JsonUtils.toJson(body);
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }
}
