-- Insert initial warehouses
INSERT INTO warehouse (id, code, name, street, city, state, postal_code, country, is_active, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 'WH001', 'Main Warehouse', '123 Main St', 'New York', 'NY', '10001', 'US', TRUE, NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'WH002', 'Secondary Warehouse', '456 Oak Ave', 'Los Angeles', 'CA', '90001', 'US', TRUE, NOW(), NOW());

-- Insert initial inventory item
INSERT INTO inventory_item (id, warehouse_id, product_id, available_qty, reserved_qty, allocated_qty, total_qty, qty_unit, safety_stock_level, reorder_point, last_movement_at, version) VALUES
('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE', 1000.000, 100.000, 50.000, 1150.000, 'pieces', 100.000, 200.000, NOW(), 0);

-- Insert initial batch
INSERT INTO batch (id, warehouse_id, product_id, batch_number, quantity, qty_unit, manufacturing_date, expiration_date, received_at, supplier_id, version) VALUES
('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE', 'BATCH001', 500.000, 'pieces', '2024-01-01', '2025-01-01', NOW(), '55555555-5555-5555-5555-555555555555', 0);

-- Insert initial stock movement
INSERT INTO stock_movement (id, warehouse_id, product_id, movement_type, quantity, qty_unit, reference_id, reference_type, batch_id, reason, performed_by, performed_at, cost_per_unit, cost_currency, created_at) VALUES
('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 'AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE', 'INBOUND', 500.000, 'pieces', NULL, NULL, '44444444-4444-4444-4444-444444444444', 'Initial stock', 'system', NOW(), 10.00, 'USD', NOW());
