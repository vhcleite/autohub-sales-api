package com.fiap.autohub.autohub_sales_api.infrastructure.messaging.consumers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_sales_api.domain.events.*;
import com.fiap.autohub.autohub_sales_api.domain.ports.in.SaleServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente responsável por processar os eventos SQS recebidos pela Sales API.
 * Contém a lógica de roteamento baseada no eventType.
 */
@Component
public class SaleEventListener {

    private static final Logger log = LoggerFactory.getLogger(SaleEventListener.class);

    private final SaleServicePort saleService;
    private final ObjectMapper objectMapper;

    public SaleEventListener(SaleServicePort saleService, ObjectMapper objectMapper) {
        this.saleService = saleService;
        this.objectMapper = objectMapper;
    }

    public void consumeEvent(SQSEvent sqsEvent) {
        try {
            log.info("sqsEvent {}", objectMapper.writeValueAsString(sqsEvent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (sqsEvent == null || sqsEvent.getRecords() == null) {
            log.warn("Received null or empty SQSEvent.");
            return;
        }
        log.info("Processing SQS event with {} record(s)", sqsEvent.getRecords().size());
        List<SQSEvent.SQSMessage> messages = sqsEvent.getRecords();

        for (SQSEvent.SQSMessage message : messages) {
            String messageId = message.getMessageId();
            String messageBody = message.getBody();
            log.info("Processing message ID: {}, Body: {}", messageId, messageBody);
            try {
                JsonNode rootNode = objectMapper.readTree(messageBody);
                String eventType = rootNode.path("event_type").asText(null);

                if (eventType == null) {
                    log.error("Received message (ID: {}) without 'event_type' field: {}", messageId, messageBody);
                    throw new IllegalArgumentException("Missing event_type in message body for message ID: " + messageId);
                }

                log.info("Routing event (Message ID: {}) based on eventType: {}", messageId, eventType);

                switch (eventType) {
                    case "VehicleReservationFailed":
                        VehicleReservationFailedEvent vrfe = objectMapper.readValue(messageBody, VehicleReservationFailedEvent.class);
                        saleService.handleVehicleReservationFailure(vrfe);
                        break;
                    case "ChargeCreationFailed":
                        ChargeCreationFailedEvent ccfe = objectMapper.readValue(messageBody, ChargeCreationFailedEvent.class);
                        saleService.handleChargeCreationFailure(ccfe);
                        break;
                    case "PaymentCompleted":
                        PaymentCompletedEvent pce = objectMapper.readValue(messageBody, PaymentCompletedEvent.class);
                        saleService.handlePaymentCompletion(pce);
                        break;
                    case "PaymentFailed":
                        PaymentFailedEvent pfe = objectMapper.readValue(messageBody, PaymentFailedEvent.class);
                        saleService.handlePaymentFailure(pfe);
                        break;
                    case "ChargeExpired":
                        ChargeExpiredEvent cee = objectMapper.readValue(messageBody, ChargeExpiredEvent.class);
                        saleService.handleChargeExpiration(cee);
                        break;
                    default:
                        log.warn("Received unhandled eventType '{}' for message ID: {}", eventType, messageId);
                        break;
                }
                log.debug("Finished processing message ID: {} for eventType: {}", messageId, eventType);

            } catch (JsonProcessingException e) {
                log.error("Failed to parse message body (Message ID: {}): {}", messageId, messageBody, e);
                throw new RuntimeException("Message parsing failed for message ID: " + messageId, e);
            } catch (Exception e) {
                log.error("Failed to process message (Message ID: {}): {}", messageId, messageBody, e);
                throw new RuntimeException("Message processing failed for message ID: " + messageId, e);
            }
        }
        log.info("Finished processing batch of {} message(s).", messages.size());
    }
}