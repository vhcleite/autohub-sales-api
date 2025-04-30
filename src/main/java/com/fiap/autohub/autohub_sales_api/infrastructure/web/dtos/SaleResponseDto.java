package com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos;

import com.fiap.autohub.autohub_sales_api.domain.entities.SaleStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para a resposta das operações de venda.
 */
public record SaleResponseDto(
        UUID id,
        UUID vehicleId,
        String buyerUserId,
        String sellerUserId,
        BigDecimal price,
        SaleStatus status,
        String failureReason,
        String chargeId,
        String detranProcessId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}