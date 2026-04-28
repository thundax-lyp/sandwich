package com.github.thundax.modules.member.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class FrontSpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final MemberSpringAuthenticationProvider authenticationProvider;

    public FrontSpringSecurityConfiguration(MemberSpringAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
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
                .antMatchers("/static/**", "/servlet/**", "/auth/register", "/auth/login")
                .permitAll()
                .antMatchers("/member/**")
                .authenticated()
                .anyRequest()
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/auth/logout")
                .and()
                .addFilterAt(memberSpringAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private MemberSpringAuthenticationFilter memberSpringAuthenticationFilter() throws Exception {
        MemberSpringAuthenticationFilter filter = new MemberSpringAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }
}
