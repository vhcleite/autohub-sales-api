package com.fiap.autohub.autohub_sales_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Pagamento Concluído (Publicado pela Charges API, Consumido pela Sales API).
 */
public record PaymentCompletedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "PaymentCompleted"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "charges-api"
        @JsonProperty("data") PcData data
) {
    public record PcData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("charge_id") String chargeId,
            @JsonProperty("paid_at") OffsetDateTime paidAt
    ) {
    }

    public UUID saleId() {
        return data != null ? data.saleId() : null;
    }

    public String chargeId() {
        return data != null ? data.chargeId() : null;
    }

    public OffsetDateTime paidAt() {
        return data != null ? data.paidAt() : null;
    }
}