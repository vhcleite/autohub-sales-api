-- Flyway Migration Script - V1
-- Cria as tabelas 'sales' e 'sales_audit_log' para a Sales API

-- Habilita a extensão para gerar UUIDs, caso ainda não esteja habilitada.
-- É seguro executar mesmo que já exista.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela principal de Vendas
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- ID único da venda
    vehicle_id UUID NOT NULL,                        -- ID do veículo (FK conceitual)
    buyer_user_id VARCHAR(255) NOT NULL,             -- ID do comprador (Cognito Sub)
    seller_user_id VARCHAR(255) NOT NULL,            -- ID do vendedor (Cognito Sub)
    price NUMERIC(12, 2) NOT NULL,                   -- Preço final da venda
    status VARCHAR(50) NOT NULL,                     -- Status atual da venda (PENDING_RESERVATION, COMPLETED, FAILED, etc.)
    failure_reason TEXT,                             -- Motivo da falha (opcional)
    charge_id VARCHAR(255),                          -- ID da cobrança (opcional)
    detran_process_id VARCHAR(255),                  -- ID do processo DETRAN (opcional)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp de criação
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp da última atualização
    version BIGINT NOT NULL DEFAULT 0                -- Versão para controle de concorrência
);

-- Índices para otimizar consultas comuns na tabela 'sales'
CREATE INDEX idx_sales_vehicle_id ON sales (vehicle_id);
CREATE INDEX idx_sales_buyer_user_id ON sales (buyer_user_id);
CREATE INDEX idx_sales_status ON sales (status);
CREATE INDEX idx_sales_charge_id ON sales (charge_id);

-- Tabela de Auditoria de Vendas (com snapshot JSON)
CREATE TABLE sales_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,                  -- ID auto-incremental da auditoria
    sale_id UUID NOT NULL,                           -- ID da venda auditada (FK conceitual)
    change_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp da auditoria
    changed_by VARCHAR(255),                         -- Quem/O que causou a mudança (opcional)
    sale_data_snapshot JSONB NOT NULL                -- Snapshot JSON do registro da venda
);

-- Índice para buscar auditorias por ID da venda
CREATE INDEX idx_sales_audit_sale_id ON sales_audit_log (sale_id);
