package com.fiap.autohub.autohub_sales_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Cobrança Expirada (Publicado pela Lambda de Timeout, Consumido pela Sales API).
 */
public record ChargeExpiredEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "ChargeExpired"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "timeout-lambda"
        @JsonProperty("data") CeData data
) {
    public record CeData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("charge_id") String chargeId
    ) {
    }

    public UUID saleId() {
        return data != null ? data.saleId() : null;
    }

    public String chargeId() {
        return data != null ? data.chargeId() : null;
    }
}