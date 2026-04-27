package com.github.thundax.modules.auth.security;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.modules.auth.security.filter.AccessTokenAuthenticationFilter;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** 后台 Spring Security 配置。 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final VltavaProperties properties;
    private final AuthService authService;
    private final PermissionService permissionService;

    public SpringSecurityConfiguration(
            VltavaProperties properties,
            AuthService authService,
            PermissionService permissionService) {
        this.properties = properties;
        this.authService = authService;
        this.permissionService = permissionService;
    }

    @Override
    public void configure(WebSecurity web) {
        List<String> excludePaths = properties.getAccessTokenFilter().getExcludePath();
        if (excludePaths != null && !excludePaths.isEmpty()) {
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
                                properties.getAccessTokenFilter(), authService, permissionService),
                        UsernamePasswordAuthenticationFilter.class);
    }
}
