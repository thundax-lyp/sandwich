package com.github.thundax.common.security.config;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.authorization.HasPermissionBeanPostProcessor;
import com.github.thundax.common.security.permission.PermissionAuthorizationService;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.user.CurrentUserProvider;
import com.github.thundax.common.security.user.CurrentUserResolver;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SandwishSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SandwishSecurityAutoConfiguration.class));

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldRegisterCommonSecurityBeans() {
        contextRunner.run(context -> {
            context.getBean(CurrentUserResolver.class);
            context.getBean(CurrentUserProvider.class);
            context.getBean(PermissionMatcher.class);
            context.getBean(PermissionAuthorizationService.class);
            context.getBean(HasPermissionBeanPostProcessor.class);
        });
    }

    @Test
    public void shouldBackOffWhenCustomCurrentUserProviderExists() {
        contextRunner
                .withUserConfiguration(CustomCurrentUserProviderConfiguration.class)
                .run(context -> {
                    assertEquals(
                            1, context.getBeansOfType(CurrentUserProvider.class).size());
                });
    }

    @Test
    public void shouldAllowHasPermissionMethodWhenCurrentUserHasPermission() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("admin", "credential", "sys:user:view"));

        contextRunner
                .withUserConfiguration(HasPermissionServiceConfiguration.class)
                .run(context -> {
                    assertEquals(
                            "ok",
                            context.getBean(SampleHasPermissionService.class).view());
                });
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldDenyHasPermissionMethodWhenCurrentUserMissesPermission() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("admin", "credential", "sys:role:view"));

        contextRunner
                .withUserConfiguration(HasPermissionServiceConfiguration.class)
                .run(context -> {
                    context.getBean(SampleHasPermissionService.class).view();
                });
    }

    @Configuration
    static class CustomCurrentUserProviderConfiguration {

        @Bean
        public CurrentUserProvider customCurrentUserProvider() {
            return () -> null;
        }
    }

    @Configuration
    static class HasPermissionServiceConfiguration {

        @Bean
        public SampleHasPermissionService sampleHasPermissionService() {
            return new SampleHasPermissionService();
        }
    }

    static class SampleHasPermissionService {

        @HasPermission("sys:user:view")
        public String view() {
            return "ok";
        }
    }
}
