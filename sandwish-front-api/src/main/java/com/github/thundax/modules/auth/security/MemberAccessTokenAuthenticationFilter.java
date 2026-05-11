package com.github.thundax.modules.auth.security;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.query.MemberAuthQuery;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
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
            PrincipalAccessToken token = memberAuthService.getValidAccessToken(memberAuthQuery(accessToken));
            if (token != null) {
                SandwishContextHolder.setSubject(new SandwishSubject(
                        String.valueOf(token.getPrincipalKey().getPrincipalId()),
                        SandwishSubjectType.FRONT_MEMBER,
                        null,
                        accessToken,
                        Collections.singletonList(MEMBER_PERMISSION)));
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

    private MemberAuthQuery memberAuthQuery(String accessToken) {
        MemberAuthQuery query = new MemberAuthQuery();
        query.setAccessToken(accessToken);
        return query;
    }
}
