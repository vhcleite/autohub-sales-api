package com.fiap.autohub.autohub_sales_api.domain.ports.out;

import com.fiap.autohub.autohub_sales_api.domain.events.SaleCreatedEvent;

/**
 * Porta de saída para publicação de eventos de negócio relacionados a Vendas.
 * Abstrai os detalhes do mecanismo de mensageria (ex: SNS).
 */
public interface SaleEventPublisherPort {

    /**
     * Publica um evento indicando que uma nova venda foi criada.
     *
     * @param event O evento a ser publicado.
     */
    void publishSaleCreated(SaleCreatedEvent event);

    // Outros métodos para publicar outros eventos originados pela Sales API, se houver.
}