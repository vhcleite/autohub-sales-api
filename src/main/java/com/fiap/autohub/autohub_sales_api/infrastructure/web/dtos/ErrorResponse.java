package com.fiap.autohub.autohub_sales_api.infrastructure.web.dtos;

/**
 * DTO padrão para respostas de erro.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}