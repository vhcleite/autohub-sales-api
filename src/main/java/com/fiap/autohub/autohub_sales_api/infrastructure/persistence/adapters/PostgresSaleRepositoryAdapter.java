package com.fiap.autohub.autohub_sales_api.infrastructure.persistence.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;
import com.fiap.autohub.autohub_sales_api.domain.ports.out.SaleRepositoryPort;
import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.entities.SaleAuditLogPersistenceEntity;
import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.entities.SalePersistenceEntity;
import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.mappers.SalePersistenceMapper;
import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.repositories.JpaSaleAuditLogRepository;
import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.repositories.JpaSaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa a porta SaleRepositoryPort usando JPA e o JpaSaleRepository.
 * Também lida com a lógica de salvar o log de auditoria.
 */
@Component
public class PostgresSaleRepositoryAdapter implements SaleRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(PostgresSaleRepositoryAdapter.class);

    private final JpaSaleRepository jpaSaleRepository;
    private final JpaSaleAuditLogRepository auditLogRepository;
    private final SalePersistenceMapper mapper;
    private final ObjectMapper objectMapper;

    public PostgresSaleRepositoryAdapter(JpaSaleRepository jpaSaleRepository,
                                         JpaSaleAuditLogRepository auditLogRepository,
                                         SalePersistenceMapper mapper,
                                         ObjectMapper objectMapper) {
        this.jpaSaleRepository = jpaSaleRepository;
        this.auditLogRepository = auditLogRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Sale save(Sale sale) {
        SalePersistenceEntity entityToSave = mapper.toPersistenceEntity(sale);

        // O JPA/Hibernate cuidará da versão e timestamps ao salvar a entidade
        SalePersistenceEntity savedEntity = jpaSaleRepository.save(entityToSave);
        log.debug("Saved Sale entity with ID: {}", savedEntity.getId());

        saveAuditLogInternal(savedEntity);

        return mapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Sale> findById(UUID id) {
        log.debug("Finding Sale entity by ID: {}", id);
        return jpaSaleRepository.findById(id).map(mapper::toDomainEntity);
    }

    private void saveAuditLogInternal(SalePersistenceEntity entity) {
        try {
            String snapshot = objectMapper.writeValueAsString(entity);

            SaleAuditLogPersistenceEntity logEntry = new SaleAuditLogPersistenceEntity();
            logEntry.setSaleId(entity.getId());
            logEntry.setChangedBy("SYSTEM");
            logEntry.setSaleDataSnapshot(snapshot);

            auditLogRepository.save(logEntry);
            log.debug("Saved audit log for sale ID: {}", entity.getId());

        } catch (JsonProcessingException e) {
            log.error("CRITICAL: Failed to serialize sale snapshot for audit log. Sale ID: {}", entity.getId(), e);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to save audit log for sale ID: {}", entity.getId(), e);
        }
    }
}
