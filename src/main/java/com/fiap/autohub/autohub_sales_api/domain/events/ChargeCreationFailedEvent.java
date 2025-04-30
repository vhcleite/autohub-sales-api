package com.fiap.autohub.autohub_sales_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Falha na Criação da Cobrança (Publicado pela Charges API, Consumido pela Sales API).
 */
public record ChargeCreationFailedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "ChargeCreationFailed"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "charges-api"
        @JsonProperty("data") CcfData data
) {
    public record CcfData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("reason") String reason
    ) {
    }

    public UUID saleId() {
        return data != null ? data.saleId() : null;
    }

    public String reason() {
        return data != null ? data.reason() : null;
    }
}
