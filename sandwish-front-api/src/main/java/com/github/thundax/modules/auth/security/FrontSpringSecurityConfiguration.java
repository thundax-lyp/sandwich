package com.github.thundax.modules.auth.security;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.auth.service.MemberAuthService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class FrontSpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final MemberAuthService memberAuthService;

    public FrontSpringSecurityConfiguration(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()
                .authorizeRequests()
                .antMatchers(
                        "/static/**",
                        "/api/auth/register/**",
                        "/api/auth/session/pre-auth-session",
                        "/api/auth/session/pre-auth-session/refresh",
                        "/api/auth/session/login/**")
                .permitAll()
                .antMatchers("/member/**")
                .authenticated()
                .anyRequest()
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/api/auth/session/logout")
                .logoutSuccessHandler((request, response, authentication) -> writeLogoutResponse(response))
                .and()
                .addFilterBefore(
                        new MemberAccessTokenAuthenticationFilter(memberAuthService),
                        UsernamePasswordAuthenticationFilter.class);
    }

    private void writeLogoutResponse(HttpServletResponse response) throws IOException {
        String jsonString = JsonUtils.toJson(MemberLoginInterfaceAssembler.toLogoutResponse());
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }
}
