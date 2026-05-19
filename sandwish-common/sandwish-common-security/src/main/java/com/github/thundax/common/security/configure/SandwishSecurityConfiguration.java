package com.github.thundax.common.security.configure;

import com.github.thundax.common.security.authorization.HasPermissionBeanPostProcessor;
import com.github.thundax.common.security.permission.PermissionAuthorizationService;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SandwishMethodSecurityConfiguration.class)
public class SandwishSecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionMatcher permissionMatcher() {
        return new PrefixPermissionMatcher();
    }

    @Bean("permissionAuthorizationService")
    @ConditionalOnMissingBean(value = PermissionAuthorizationService.class, name = "permissionAuthorizationService")
    public PermissionAuthorizationService permissionAuthorizationService(PermissionMatcher permissionMatcher) {
        return new PermissionAuthorizationService(permissionMatcher);
    }

    @Bean
    @ConditionalOnBean(PermissionAuthorizationService.class)
    @ConditionalOnMissingBean
    public HasPermissionBeanPostProcessor hasPermissionBeanPostProcessor(
            PermissionAuthorizationService permissionAuthorizationService) {
        return new HasPermissionBeanPostProcessor(permissionAuthorizationService);
    }
}
