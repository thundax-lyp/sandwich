package com.github.thundax;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.modules.auth.config.AuthProperties;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(
        basePackages = {"com.github.thundax.modules"},
        annotationClass = Mapper.class)
@EnableConfigurationProperties(value = {SandwishProperties.class, AuthProperties.class})
@EnableScheduling
public class FrontApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FrontApiApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        this.setRegisterErrorPageFilter(false);
        return builder.sources(FrontApiApplication.class);
    }
}
