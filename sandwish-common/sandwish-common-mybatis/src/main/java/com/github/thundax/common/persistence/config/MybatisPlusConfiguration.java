package com.github.thundax.common.persistence.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.mybatis.interceptor.AuditFieldInterceptor;
import com.github.thundax.common.mybatis.typehandler.EntityIdTypeHandler;
import com.github.thundax.common.mybatis.typehandler.StringListJsonTypeHandler;
import com.github.thundax.common.security.user.CurrentUserProvider;
import java.util.List;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.DM));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditFieldInterceptor auditFieldInterceptor(CurrentUserProvider currentUserProvider) {
        return new AuditFieldInterceptor(currentUserProvider);
    }

    @Bean
    @ConditionalOnMissingBean(name = "mybatisPlusTypeHandlerCustomizer")
    public ConfigurationCustomizer mybatisPlusTypeHandlerCustomizer() {
        return configuration -> registerDefaultTypeHandlers(configuration.getTypeHandlerRegistry());
    }

    private void registerDefaultTypeHandlers(TypeHandlerRegistry registry) {
        registry.register(EntityId.class, EntityIdTypeHandler.class);
        registry.register(List.class, StringListJsonTypeHandler.class);
    }
}
