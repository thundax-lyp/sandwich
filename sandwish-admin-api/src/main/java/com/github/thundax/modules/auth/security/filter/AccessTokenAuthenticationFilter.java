package com.github.thundax.modules.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.common.web.exception.WebErrorCode;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.service.command.AdminAuthCommand;
import com.github.thundax.modules.auth.service.query.AdminAuthQuery;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_TOKEN = Constants.HEADER_TOKEN;
    private static final String PARAM_TOKEN = Constants.PARAM_TOKEN;
    private static final String APPLICATION_JSON_UTF8_VALUE = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePatternList = new ArrayList<>();

    private final AdminAuthService authService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AccessTokenAuthenticationFilter(
            SandwishProperties.AccessTokenFilterProperties properties,
            AdminAuthService authService,
            PermissionService permissionService,
            UserService userService,
            ObjectMapper objectMapper) {
        this(properties.getExcludePath(), authService, permissionService, userService, objectMapper);
    }

    public AccessTokenAuthenticationFilter(
            List<String> excludePaths,
            AdminAuthService authService,
            PermissionService permissionService,
            UserService userService,
            ObjectMapper objectMapper) {
        this.excludePatternList.addAll(excludePaths);
        this.authService = authService;
        this.permissionService = permissionService;
        this.userService = userService;
        this.objectMapper = objectMapper;
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

        AuthAccessTokenResult accessToken = authService.getAccessToken(tokenQuery(token));
        if (accessToken == null) {
            writeError(response);
            return;
        }

        if (StringUtils.isBlank(accessToken.getUserId())) {
            writeError(response);
            return;
        }
        User currentUser = userService.get(UserIdCodec.toDomain(Long.valueOf(accessToken.getUserId())));
        if (currentUser.getId() == null || !currentUser.isEnable()) {
            writeError(response);
            return;
        }

        Set<String> permissions = permissionService.getPermissions(token);
        if (permissions == null) {
            permissions = permissionService.createPermissions(token, accessToken.getUserId());
        }

        authService.activeAccessToken(accessTokenCommand(accessToken));
        SandwishContextHolder.setSubject(new SandwishSubject(
                accessToken.getUserId(), SandwishSubjectType.ADMIN_USER, currentUser.getName(), token, permissions));

        filterChain.doFilter(request, response);
    }

    private String findToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        return request.getParameter(PARAM_TOKEN);
    }

    private AdminAuthQuery tokenQuery(String token) {
        AdminAuthQuery query = new AdminAuthQuery();
        query.setToken(token);
        return query;
    }

    private AdminAuthCommand accessTokenCommand(AuthAccessTokenResult accessToken) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setAccessToken(accessToken);
        return command;
    }

    private void writeError(HttpServletResponse response) throws IOException {
        String jsonString =
                objectMapper.writeValueAsString(new ResponseBodyWrapper(WebErrorCode.UNAUTHORIZED.getCode(), "未授权用户"));

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(APPLICATION_JSON_UTF8_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }
}
