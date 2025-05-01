package com.fiap.autohub.autohub_sales_api.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    @Profile("!local & !test")
    public DataSource dataSource(
            @Value("${aws.region}") String awsRegion,
            @Value("${spring.datasource.url}") String dbUrl,
            @Value("${spring.datasource.username}") String dbUsername,
            @Value("${aws.secretsmanager.db-password-secret-arn}") String dbPasswordSecretArn) {

        log.info("Configuring DataSource for URL: {}", dbUrl);
        String password = getSecretValue(awsRegion, dbPasswordSecretArn);

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    private String getSecretValue(String region, String secretArn) {
        log.info("Fetching DB password from Secrets Manager. AWS Region: {}, Secret ARN: {}", region, secretArn);

        try (SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretArn)
                    .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            log.info("Successfully fetched password from Secrets Manager.");
            return valueResponse.secretString();
        } catch (Exception e) {
            log.error("Failed to retrieve secret '{}' from Secrets Manager in region '{}'", secretArn, region, e);
            throw new RuntimeException("Failed to retrieve DB password from Secrets Manager", e);
        }
    }
}