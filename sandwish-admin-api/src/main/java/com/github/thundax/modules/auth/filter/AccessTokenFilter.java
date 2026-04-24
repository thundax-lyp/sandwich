package com.github.thundax.modules.auth.filter;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wdit
 */
public class AccessTokenFilter extends OncePerRequestFilter {

    private static final String HEADER_TOKEN = Constants.HEADER_TOKEN;
    private static final String PARAM_TOKEN = Constants.PARAM_TOKEN;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePatternList = new ArrayList<>();

    private final AuthService authService;

    public AccessTokenFilter(VltavaProperties.AccessTokenFilterProperties properties,
                             AuthService authService) {
        this.excludePatternList.addAll(properties.getExcludePath());
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestUri = request.getRequestURI().substring(request.getContextPath().length());

        for (String excludePattern : excludePatternList) {
            if (pathMatcher.match(excludePattern, requestUri)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
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

        //校验用户有效性
        User currentUser = UserAccessHolder.currentUser();
        if (currentUser.getId() == null || !currentUser.isEnable()) {
            writeError(response);
            return;
        }

        authService.activeAccessToken(accessToken);

        filterChain.doFilter(request, response);
    }

    private String findToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        return request.getParameter(PARAM_TOKEN);
    }

    protected void writeError(HttpServletResponse response) throws IOException {
        String jsonString = JsonUtils.toJson(new ResponseBodyWrapper(HttpStatus.UNAUTHORIZED.value(), "未授权用户"));

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }

}
