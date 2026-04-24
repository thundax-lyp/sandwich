package com.github.thundax.autoconfigure;


import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import com.github.thundax.modules.assist.service.impl.DefaultSignServiceImpl;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.service.UserEncryptService;
import com.github.thundax.modules.sys.service.impl.DefaultUserEncryptServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 加密服务默认配置
 *
 * @author wdit
 */
@Configuration
public class DefaultEncryptConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserEncryptService userEncryptService(UserEncryptDao dao, RedisClient redisClient) {
        return new DefaultUserEncryptServiceImpl(dao, redisClient);
    }

}
