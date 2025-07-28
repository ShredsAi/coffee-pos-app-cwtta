-- Initial schema creation for Product Catalog

-- Product table
CREATE TABLE product (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    brand VARCHAR(255),
    model VARCHAR(255),
    sku VARCHAR(100) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    publication_status VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_product_name UNIQUE (name),
    CONSTRAINT uk_product_sku UNIQUE (sku),
    CONSTRAINT uk_product_slug UNIQUE (slug)
);

-- Category table
CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    slug VARCHAR(255) NOT NULL,
    parent_category_id UUID,
    level INTEGER NOT NULL,
    path VARCHAR(1000) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_category_slug UNIQUE (slug),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(id)
);

-- Product Attribute table
CREATE TABLE product_attribute (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    attribute_type VARCHAR(20) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    is_filterable BOOLEAN DEFAULT FALSE,
    is_searchable BOOLEAN DEFAULT FALSE,
    unit VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_attribute_code UNIQUE (code)
);

-- Attribute Option table
CREATE TABLE attribute_option (
    id UUID PRIMARY KEY,
    attribute_id UUID NOT NULL,
    value VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_option_attribute FOREIGN KEY (attribute_id) REFERENCES product_attribute(id) ON DELETE CASCADE
);

-- Product Attribute Value table
CREATE TABLE product_attribute_value (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    attribute_id UUID NOT NULL,
    text_value VARCHAR(500),
    numeric_value NUMERIC(19,4),
    boolean_value BOOLEAN,
    date_value DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_attribute_value_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_attribute_value_attribute FOREIGN KEY (attribute_id) REFERENCES product_attribute(id)
);

-- Attribute Value Option (junction table)
CREATE TABLE attribute_value_option (
    attribute_value_id UUID NOT NULL,
    option_id UUID NOT NULL,
    
    PRIMARY KEY (attribute_value_id, option_id),
    CONSTRAINT fk_value_option_value FOREIGN KEY (attribute_value_id) REFERENCES product_attribute_value(id) ON DELETE CASCADE,
    CONSTRAINT fk_value_option_option FOREIGN KEY (option_id) REFERENCES attribute_option(id) ON DELETE CASCADE
);

-- Product Media table
CREATE TABLE product_media (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    file_name VARCHAR(255),
    url VARCHAR(2048) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    alt_text VARCHAR(255),
    title VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    width INTEGER,
    height INTEGER,
    file_size BIGINT,
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_media_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Product Category (junction table)
CREATE TABLE product_category (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    category_id UUID NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    
    CONSTRAINT fk_product_category_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_category_category FOREIGN KEY (category_id) REFERENCES category(id)
);

-- Product Audit table
CREATE TABLE product_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    product_id UUID,
    change_type VARCHAR(10),
    before_state JSONB,
    after_state JSONB,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    changed_by VARCHAR(100)
);

-- Category Audit table
CREATE TABLE category_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    category_id UUID,
    change_type VARCHAR(10),
    before_state JSONB,
    after_state JSONB,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    changed_by VARCHAR(100)
);

-- Attribute Audit table
CREATE TABLE attribute_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    attribute_id UUID,
    change_type VARCHAR(10),
    before_state JSONB,
    after_state JSONB,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    changed_by VARCHAR(100)
);

-- Media Audit table
CREATE TABLE media_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    media_id UUID,
    change_type VARCHAR(10),
    before_state JSONB,
    after_state JSONB,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    changed_by VARCHAR(100)
);