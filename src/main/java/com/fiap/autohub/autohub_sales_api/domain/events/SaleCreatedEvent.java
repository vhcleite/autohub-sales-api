package com.fiap.autohub.autohub_sales_api.domain.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento publicado quando uma nova venda é iniciada.
 */
public record SaleCreatedEvent(
        UUID eventId,
        String eventType,
        OffsetDateTime timestamp,
        String source,
        SaleData data
) {
    // Construtor para facilitar a criação
    public SaleCreatedEvent(UUID saleId, UUID vehicleId, String buyerUserId, String sellerUserId, BigDecimal price) {
        this(
                UUID.randomUUID(),
                "SaleCreated",
                OffsetDateTime.now(),
                "sales-api",
                new SaleData(saleId, vehicleId, buyerUserId, sellerUserId, price)
        );
    }

    // Classe interna para os dados específicos
    public record SaleData(
            UUID saleId,
            UUID vehicleId,
            String buyerUserId,
            String sellerUserId,
            BigDecimal price
    ) {
    }
}