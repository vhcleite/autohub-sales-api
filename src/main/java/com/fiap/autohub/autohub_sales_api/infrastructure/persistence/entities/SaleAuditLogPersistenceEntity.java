package com.fiap.autohub.autohub_sales_api.infrastructure.persistence.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Entidade JPA para a tabela de auditoria 'sales_audit_log'.
 */
@Entity
@Table(name = "sales_audit_log", indexes = {
        @Index(name = "idx_sales_audit_sale_id", columnList = "sale_id")
})
public class SaleAuditLogPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false)
    private Long auditId;

    @Column(name = "sale_id", nullable = false)
    private UUID saleId;

    @Column(name = "change_timestamp", nullable = false)
    private OffsetDateTime changeTimestamp;

    @Column(name = "changed_by")
    private String changedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sale_data_snapshot", columnDefinition = "jsonb", nullable = false)
    private String saleDataSnapshot;

    public SaleAuditLogPersistenceEntity() {
        this.changeTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
    }

    // Getters e Setters
    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public UUID getSaleId() {
        return saleId;
    }

    public void setSaleId(UUID saleId) {
        this.saleId = saleId;
    }

    public OffsetDateTime getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(OffsetDateTime changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getSaleDataSnapshot() {
        return saleDataSnapshot;
    }

    public void setSaleDataSnapshot(String saleDataSnapshot) {
        this.saleDataSnapshot = saleDataSnapshot;
    }
}
