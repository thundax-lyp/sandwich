package com.github.thundax.modules.member.security;

import com.github.thundax.modules.member.entity.MemberAccessToken;
import com.github.thundax.modules.member.service.MemberAuthService;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class MemberAccessTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MEMBER_PERMISSION = "member";

    private final MemberAuthService memberAuthService;

    public MemberAccessTokenAuthenticationFilter(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);
        if (StringUtils.isNotBlank(accessToken)) {
            MemberAccessToken token = memberAuthService.getValidAccessToken(accessToken);
            if (token != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        new MemberSpringPrincipal(
                                String.valueOf(token.getMemberId().value())),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(MEMBER_PERMISSION)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.startsWithIgnoreCase(authorization, BEARER_PREFIX)) {
            return StringUtils.substring(authorization, BEARER_PREFIX.length());
        }
        return request.getParameter("accessToken");
    }
}
