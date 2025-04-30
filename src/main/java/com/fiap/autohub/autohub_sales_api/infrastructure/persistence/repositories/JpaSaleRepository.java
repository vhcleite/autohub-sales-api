package com.fiap.autohub.autohub_sales_api.infrastructure.persistence.repositories;

import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.entities.SalePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interface Spring Data JPA para a entidade SalePersistenceEntity.
 * Fornece métodos CRUD básicos.
 */
@Repository
public interface JpaSaleRepository extends JpaRepository<SalePersistenceEntity, UUID> {
    List<SalePersistenceEntity> findByBuyerUserId(String buyerUserId);
}