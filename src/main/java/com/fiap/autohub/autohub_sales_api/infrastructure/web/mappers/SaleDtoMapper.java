package com.fiap.autohub.autohub_sales_api.infrastructure.web.mappers;

import com.fiap.autohub.autohub_sales_api.domain.commands.CreateSaleCommand;
import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;
import com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos.CreateSaleRequestDto;
import com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos.SaleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper entre DTOs da camada Web e objetos de Domínio (Commands, Entities).
 * Usa MapStruct com componente Spring.
 */
@Mapper(componentModel = "spring")
public interface SaleDtoMapper {

    CreateSaleCommand toCreateCommand(CreateSaleRequestDto dto);

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
    SaleResponseDto toResponseDto(Sale sale);

    List<SaleResponseDto> toResponseDtoList(List<Sale> sales);
}