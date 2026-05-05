package com.github.thundax.common.oss.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.support.LocalFileObjectStorageClient;
import com.github.thundax.common.oss.support.S3ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SandwishOssProperties.class)
public class SandwishOssAutoConfiguration {

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
}
