package com.fiap.autohub.autohub_sales_api.domain.entities;

/**
 * Enum representando os possíveis status de uma Venda.
 */
public enum SaleStatus {
    RESERVATION_FAILED,       // Falha ao reservar o veículo
    PAYMENT_FAILED,           // Pagamento falhou
    PAYMENT_EXPIRED,          // Cobrança expirou antes do pagamento
    PROCESSING_DOCUMENTATION, // Pagamento OK, processando documentação (ex: DETRAN)
    PROCESSING,               // Aguardando reserva do veículo e realização do pagamento
    COMPLETED,                // Venda concluída com sucesso
    FAILED,                   // Estado genérico de falha (pode agrupar falhas específicas)
}