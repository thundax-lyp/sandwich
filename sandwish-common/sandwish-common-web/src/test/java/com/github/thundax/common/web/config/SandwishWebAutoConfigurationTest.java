package com.github.thundax.common.web.config;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.context.DefaultSandwishContextResolver;
import com.github.thundax.common.web.context.SandwishContextFilter;
import com.github.thundax.common.web.context.SandwishContextResolver;
import com.github.thundax.common.web.exception.GlobalExceptionHandler;
import com.github.thundax.common.web.i18n.I18nMessageResolver;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

public class SandwishWebAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SandwishWebAutoConfiguration.class));

    @Test
    public void shouldRegisterCommonWebBeans() {
        contextRunner.run(context -> {
            context.getBean(I18nMessageResolver.class);
            context.getBean(GlobalExceptionHandler.class);
            context.getBean(ApiResponseBodyAdvice.class);
            context.getBean(SandwishContextResolver.class);
            context.getBean(SandwishContextFilter.class);
        });
    }

    @Test
    public void shouldRegisterContextFilterWithHighestPrecedence() {
        contextRunner.run(context -> {
            FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);

            assertEquals(Ordered.HIGHEST_PRECEDENCE, registration.getOrder());
        });
    }

    @Test
    public void shouldBackOffWhenCustomContextResolverExists() {
        contextRunner
                .withUserConfiguration(CustomContextResolverConfiguration.class)
                .run(context -> {
                    assertEquals(
                            1,
                            context.getBeansOfType(SandwishContextResolver.class)
                                    .size());
                });
    }

    @Configuration
    static class CustomContextResolverConfiguration {

        @Bean
        public SandwishContextResolver customSandwishContextResolver() {
            return new DefaultSandwishContextResolver();
        }
    }
}
