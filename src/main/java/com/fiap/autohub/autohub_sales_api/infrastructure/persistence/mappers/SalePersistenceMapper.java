package com.fiap.autohub.autohub_sales_api.infrastructure.persistence.mappers;

import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;
import com.fiap.autohub.autohub_sales_api.infrastructure.persistence.entities.SalePersistenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper entre a Entidade de Domínio Sale e a Entidade de Persistência JPA SalePersistenceEntity.
 */
@Mapper(componentModel = "spring")
public interface SalePersistenceMapper {

    SalePersistenceEntity toPersistenceEntity(Sale sale);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "vehicleId", target = "vehicleId")
    @Mapping(source = "buyerUserId", target = "buyerUserId")
    @Mapping(source = "sellerUserId", target = "sellerUserId")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "failureReason", target = "failureReason")
    @Mapping(source = "chargeId", target = "chargeId")
    @Mapping(source = "detranProcessId", target = "detranProcessId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "version", target = "version")
    Sale toDomainEntity(SalePersistenceEntity entity);
}