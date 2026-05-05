package com.github.thundax.common.security.config;

import com.github.thundax.common.security.authorization.HasPermissionBeanPostProcessor;
import com.github.thundax.common.security.permission.PermissionAuthorizationService;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import com.github.thundax.common.security.user.CurrentUserProvider;
import com.github.thundax.common.security.user.CurrentUserResolver;
import com.github.thundax.common.security.user.SecurityContextCurrentUserResolver;
import com.github.thundax.common.security.user.SpringSecurityCurrentUserProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SandwishMethodSecurityConfiguration.class)
public class SandwishSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CurrentUserResolver currentUserResolver() {
        return new SecurityContextCurrentUserResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentUserProvider currentUserProvider(CurrentUserResolver currentUserResolver) {
        return new SpringSecurityCurrentUserProvider(currentUserResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionMatcher permissionMatcher() {
        return new PrefixPermissionMatcher();
    }

    @Bean("permissionAuthorizationService")
    @ConditionalOnMissingBean(value = PermissionAuthorizationService.class, name = "permissionAuthorizationService")
    public PermissionAuthorizationService permissionAuthorizationService(
            CurrentUserProvider currentUserProvider, PermissionMatcher permissionMatcher) {
        return new PermissionAuthorizationService(currentUserProvider, permissionMatcher);
    }

    @Bean
    @ConditionalOnBean(PermissionAuthorizationService.class)
    @ConditionalOnMissingBean
    public HasPermissionBeanPostProcessor hasPermissionBeanPostProcessor(
            PermissionAuthorizationService permissionAuthorizationService) {
        return new HasPermissionBeanPostProcessor(permissionAuthorizationService);
    }
}
