package com.github.thundax.autoconfigure;

import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import com.github.thundax.modules.assist.service.impl.DefaultSignServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 签名服务默认配置
 *
 * @author wdit
 */
@Configuration
public class DefaultSignatureConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SignService signService(SignatureService signatureService) {
        return new DefaultSignServiceImpl(signatureService);
    }
}
