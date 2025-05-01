package com.fiap.autohub.autohub_sales_api.infrastructure.messaging.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_sales_api.domain.events.SaleCreatedEvent;
import com.fiap.autohub.autohub_sales_api.domain.ports.out.SaleEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter que implementa a porta SaleEventPublisherPort usando AWS SNS.
 */
@Component
public class SnsSaleEventPublisherAdapter implements SaleEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SnsSaleEventPublisherAdapter.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SnsSaleEventPublisherAdapter(SnsClient snsClient,
                                        ObjectMapper objectMapper,
                                        @Value("${sns.topic.main-event-bus-arn}") String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
        log.info("SNSEventPublisher configured for topic ARN: {}", topicArn);
    }

    @Override
    public void publishSaleCreated(SaleCreatedEvent event) {
        publishEvent(event, event.eventType());
    }

    private void publishEvent(Object eventPayload, String eventType) {
        try {
            String messageBody = objectMapper.writeValueAsString(eventPayload);
            log.info("Publishing event type '{}' to SNS topic {}: {}", eventType, topicArn, messageBody);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(eventType)
                    .build());

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(messageBody)
                    .messageAttributes(messageAttributes)
                    .build();

            snsClient.publish(publishRequest);
            log.info("Event type '{}' published successfully to topic {}.", eventType, topicArn);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for type {}: {}", eventType, eventPayload, e);
        } catch (SnsException e) {
            log.error("Failed to publish event type '{}' to SNS topic {}: {}", eventType, topicArn, e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error publishing event type '{}' to SNS topic {}: {}", eventType, topicArn, e.getMessage(), e);
        }
    }
}