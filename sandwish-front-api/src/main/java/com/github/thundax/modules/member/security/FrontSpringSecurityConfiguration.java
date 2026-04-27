package com.github.thundax.modules.member.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/** 前台 Spring Security 迁移期配置。 */
@Configuration
@Profile("front-spring-security")
public class FrontSpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
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
                .logoutUrl("/auth/logout");
    }
}
