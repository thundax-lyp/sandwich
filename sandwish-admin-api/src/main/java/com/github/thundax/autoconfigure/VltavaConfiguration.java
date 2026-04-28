package com.github.thundax.autoconfigure;

import com.github.thundax.common.jasypt.JasyptStringEncryptor;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.service.impl.Sm3PasswordServiceImpl;
import com.github.thundax.modules.sys.aop.SysLogPointcutAdvisor;
import com.github.thundax.modules.utils.AvatarUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties({VltavaProperties.class, AuthProperties.class})
public class VltavaConfiguration {

    @Autowired
    public VltavaConfiguration(VltavaProperties properties) {
        AvatarUtils.setStoragePath(properties.getUpload().getStoragePath());
    }

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        return new JasyptStringEncryptor();
    }

    @Bean
    public PasswordService passwordService() {
        return new Sm3PasswordServiceImpl();
    }

    @Bean
    public SysLogPointcutAdvisor sysLogPointcutAdvisor() {
        return new SysLogPointcutAdvisor();
    }

    /**
     * 低版本Springboot需要自己配置
     *
     * @return bean
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(200);
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(100);
        return taskExecutor;
    }

    public static void main(String[] argc) {
        String[] plainStrings = new String[] {
            "SYSTEM",
            "Kbs2020v8.com.cn",
            "uos2020@#test",
            "cms",
            "wdit2020@#rbmq",
            "666666",
            "shmhq",
            "b2f6753ce85d40d5b77954e350e40ec3",
            "031d74741d9c23c639f78bbeb81a5442"
        };
        StringEncryptor encryptor = new JasyptStringEncryptor();

        for (String plainString : plainStrings) {
            System.out.println(plainString + " : ENC(" + encryptor.encrypt(plainString) + ")");
        }
    }
}
