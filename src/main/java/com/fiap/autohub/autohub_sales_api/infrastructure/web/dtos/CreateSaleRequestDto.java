package com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para a requisição de criação de venda (POST /sales).
 */
public record CreateSaleRequestDto(
        @NotNull(message = "Vehicle ID cannot be null")
        UUID vehicleId,

        @NotNull(message = "Seller User ID cannot be null")
        @Schema(description = "Id do vendedos", example = "443884e8-a051-70d8-f822-d142d11b109f", requiredMode = Schema.RequiredMode.REQUIRED)
        String sellerUserId, // ID do vendedor (obtido do veículo pelo frontend/caller)

        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price must be positive")
        @Schema(description = "Preço do veículo", example = "55000.90", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price // Preço acordado
) {
}