package com.fiap.autohub.autohub_sales_api.domain.exceptions;

import java.util.UUID;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException(String message) {
        super(message);
    }

    public SaleNotFoundException(UUID saleId) {
        super("Sale not found with id: " + saleId);
    }
}
