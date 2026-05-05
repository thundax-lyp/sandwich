package com.github.thundax.common.swagger.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnClass(Docket.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@EnableSwagger2
public class SwaggerAutoConfiguration implements WebMvcConfigurer {

    private static final String PACKAGE_SEPARATOR = ",";

    private final SwaggerProperties properties;

    public SwaggerAutoConfiguration(SwaggerProperties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("deprecation")
    @Bean
    @ConditionalOnMissingBean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .termsOfServiceUrl(properties.getTermsOfServiceUrl())
                .contact(new Contact(
                        properties.getContactName(), properties.getContactUrl(), properties.getContactEmail()))
                .license(properties.getLicense())
                .licenseUrl(properties.getLicenseUrl())
                .version(properties.getVersion())
                .build();

        List<ResponseMessage> responseMessageList = new ArrayList<>();
        responseMessageList.add(
                new ResponseMessageBuilder().code(200).message("成功").build());

        ApiSelectorBuilder builder = new Docket(DocumentationType.SWAGGER_2)
                .enable(properties.isEnabled())
                .globalResponseMessage(RequestMethod.GET, responseMessageList)
                .globalResponseMessage(RequestMethod.POST, responseMessageList)
                .apiInfo(apiInfo)
                .select();

        String basePackage = properties.getBasePackage();

        builder.apis(input -> Optional.ofNullable(input)
                .flatMap(requestHandler -> Optional.ofNullable(requestHandler.declaringClass()))
                .map(inputClass -> {
                    for (String pkg : basePackage.split(PACKAGE_SEPARATOR)) {
                        String packageName = pkg.trim();
                        if (!packageName.isEmpty()
                                && inputClass.getPackage().getName().startsWith(packageName)) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(true));

        builder.paths(PathSelectors.any());

        return builder.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
