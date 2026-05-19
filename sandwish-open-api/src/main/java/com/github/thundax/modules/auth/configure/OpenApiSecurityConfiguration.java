package com.github.thundax.modules.auth.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.modules.auth.security.OpenApiIpWhitelistMatcher;
import com.github.thundax.modules.auth.security.OpenApiNonceStore;
import com.github.thundax.modules.auth.security.OpenApiPermissionChecker;
import com.github.thundax.modules.auth.security.OpenApiSignatureVerifier;
import com.github.thundax.modules.auth.security.filter.OpenApiAuthenticationFilter;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.open.service.OpenClientService;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class OpenApiSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;
    private final OpenClientService openClientService;
    private final OpenApiNonceStore nonceStore;
    private final OpenApiIpWhitelistMatcher ipWhitelistMatcher;
    private final OpenApiPermissionChecker permissionChecker;
    private final StringEncryptor stringEncryptor;
    private final ObjectMapper objectMapper;

    public OpenApiSecurityConfiguration(
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            OpenClientService openClientService,
            OpenApiNonceStore nonceStore,
            OpenApiIpWhitelistMatcher ipWhitelistMatcher,
            OpenApiPermissionChecker permissionChecker,
            StringEncryptor stringEncryptor,
            ObjectMapper objectMapper) {
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.openClientService = openClientService;
        this.nonceStore = nonceStore;
        this.ipWhitelistMatcher = ipWhitelistMatcher;
        this.permissionChecker = permissionChecker;
        this.stringEncryptor = stringEncryptor;
        this.objectMapper = objectMapper;
    }

    @Bean
    public OpenApiSignatureVerifier openApiSignatureVerifier() {
        return new OpenApiSignatureVerifier();
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
                .addFilterBefore(openApiAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private OpenApiAuthenticationFilter openApiAuthenticationFilter() {
        return new OpenApiAuthenticationFilter(
                principalIdentityService,
                principalCredentialService,
                openClientService,
                openApiSignatureVerifier(),
                nonceStore,
                ipWhitelistMatcher,
                permissionChecker,
                stringEncryptor,
                objectMapper);
    }
}
