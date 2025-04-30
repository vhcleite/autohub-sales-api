package com.fiap.autohub.autohub_sales_api.infrastructure.persistence.repositories;

import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.entities.SaleAuditLogPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface Spring Data JPA para a entidade SaleAuditLogPersistenceEntity.
 */
@Repository
public interface JpaSaleAuditLogRepository extends JpaRepository<SaleAuditLogPersistenceEntity, Long> {
}