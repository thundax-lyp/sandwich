package com.github.thundax.common.web.config;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.context.DefaultSandwishContextResolver;
import com.github.thundax.common.web.context.SandwishContextFilter;
import com.github.thundax.common.web.context.SandwishContextResolver;
import com.github.thundax.common.web.exception.DefaultExceptionTranslator;
import com.github.thundax.common.web.exception.ExceptionTranslator;
import com.github.thundax.common.web.exception.GlobalExceptionHandler;
import com.github.thundax.common.web.i18n.I18nMessageResolver;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SandwishWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public I18nMessageResolver i18nMessageResolver(MessageSource messageSource) {
        return new I18nMessageResolver(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(
            I18nMessageResolver i18nMessageResolver, List<ExceptionTranslator> exceptionTranslators) {
        return new GlobalExceptionHandler(i18nMessageResolver, exceptionTranslators);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultExceptionTranslator.class)
    public DefaultExceptionTranslator defaultExceptionTranslator() {
        return new DefaultExceptionTranslator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseBodyAdvice apiResponseBodyAdvice() {
        return new ApiResponseBodyAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public SandwishContextResolver sandwishContextResolver() {
        return new DefaultSandwishContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public SandwishContextFilter sandwishContextFilter(SandwishContextResolver sandwishContextResolver) {
        return new SandwishContextFilter(sandwishContextResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sandwishContextFilterRegistration")
    public FilterRegistrationBean<SandwishContextFilter> sandwishContextFilterRegistration(
            SandwishContextFilter sandwishContextFilter) {
        FilterRegistrationBean<SandwishContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(sandwishContextFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
