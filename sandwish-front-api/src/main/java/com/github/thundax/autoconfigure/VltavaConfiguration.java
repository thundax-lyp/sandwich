package com.github.thundax.autoconfigure;

import com.github.thundax.common.jasypt.JasyptStringEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VltavaProperties.class)
public class VltavaConfiguration {

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        return new JasyptStringEncryptor();
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
