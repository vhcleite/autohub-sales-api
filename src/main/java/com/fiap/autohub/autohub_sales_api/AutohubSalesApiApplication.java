package com.fiap.autohub.autohub_sales_api;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fiap.autohub.autohub_sales_api.infrastructure.messaging.consumers.SaleEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class AutohubSalesApiApplication {

    private static final Logger log = LoggerFactory.getLogger(AutohubSalesApiApplication.class); // Logger

    public static void main(String[] args) {
        SpringApplication.run(AutohubSalesApiApplication.class, args);
    }

    @Bean
    public Consumer<SQSEvent> saleCreatedConsumer(SaleEventListener consumerLogic) {
        log.info("Creating saleCreatedConsumer bean for SQS profile.");
        return consumerLogic::consumeEvent;
    }
}