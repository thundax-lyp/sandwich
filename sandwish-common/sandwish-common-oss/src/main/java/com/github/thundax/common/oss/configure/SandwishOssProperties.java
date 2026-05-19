package com.github.thundax.common.oss.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sandwish.oss")
public class SandwishOssProperties {

    private String type = "local";
    private Local local = new Local();
    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Local {

        private String rootPath = "storage";
        private String locationPrefix = "file:";
    }

    @Getter
    @Setter
    public static class S3 {

        private String endpoint;
        private String region = "us-east-1";
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String locationPrefix;
        private boolean pathStyleAccess = true;
    }
}
