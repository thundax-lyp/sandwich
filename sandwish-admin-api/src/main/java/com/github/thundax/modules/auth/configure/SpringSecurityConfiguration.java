package com.github.thundax.modules.auth.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.configure.SandwishProperties;
import com.github.thundax.modules.auth.security.filter.AccessTokenAuthenticationFilter;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.sys.service.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final SandwishProperties properties;
    private final AdminAuthService authService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;

    public SpringSecurityConfiguration(
            SandwishProperties properties,
            AdminAuthService authService,
            PermissionService permissionService,
            UserService userService,
            RequestMappingHandlerMapping requestMappingHandlerMapping,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.authService = authService;
        this.permissionService = permissionService;
        this.userService = userService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(WebSecurity web) {
        List<String> excludePaths = accessTokenExcludePaths();
        if (!excludePaths.isEmpty()) {
            web.ignoring().antMatchers(excludePaths.toArray(new String[0]));
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()
                .logout()
                .disable()
                .authorizeRequests()
                .antMatchers("/api/**")
                .authenticated()
                .anyRequest()
                .permitAll()
                .and()
                .addFilterBefore(
                        new AccessTokenAuthenticationFilter(
                                accessTokenExcludePaths(), authService, permissionService, userService, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);
    }

    private List<String> accessTokenExcludePaths() {
        List<String> excludePaths =
                new ArrayList<>(properties.getAccessTokenFilter().getExcludePath());
        excludePaths.addAll(publicApiPaths());
        return excludePaths;
    }

    private List<String> publicApiPaths() {
        if (requestMappingHandlerMapping == null) {
            return Collections.emptyList();
        }

        List<String> paths = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry :
                requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!isPublicApi(handlerMethod)) {
                continue;
            }

            paths.addAll(entry.getKey().getPatternsCondition().getPatterns());
        }
        return paths;
    }

    private boolean isPublicApi(HandlerMethod handlerMethod) {
        return AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), PublicApi.class) != null
                || AnnotationUtils.findAnnotation(handlerMethod.getMethod(), PublicApi.class) != null;
    }
}
