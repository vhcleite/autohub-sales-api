package com.fiap.autohub.autohub_sales_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Falha no Pagamento (Publicado pela Charges API, Consumido pela Sales API).
 * Segue o padrão de Envelope. (Exemplo)
 */
public record PaymentFailedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "PaymentFailed"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "charges-api"
        @JsonProperty("data") PfData data
) {
    public record PfData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("charge_id") String chargeId,
            @JsonProperty("reason") String reason
    ) {
    }

    public UUID saleId() {
        return data != null ? data.saleId() : null;
    }

    public String chargeId() {
        return data != null ? data.chargeId() : null;
    }

    public String reason() {
        return data != null ? data.reason() : null;
    }
}