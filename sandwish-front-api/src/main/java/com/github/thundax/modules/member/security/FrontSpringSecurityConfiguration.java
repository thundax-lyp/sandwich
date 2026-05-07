package com.github.thundax.modules.member.security;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.utils.RsaSessionUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class FrontSpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final MemberSpringAuthenticationProvider authenticationProvider;
    private final RsaSessionUtils rsaSessionUtils;

    public FrontSpringSecurityConfiguration(
            MemberSpringAuthenticationProvider authenticationProvider, RsaSessionUtils rsaSessionUtils) {
        this.authenticationProvider = authenticationProvider;
        this.rsaSessionUtils = rsaSessionUtils;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
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
                .antMatchers("/static/**", "/auth/register/**", "/auth/login")
                .permitAll()
                .antMatchers("/member/**")
                .authenticated()
                .anyRequest()
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> writeLogoutResponse(response))
                .and()
                .addFilterAt(memberSpringAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private MemberSpringAuthenticationFilter memberSpringAuthenticationFilter() throws Exception {
        MemberSpringAuthenticationFilter filter = new MemberSpringAuthenticationFilter(rsaSessionUtils);
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    private void writeLogoutResponse(HttpServletResponse response) throws IOException {
        String jsonString = JsonUtils.toJson(MemberLoginInterfaceAssembler.toLogoutResponse());
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }
}
