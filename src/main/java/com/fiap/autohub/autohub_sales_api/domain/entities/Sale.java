package com.fiap.autohub.autohub_sales_api.domain.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa a entidade de domínio de uma Venda.
 * Usamos uma classe Java padrão para permitir mutabilidade controlada.
 */
public class Sale {

    private UUID id;
    private UUID vehicleId;
    private String buyerUserId;
    private String sellerUserId;
    private BigDecimal price;
    private SaleStatus status;
    private String failureReason;
    private String chargeId;
    private String detranProcessId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;

    public Sale() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }

    public Sale(UUID vehicleId, String buyerUserId, String sellerUserId, BigDecimal price) {
        this();
        this.id = null;
        this.vehicleId = vehicleId;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.price = price;
        this.status = SaleStatus.PROCESSING;
    }

    public Sale(UUID id, UUID vehicleId, String buyerUserId, String sellerUserId, BigDecimal price,
                SaleStatus status, String failureReason, String chargeId, String detranProcessId,
                OffsetDateTime createdAt, OffsetDateTime updatedAt, Long version) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.price = price;
        this.status = status;
        this.failureReason = failureReason;
        this.chargeId = chargeId;
        this.detranProcessId = detranProcessId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public String getBuyerUserId() {
        return buyerUserId;
    }

    public String getSellerUserId() {
        return sellerUserId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getChargeId() {
        return chargeId;
    }

    public String getDetranProcessId() {
        return detranProcessId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setBuyerUserId(String buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public void setSellerUserId(String sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        if (failureReason != null && this.status != SaleStatus.FAILED) {
            this.setStatus(SaleStatus.FAILED);
        } else {
            this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void setDetranProcessId(String detranProcessId) {
        this.detranProcessId = detranProcessId;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}