package com.github.thundax.modules.auth.security.filter;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.PermissionSession;
import com.github.thundax.modules.auth.filter.ResponseBodyWrapper;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;


public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_TOKEN = Constants.HEADER_TOKEN;
    private static final String PARAM_TOKEN = Constants.PARAM_TOKEN;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePatternList = new ArrayList<>();

    private final AuthService authService;
    private final PermissionService permissionService;

    public AccessTokenAuthenticationFilter(
            VltavaProperties.AccessTokenFilterProperties properties,
            AuthService authService,
            PermissionService permissionService) {
        if (properties.getExcludePath() != null) {
            this.excludePatternList.addAll(properties.getExcludePath());
        }
        this.authService = authService;
        this.permissionService = permissionService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri =
                request.getRequestURI().substring(request.getContextPath().length());

        for (String excludePattern : excludePatternList) {
            if (pathMatcher.match(excludePattern, requestUri)) {
                return true;
            }
        }

        return !pathMatcher.match("/api/**", requestUri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = findToken(request);
        if (StringUtils.isBlank(token)) {
            writeError(response);
            return;
        }

        AccessToken accessToken = authService.getAccessToken(token);
        if (accessToken == null) {
            writeError(response);
            return;
        }

        UserAccessHolder.currentUserId(accessToken.getUserId(), token);

        User currentUser = UserAccessHolder.currentUser();
        if (currentUser.getId() == null || !currentUser.isEnable()) {
            writeError(response);
            return;
        }

        PermissionSession session = permissionService.getSession(token);
        if (session == null) {
            session = permissionService.createSession(token, accessToken.getUserId());
        }

        authService.activeAccessToken(accessToken);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        accessToken.getUserId(), token, toAuthorities(session.getPermissions())));

        filterChain.doFilter(request, response);
    }

    private String findToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        return request.getParameter(PARAM_TOKEN);
    }

    private Collection<SimpleGrantedAuthority> toAuthorities(Collection<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (permissions == null || permissions.isEmpty()) {
            return authorities;
        }

        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }

    private void writeError(HttpServletResponse response) throws IOException {
        String jsonString = JsonUtils.toJson(new ResponseBodyWrapper(HttpStatus.UNAUTHORIZED.value(), "未授权用户"));

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }
}
