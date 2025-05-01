package com.fiap.autohub.autohub_sales_api.application.services;

import com.fiap.autohub.autohub_sales_api.domain.commands.CreateSaleCommand;
import com.fiap.autohub.autohub_sales_api.domain.entities.Sale;
import com.fiap.autohub.autohub_sales_api.domain.entities.SaleStatus;
import com.fiap.autohub.autohub_sales_api.domain.events.*;
import com.fiap.autohub.autohub_sales_api.domain.exceptions.SaleNotFoundException;
import com.fiap.autohub.autohub_sales_api.domain.ports.in.SaleServicePort;
import com.fiap.autohub.autohub_sales_api.domain.ports.out.SaleEventPublisherPort;
import com.fiap.autohub.autohub_sales_api.domain.ports.out.SaleRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementação da porta de entrada para os casos de uso de Vendas.
 * Orquestra a lógica de negócio e interage com as portas de saída.
 */
@Service
public class SaleServiceImpl implements SaleServicePort {

    private static final Logger log = LoggerFactory.getLogger(SaleServiceImpl.class);

    private final SaleRepositoryPort saleRepository;
    private final SaleEventPublisherPort eventPublisher;

    public SaleServiceImpl(SaleRepositoryPort saleRepository,
                           SaleEventPublisherPort eventPublisher
    ) {
        this.saleRepository = saleRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Sale initiateSale(CreateSaleCommand command, String buyerUserId) {
        log.info("Initiating sale for vehicle {} by buyer {}", command.vehicleId(), buyerUserId);

        Sale newSale = new Sale(
                command.vehicleId(),
                buyerUserId,
                command.sellerUserId(),
                command.price()
        );

        Sale savedSale = saleRepository.save(newSale);
        log.info("Sale record created with ID: {}", savedSale.getId());

        SaleCreatedEvent event = new SaleCreatedEvent(
                savedSale.getId(),
                savedSale.getVehicleId(),
                savedSale.getBuyerUserId(),
                savedSale.getSellerUserId(),
                savedSale.getPrice()
        );
        eventPublisher.publishSaleCreated(event);
        log.info("Published SaleCreatedEvent for sale ID: {}", savedSale.getId());

        return savedSale;
    }

    @Override
    @Transactional
    public void handleVehicleReservationFailure(VehicleReservationFailedEvent event) {
        log.warn("Handling VehicleReservationFailedEvent for sale ID: {}. Reason: {}", event.saleId(), event.reason());
        Sale sale = findSaleByIdOrThrow(event.saleId());
        if (sale.getStatus() == SaleStatus.PROCESSING) {
            sale.setStatus(SaleStatus.RESERVATION_FAILED);
            sale.setFailureReason(event.reason());
            saleRepository.save(sale);
            log.info("Sale {} status updated to RESERVATION_FAILED", event.saleId());
        } else {
            log.warn("Ignoring VehicleReservationFailedEvent for sale {} because its status is already {}", event.saleId(), sale.getStatus());
        }
    }

    @Override
    @Transactional
    public void handleChargeCreationFailure(ChargeCreationFailedEvent event) {
        log.warn("Handling ChargeCreationFailedEvent for sale ID: {}. Reason: {}", event.saleId(), event.reason());
        Sale sale = findSaleByIdOrThrow(event.saleId());
        if (sale.getStatus() == SaleStatus.PROCESSING) {
            sale.setFailureReason("Charge creation failed: " + event.reason());
            saleRepository.save(sale);
            log.info("Sale {} status updated to FAILED due to charge creation failure", event.saleId());
        } else {
            log.warn("Ignoring ChargeCreationFailedEvent for sale {} because its status is already {}", event.saleId(), sale.getStatus());
        }
    }

    @Override
    @Transactional
    public void handlePaymentCompletion(PaymentCompletedEvent event) {
        log.info("Handling PaymentCompletedEvent for sale ID: {}", event.saleId());
        Sale sale = findSaleByIdOrThrow(event.saleId());
        if (sale.getStatus() == SaleStatus.PROCESSING) {
            log.info("Payment completed for sale {}. Proceeding to documentation phase.", sale.getId());
            sale.setStatus(SaleStatus.PROCESSING_DOCUMENTATION);
            sale.setChargeId(event.chargeId());
            saleRepository.save(sale);

            // Simula chamada DETRAN
            boolean detranSuccess = true;

            if (detranSuccess) {
                sale.setStatus(SaleStatus.COMPLETED);
                saleRepository.save(sale);
                log.info("Sale {} status updated to COMPLETED", sale.getId());
            } else {
                sale.setFailureReason("DETRAN processing failed.");
                saleRepository.save(sale);
                log.error("Sale {} status updated to DOCUMENTATION_FAILED", sale.getId());
            }
        } else {
            log.warn("Ignoring PaymentCompletedEvent for sale {} because its status is already {}", event.saleId(), sale.getStatus());
        }
    }

    @Override
    @Transactional
    public void handlePaymentFailure(PaymentFailedEvent event) {
        log.warn("Handling PaymentFailedEvent for sale ID: {}. Reason: {}", event.saleId(), event.reason());
        Sale sale = findSaleByIdOrThrow(event.saleId());
        if (sale.getStatus() == SaleStatus.PROCESSING) {
            sale.setStatus(SaleStatus.PAYMENT_FAILED);
            sale.setFailureReason("Payment failed: " + event.reason());
            saleRepository.save(sale);
            log.info("Sale {} status updated to PAYMENT_FAILED", event.saleId());
        } else {
            log.warn("Ignoring PaymentFailedEvent for sale {} because its status is already {}", event.saleId(), sale.getStatus());
        }
    }

    @Override
    @Transactional
    public void handleChargeExpiration(ChargeExpiredEvent event) {
        log.warn("Handling ChargeExpiredEvent for sale ID: {}", event.saleId());
        Sale sale = findSaleByIdOrThrow(event.saleId());
        if (sale.getStatus() == SaleStatus.PROCESSING) {
            sale.setStatus(SaleStatus.PAYMENT_EXPIRED);
            sale.setFailureReason("Payment charge expired.");
            saleRepository.save(sale);
            log.info("Sale {} status updated to PAYMENT_EXPIRED", event.saleId());
        } else {
            log.warn("Ignoring ChargeExpiredEvent for sale {} because its status is already {}", event.saleId(), sale.getStatus());
        }
    }

    @Override
    public Sale findSaleById(UUID saleId) {
        log.debug("Finding sale by ID: {}", saleId);
        return findSaleByIdOrThrow(saleId);
    }

    private Sale findSaleByIdOrThrow(UUID saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> {
                    log.error("Sale not found with ID: {}", saleId);
                    return new SaleNotFoundException(saleId);
                });
    }
}