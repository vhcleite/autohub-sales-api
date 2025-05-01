package com.fiap.autohub.autohub_sales_api.infrastructure.web.controllers;

import com.fiap.autohub.autohub_sales_api.domain.commands.CreateSaleCommand;
import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;
import com.fiap.autohub.autohub_sales_api.domain.ports.in.SaleServicePort;
import com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos.CreateSaleRequestDto;
import com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos.ErrorResponse;
import com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos.SaleResponseDto;
import com.fiap.autohub.autohub_sales_api.infrastructure.web.mappers.SaleDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Controller REST para as operações da API de Vendas.
 */
@RestController
@RequestMapping("/sales")
@Tag(name = "Sales Management", description = "Endpoints para gerenciamento de vendas")
public class SaleController {

    private static final Logger log = LoggerFactory.getLogger(SaleController.class);

    private final SaleServicePort saleService;
    private final SaleDtoMapper mapper;

    public SaleController(SaleServicePort saleService, SaleDtoMapper mapper) {
        this.saleService = saleService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Inicia uma nova venda", description = "Cria um registro inicial de venda e dispara o processo assíncrono.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venda iniciada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SaleResponseDto> initiateSale(
            @Valid @RequestBody CreateSaleRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String buyerUserId = jwt.getSubject();
        log.info("Received request to initiate sale for vehicle {} from buyer {}", requestDto.vehicleId(), buyerUserId);

        CreateSaleCommand command = mapper.toCreateCommand(requestDto);
        Sale initiatedSale = saleService.initiateSale(command, buyerUserId);

        log.info("Sale initiated successfully with ID: {}", initiatedSale.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDto(initiatedSale));
    }

    // Exemplo de endpoint GET (descomentar e ajustar se necessário)

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma venda por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venda encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Proibido (usuário não é comprador/vendedor)"),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SaleResponseDto> getSaleById(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get sale {} by user {}", id, userId);
        Sale sale = saleService.findSaleById(id);
        if (!sale.getBuyerUserId().equals(userId) && !sale.getSellerUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized to view this sale");
        }
        return ResponseEntity.ok(mapper.toResponseDto(sale));
    }


}