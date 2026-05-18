package com.github.thundax.common.oss.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.support.LocalFileObjectStorageClient;
import com.github.thundax.common.oss.support.S3ObjectStorageClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(SandwishOssProperties.class)
public class SandwishOssAutoConfiguration {

    @Bean
    public SandwishOssConfigurationValidator sandwishOssConfigurationValidator(SandwishOssProperties properties) {
        return new SandwishOssConfigurationValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sandwish.oss", name = "type", havingValue = "local", matchIfMissing = true)
    public ObjectStorageClient localFileObjectStorageClient(SandwishOssProperties properties) {
        return new LocalFileObjectStorageClient(
                properties.getLocal().getRootPath(), properties.getLocal().getLocationPrefix());
    }

    @Bean
    @ConditionalOnMissingBean(AmazonS3.class)
    @ConditionalOnProperty(prefix = "sandwish.oss", name = "type", havingValue = "s3")
    public AmazonS3 amazonS3(SandwishOssProperties properties) {
        SandwishOssProperties.S3 s3 = properties.getS3();
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(s3.isPathStyleAccess())
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(s3.getAccessKey(), s3.getSecretKey())));
        if (s3.getEndpoint() != null && s3.getEndpoint().length() > 0) {
            builder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(s3.getEndpoint(), s3.getRegion()));
        } else {
            builder.withRegion(s3.getRegion());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectStorageClient.class)
    @ConditionalOnProperty(prefix = "sandwish.oss", name = "type", havingValue = "s3")
    public ObjectStorageClient s3ObjectStorageClient(AmazonS3 amazonS3, SandwishOssProperties properties) {
        return new S3ObjectStorageClient(
                amazonS3, properties.getS3().getBucket(), properties.getS3().getLocationPrefix());
    }

    public static class SandwishOssConfigurationValidator implements InitializingBean {

        private final SandwishOssProperties properties;

        public SandwishOssConfigurationValidator(SandwishOssProperties properties) {
            this.properties = properties;
        }

        @Override
        public void afterPropertiesSet() {
            String type = properties.getType();
            if (!StringUtils.hasText(type)) {
                throw new IllegalStateException("Missing OSS configuration. Configure sandwish.oss.type.");
            }
            if ("local".equalsIgnoreCase(type)) {
                validateLocal();
                return;
            }
            if ("s3".equalsIgnoreCase(type)) {
                validateS3();
                return;
            }
            throw new IllegalStateException(
                    "Unsupported OSS type: " + type + ". Sandwich currently supports local and s3.");
        }

        private void validateLocal() {
            SandwishOssProperties.Local local = properties.getLocal();
            if (local == null || !StringUtils.hasText(local.getRootPath())) {
                throw new IllegalStateException(
                        "Missing local OSS configuration. Configure sandwish.oss.local.root-path.");
            }
            if (!StringUtils.hasText(local.getLocationPrefix())) {
                throw new IllegalStateException(
                        "Missing local OSS configuration. Configure sandwish.oss.local.location-prefix.");
            }
        }

        private void validateS3() {
            SandwishOssProperties.S3 s3 = properties.getS3();
            if (s3 == null) {
                throw new IllegalStateException("Missing S3 OSS configuration. Configure sandwish.oss.s3.");
            }
            requireText(s3.getRegion(), "sandwish.oss.s3.region");
            requireText(s3.getBucket(), "sandwish.oss.s3.bucket");
            requireText(s3.getAccessKey(), "sandwish.oss.s3.access-key");
            requireText(s3.getSecretKey(), "sandwish.oss.s3.secret-key");
            requireText(s3.getLocationPrefix(), "sandwish.oss.s3.location-prefix");
        }

        private void requireText(String value, String propertyName) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException("Missing S3 OSS configuration. Configure " + propertyName + ".");
            }
        }
    }
}
