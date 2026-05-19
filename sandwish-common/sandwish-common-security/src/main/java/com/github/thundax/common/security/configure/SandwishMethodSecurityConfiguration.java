package com.github.thundax.common.security.configure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@ConditionalOnMissingBean(MethodSecurityInterceptor.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SandwishMethodSecurityConfiguration {}
