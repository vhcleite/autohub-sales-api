package com.fiap.autohub.autohub_sales_api.domain.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando para iniciar uma nova venda.
 * Contém os dados necessários vindos da camada de infraestrutura (ex: Controller).
 */
public record CreateSaleCommand(
        UUID vehicleId,
        String sellerUserId, // Incluído conforme decisão
        BigDecimal price     // Incluído conforme decisão
) {
}