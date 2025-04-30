package com.fiap.autohub.autohub_sales_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AutohubSalesApiApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine") // Versão do Postgres
            .withDatabaseName("testdb_sales") // Nome do banco de dados no container
            .withUsername("testuser")
            .withPassword("testpass");

    @MockBean
    private SnsClient snsClient;

    @MockBean
    private SqsAsyncClient sqsAsyncClient;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver"); // Garante o driver PG
    }

    @Test
    void contextLoads() {
        System.out.println("Context loaded successfully using Testcontainers PostgreSQL!");
        System.out.println("DB URL: " + postgresContainer.getJdbcUrl());
    }

}
