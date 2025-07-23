-- ====================================
-- Stock Ledger Management Database Schema
-- ====================================

-- Create warehouse table
CREATE TABLE IF NOT EXISTS warehouse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100) UNIQUE NOT NULL,
    street VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Create inventory_item table
CREATE TABLE IF NOT EXISTS inventory_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouse(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    available_qty NUMERIC(19,6) NOT NULL DEFAULT 0,
    reserved_qty NUMERIC(19,6) NOT NULL DEFAULT 0,
    allocated_qty NUMERIC(19,6) NOT NULL DEFAULT 0,
    total_qty NUMERIC(19,6) NOT NULL DEFAULT 0,
    qty_unit VARCHAR(20) NOT NULL,
    safety_stock_level NUMERIC(19,6) NOT NULL DEFAULT 0,
    reorder_point NUMERIC(19,6) NOT NULL DEFAULT 0,
    last_movement_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create batch table
CREATE TABLE IF NOT EXISTS batch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouse(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    batch_number VARCHAR(64) NOT NULL,
    quantity NUMERIC(19,6) NOT NULL DEFAULT 0,
    qty_unit VARCHAR(20) NOT NULL,
    manufacturing_date DATE,
    expiration_date DATE,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    supplier_id UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create stock_movement table
CREATE TABLE IF NOT EXISTS stock_movement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouse(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    movement_type VARCHAR(20) NOT NULL CHECK (movement_type IN ('INBOUND','OUTBOUND','TRANSFER','ADJUSTMENT')),
    quantity NUMERIC(19,6) NOT NULL,
    qty_unit VARCHAR(20) NOT NULL,
    reference_id VARCHAR(64),
    reference_type VARCHAR(32),
    batch_id UUID REFERENCES batch(id),
    reason VARCHAR(500),
    performed_by VARCHAR(64) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    cost_per_unit NUMERIC(19,4),
    cost_currency CHAR(3),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Create event_outbox table
CREATE TABLE IF NOT EXISTS event_outbox (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID,
    aggregate_type VARCHAR(64),
    event_type VARCHAR(64),
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- ====================================
-- Create Indexes for Performance
-- ====================================

-- Unique composite index for inventory_item
CREATE UNIQUE INDEX IF NOT EXISTS idx_inventory_item_composite 
ON inventory_item(warehouse_id, product_id);

-- Index for batch queries (warehouse + product)
CREATE INDEX IF NOT EXISTS idx_batch_warehouse_product 
ON batch(warehouse_id, product_id);

-- Index for batch FIFO ordering
CREATE INDEX IF NOT EXISTS idx_batch_fifo 
ON batch(warehouse_id, product_id, received_at ASC);

-- Unique constraint for batch number within warehouse-product
CREATE UNIQUE INDEX IF NOT EXISTS idx_batch_unique_number 
ON batch(warehouse_id, product_id, batch_number);

-- Index for stock movement queries
CREATE INDEX IF NOT EXISTS idx_movement_warehouse_product_date 
ON stock_movement(warehouse_id, product_id, performed_at DESC);

-- Index for stock movement by type
CREATE INDEX IF NOT EXISTS idx_movement_type_date 
ON stock_movement(movement_type, performed_at DESC);

-- Index for event_outbox processing
CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed 
ON event_outbox(processed, created_at ASC) WHERE processed = FALSE;

-- Index for event_outbox by aggregate
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate 
ON event_outbox(aggregate_type, aggregate_id);

-- ====================================
-- Create Triggers for Updated_at
-- ====================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for warehouse table
CREATE TRIGGER update_warehouse_updated_at BEFORE UPDATE ON warehouse
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();