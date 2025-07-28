-- Create indexes for performance optimization

-- Unique indexes (some already handled by constraints in V1)
CREATE UNIQUE INDEX IF NOT EXISTS idx_product_sku ON product(sku);
CREATE UNIQUE INDEX IF NOT EXISTS idx_product_slug ON product(slug);
CREATE UNIQUE INDEX IF NOT EXISTS idx_product_name ON product(name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_category_slug ON category(slug);
CREATE UNIQUE INDEX IF NOT EXISTS idx_attribute_code ON product_attribute(code);

-- Full-text search indexes using GIN
CREATE INDEX idx_product_name_gin ON product USING GIN(to_tsvector('english', name));
CREATE INDEX idx_product_description_gin ON product USING GIN(to_tsvector('english', description));
CREATE INDEX idx_product_short_description_gin ON product USING GIN(to_tsvector('english', short_description));

-- Product category relationship indexes
CREATE INDEX idx_product_category ON product_category(category_id, product_id);
CREATE INDEX idx_product_category_product ON product_category(product_id);

-- Product filtering indexes
CREATE INDEX idx_product_brand ON product(brand);
CREATE INDEX idx_product_model ON product(model);
CREATE INDEX idx_product_status ON product(publication_status);
CREATE INDEX idx_product_active ON product(is_active);
CREATE INDEX idx_product_created_at ON product(created_at);
CREATE INDEX idx_product_updated_at ON product(updated_at);

-- Category hierarchy indexes
CREATE INDEX idx_category_parent ON category(parent_category_id);
CREATE INDEX idx_category_level ON category(level);
CREATE INDEX idx_category_path ON category(path);
CREATE INDEX idx_category_sort_order ON category(sort_order);

-- Attribute indexes
CREATE INDEX idx_attribute_type ON product_attribute(attribute_type);
CREATE INDEX idx_attribute_filterable ON product_attribute(is_filterable);
CREATE INDEX idx_attribute_searchable ON product_attribute(is_searchable);
CREATE INDEX idx_attribute_required ON product_attribute(is_required);

-- Attribute value indexes for EAV performance
CREATE INDEX idx_attribute_value_product ON product_attribute_value(product_id);
CREATE INDEX idx_attribute_value_attribute ON product_attribute_value(attribute_id);
CREATE INDEX idx_attribute_value_text ON product_attribute_value(text_value);
CREATE INDEX idx_attribute_value_numeric ON product_attribute_value(numeric_value);
CREATE INDEX idx_attribute_value_boolean ON product_attribute_value(boolean_value);
CREATE INDEX idx_attribute_value_date ON product_attribute_value(date_value);

-- Attribute option indexes
CREATE INDEX idx_attribute_option_attribute ON attribute_option(attribute_id);
CREATE INDEX idx_attribute_option_value ON attribute_option(value);

-- Media indexes
CREATE INDEX idx_media_product ON product_media(product_id, sort_order);
CREATE INDEX idx_media_type ON product_media(media_type);
CREATE INDEX idx_media_uploaded_at ON product_media(uploaded_at);

-- Unique constraints for business rules
CREATE UNIQUE INDEX idx_product_primary_category ON product_category(product_id) WHERE is_primary = true;
CREATE UNIQUE INDEX idx_product_primary_media ON product_media(product_id) WHERE is_primary = true;

-- Composite indexes for common query patterns
CREATE INDEX idx_product_status_active ON product(publication_status, is_active);
CREATE INDEX idx_product_brand_status ON product(brand, publication_status);
CREATE INDEX idx_category_parent_sort ON category(parent_category_id, sort_order);

-- Audit table indexes for performance
CREATE INDEX idx_product_audit_product_id ON product_audit(product_id);
CREATE INDEX idx_product_audit_changed_at ON product_audit(changed_at);
CREATE INDEX idx_category_audit_category_id ON category_audit(category_id);
CREATE INDEX idx_attribute_audit_attribute_id ON attribute_audit(attribute_id);
CREATE INDEX idx_media_audit_media_id ON media_audit(media_id);

-- Partial indexes for active records only
CREATE INDEX idx_product_active_published ON product(id) WHERE is_active = true AND publication_status = 'PUBLISHED';
CREATE INDEX idx_category_active_only ON category(id, name) WHERE is_active = true;
CREATE INDEX idx_attribute_active_only ON product_attribute(id, code) WHERE is_active = true;