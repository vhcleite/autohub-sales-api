package com.fiap.autohub.autohub_sales_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleReservationFailedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // Deve ser "VehicleReservationFailed"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source,
        @JsonProperty("data") VrfData data
) {
    public record VrfData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("reason") String reason
    ) {
    }

    public UUID saleId() {
        return data.saleId();
    }

    public UUID vehicleId() {
        return data.vehicleId();
    }

    public String reason() {
        return data.reason();
    }
}