package com.fiap.autohub.autohub_sales_api.domain.ports.in;

import com.fiap.autohub.autohub_sales_api.domain.commands.CreateSaleCommand;
import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;
import com.fiap.autohub.autohub_sales_api.domain.events.*;

import java.util.UUID;

/**
 * Porta de entrada para as operações de negócio relacionadas a Vendas.
 * Define os casos de uso que a camada de aplicação deve implementar.
 */
public interface SaleServicePort {

    /**
     * Inicia o processo de uma nova venda.
     *
     * @param command     Dados para criar a venda.
     * @param buyerUserId ID do usuário comprador (obtido via autenticação).
     * @return A entidade Sale recém-criada (com status inicial).
     */
    Sale initiateSale(CreateSaleCommand command, String buyerUserId);

    /**
     * Processa a falha na reserva do veículo.
     *
     * @param event Evento contendo os detalhes da falha.
     */
    void handleVehicleReservationFailure(VehicleReservationFailedEvent event);

    /**
     * Processa a falha na criação da cobrança.
     *
     * @param event Evento contendo os detalhes da falha.
     */
    void handleChargeCreationFailure(ChargeCreationFailedEvent event); // Assumindo que exista este evento

    /**
     * Processa a conclusão do pagamento.
     *
     * @param event Evento contendo os detalhes do pagamento.
     */
    void handlePaymentCompletion(PaymentCompletedEvent event);

    /**
     * Processa a falha no pagamento.
     *
     * @param event Evento contendo os detalhes da falha.
     */
    void handlePaymentFailure(PaymentFailedEvent event); // Assumindo que exista este evento

    /**
     * Processa a expiração da cobrança.
     *
     * @param event Evento contendo os detalhes da expiração.
     */
    void handleChargeExpiration(ChargeExpiredEvent event);

    // Outros métodos para buscar vendas, cancelar, etc., podem ser adicionados aqui.
    Sale findSaleById(UUID saleId); // Exemplo
}