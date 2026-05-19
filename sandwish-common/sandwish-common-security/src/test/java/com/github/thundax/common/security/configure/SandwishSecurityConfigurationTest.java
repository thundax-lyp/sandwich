package com.github.thundax.common.security.configure;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.authorization.HasPermissionBeanPostProcessor;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.common.security.permission.PermissionAuthorizationService;
import com.github.thundax.common.security.permission.PermissionMatcher;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SandwishSecurityConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SandwishSecurityConfiguration.class));

    @After
    public void tearDown() {
        SandwishContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldRegisterCommonSecurityBeans() {
        contextRunner.run(context -> {
            context.getBean(PermissionMatcher.class);
            context.getBean(PermissionAuthorizationService.class);
            context.getBean(HasPermissionBeanPostProcessor.class);
        });
    }

    @Test
    public void shouldAllowHasPermissionMethodWhenCurrentUserHasPermission() {
        SandwishContextHolder.setSubject(new SandwishSubject(
                "admin", SandwishSubjectType.ADMIN_USER, "Admin", "token-1", Arrays.asList("sys:user:view")));

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
        SandwishContextHolder.setSubject(new SandwishSubject(
                "admin", SandwishSubjectType.ADMIN_USER, "Admin", "token-1", Arrays.asList("sys:role:view")));

        contextRunner
                .withUserConfiguration(HasPermissionServiceConfiguration.class)
                .run(context -> {
                    context.getBean(SampleHasPermissionService.class).view();
                });
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
