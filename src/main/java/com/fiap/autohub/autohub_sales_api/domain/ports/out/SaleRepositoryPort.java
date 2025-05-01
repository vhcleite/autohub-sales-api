package com.fiap.autohub.autohub_sales_api.domain.ports.out;

import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para interações com o repositório de persistência de Vendas.
 * Abstrai os detalhes de implementação do banco de dados.
 */
public interface SaleRepositoryPort {

    /**
     * Salva (cria ou atualiza) uma entidade Sale.
     *
     * @param sale A entidade a ser salva.
     * @return A entidade Sale salva (pode ter ID ou versão atualizados).
     */
    Sale save(Sale sale);

    /**
     * Busca uma venda pelo seu ID.
     *
     * @param id O ID da venda.
     * @return Um Optional contendo a venda se encontrada, ou vazio caso contrário.
     */
    Optional<Sale> findById(UUID id);
}