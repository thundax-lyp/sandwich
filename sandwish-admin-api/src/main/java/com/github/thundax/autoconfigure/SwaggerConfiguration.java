package com.github.thundax.autoconfigure;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMethod;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author thundax
 */
@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
@EnableSwagger2
@Profile({"dameng-dev"})
public class SwaggerConfiguration {

    private static final String PACKAGE_SEPARATOR = ",";

    private final SwaggerProperties properties;

    public SwaggerConfiguration(SwaggerProperties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .termsOfServiceUrl(properties.getTermsOfServiceUrl())
                .contact(new Contact(properties.getContactName(), properties.getContactUrl(), properties.getContactEmail()))
                .version(properties.getVersion())
                .build();

        List<ResponseMessage> responseMessageList = new ArrayList<>();
        responseMessageList.add(new ResponseMessageBuilder().code(200).message("成功").build());

        ApiSelectorBuilder builder = new Docket(DocumentationType.SWAGGER_2)
                .globalResponseMessage(RequestMethod.GET, responseMessageList)
                .globalResponseMessage(RequestMethod.POST, responseMessageList)
                .apiInfo(apiInfo)
                .select();

        String basePackage = properties.getBasePackage();

        builder.apis(input -> {
            assert input != null;
            return Optional.ofNullable(input.declaringClass()).map(inputClass -> {
                for (String pkg : basePackage.split(PACKAGE_SEPARATOR)) {
                    boolean isMatch = inputClass.getPackage().getName().startsWith(pkg);
                    if (isMatch) {
                        return true;
                    }
                }
                return false;
            }).orElse(true);
        });

        builder.paths(PathSelectors.any());

        return builder.build();
    }

}
