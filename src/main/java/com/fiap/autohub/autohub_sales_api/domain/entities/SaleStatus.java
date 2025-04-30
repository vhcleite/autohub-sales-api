package com.fiap.autohub.autohub_sales_api.domain.entities;

/**
 * Enum representando os possíveis status de uma Venda.
 */
public enum SaleStatus {
    PENDING_RESERVATION,    // Aguardando confirmação da reserva do veículo
    RESERVATION_FAILED,     // Falha ao reservar o veículo
    PENDING_PAYMENT,        // Aguardando criação/pagamento da cobrança
    PAYMENT_FAILED,         // Pagamento falhou
    PAYMENT_EXPIRED,        // Cobrança expirou antes do pagamento
    PROCESSING_DOCUMENTATION, // Pagamento OK, processando documentação (ex: DETRAN)
    DOCUMENTATION_FAILED,   // Falha no processamento da documentação
    COMPLETED,              // Venda concluída com sucesso
    FAILED,                 // Estado genérico de falha (pode agrupar falhas específicas)
    CANCELLED               // Venda cancelada (ex: pelo usuário, se permitido)
}