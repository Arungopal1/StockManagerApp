-- ============================================================================
-- StockManager Multi-Tenant Schema for Supabase (Free Tier)
-- Run this in Supabase Dashboard → SQL Editor
-- ============================================================================

-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- ============================================================================
-- SHOPS TABLE (Multi-tenant root)
-- ============================================================================
create table shops (
    id uuid primary key default uuid_generate_v4(),
    name text not null,
    slug text unique not null,                    -- URL-friendly: "downtown-mobile"
    owner_id uuid references auth.users(id),      -- Shop owner user
    settings jsonb default '{
        "currency": "INR",
        "timezone": "Asia/Kolkata",
        "tax_rate": 18,
        "low_stock_threshold": 5,
        "auto_backup": true
    }'::jsonb,
    is_active boolean default true,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

create index idx_shops_owner on shops(owner_id);
create index idx_shops_slug on shops(slug);

-- ============================================================================
-- EXTEND AUTH.USERS (via metadata, not direct column)
-- We use auth.users.raw_user_meta_data JSONB for shop_id & role
-- ============================================================================
-- No ALTER TABLE needed - use metadata:
-- raw_user_meta_data = {"shop_id": "uuid", "role": "owner"}

-- ============================================================================
-- PRODUCTS TABLE
-- ============================================================================
create table products (
    id uuid primary key default uuid_generate_v4(),
    shop_id uuid not null references shops(id) on delete cascade,
    
    -- Identification
    sku text not null,                            -- Unique per shop
    barcode text,                                 -- IMEI/Barcode for scanning
    name text not null,                           -- "iPhone 15 Pro"
    brand text not null,                          -- "Apple"
    model text not null,                          -- "iPhone 15 Pro"
    category text not null check (category in (
        'smartphone', 'tablet', 'laptop', 'accessory', 
        'part', 'case', 'charger', 'audio', 'wearable', 'other'
    )),
    color text not null,                          -- "Natural Titanium"
    storage text not null,                        -- "256GB"
    condition text not null check (condition in (
        'new', 'refurbished', 'used_good', 'used_fair', 'used_poor', 'for_parts'
    )),
    
    -- Pricing
    purchase_price numeric(10,2) not null,        -- Cost price
    selling_price numeric(10,2) not null,         -- Selling price
    
    -- Stock
    current_stock int default 0,
    min_stock_level int default 5,
    max_stock_level int default 100,
    location text,                                -- Shelf location: "A1-R3"
    
    -- Relations
    supplier_id uuid references suppliers(id),
    
    -- Details
    warranty_months int default 12,
    description text,
    image_url text,
    
    -- System
    is_active boolean default true,
    created_at timestamptz default now(),
    updated_at timestamptz default now(),
    
    -- Constraints
    unique (shop_id, sku),
    unique (shop_id, barcode)
);

create index idx_products_shop on products(shop_id);
create index idx_products_shop_category on products(shop_id, category);
create index idx_products_shop_brand on products(shop_id, brand);
create index idx_products_shop_stock on products(shop_id, current_stock);
create index idx_products_barcode on products(barcode) where barcode is not null;

-- ============================================================================
-- STOCK TRANSACTIONS TABLE
-- ============================================================================
create table stock_transactions (
    id uuid primary key default uuid_generate_v4(),
    shop_id uuid not null references shops(id) on delete cascade,
    product_id uuid not null references products(id) on delete restrict,
    
    transaction_type text not null check (transaction_type in (
        'stock_in', 'stock_out', 'return', 'adjustment', 'transfer'
    )),
    quantity int not null,                        -- Positive for IN, negative for OUT
    unit_cost numeric(10,2) not null,             -- Cost at time of transaction
    unit_price numeric(10,2) not null,            -- Price at time of transaction
    
    reference_number text not null,               -- GRN-2024-001, DN-2024-001
    supplier_id uuid references suppliers(id),
    customer_id uuid references customers(id),
    user_id uuid not null references auth.users(id),
    
    notes text,
    status text default 'completed' check (status in (
        'pending', 'completed', 'cancelled', 'partial'
    )),
    related_transaction_id uuid references stock_transactions(id), -- For returns
    location_from text,                           -- For transfers
    location_to text,                             -- For transfers
    
    created_at timestamptz default now()
);

create index idx_transactions_shop on stock_transactions(shop_id);
create index idx_transactions_product on stock_transactions(product_id);
create index idx_transactions_type on stock_transactions(transaction_type);
create index idx_transactions_date on stock_transactions(created_at desc);
create index idx_transactions_ref on stock_transactions(reference_number);
create index idx_transactions_user on stock_transactions(user_id);

-- ============================================================================
-- SUPPLIERS TABLE
-- ============================================================================
create table suppliers (
    id uuid primary key default uuid_generate_v4(),
    shop_id uuid not null references shops(id) on delete cascade,
    
    name text not null,
    contact_person text,
    email text,
    phone text,
    address text,
    city text,
    state text,
    country text default 'India',
    postal_code text,
    gst_number text,                              -- GSTIN
    
    payment_terms int default 30,                 -- Days
    credit_limit numeric(12,2) default 0,
    current_balance numeric(12,2) default 0,      -- Amount payable
    
    is_active boolean default true,
    notes text,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

create index idx_suppliers_shop on suppliers(shop_id);
create index idx_suppliers_name on suppliers(shop_id, name);

-- ============================================================================
-- CUSTOMERS TABLE
-- ============================================================================
create table customers (
    id uuid primary key default uuid_generate_v4(),
    shop_id uuid not null references shops(id) on delete cascade,
    
    name text not null,
    phone text not null,
    email text,
    address text,
    city text,
    state text,
    country text default 'India',
    postal_code text,
    gst_number text,
    
    customer_type text default 'retail' check (customer_type in (
        'retail', 'wholesale', 'corporate', 'online', 'walk_in'
    )),
    credit_limit numeric(12,2) default 0,
    current_balance numeric(12,2) default 0,      -- Amount receivable
    loyalty_points int default 0,
    
    is_active boolean default true,
    notes text,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

create index idx_customers_shop on customers(shop_id);
create index idx_customers_phone on customers(shop_id, phone);
create unique index idx_customers_phone_unique on customers(phone) where phone is not null;

-- ============================================================================
-- SETTINGS TABLE (Per shop configuration)
-- ============================================================================
create table settings (
    id uuid primary key default uuid_generate_v4(),
    shop_id uuid not null references shops(id) on delete cascade unique,
    
    -- Business info
    business_name text,
    business_address text,
    business_phone text,
    business_email text,
    gst_number text,
    
    -- Invoice/Receipt
    invoice_prefix text default 'INV',
    invoice_start_number int default 1,
    invoice_terms text,
    
    -- Stock defaults
    default_min_stock int default 5,
    default_max_stock int default 100,
    default_warranty_months int default 12,
    
    -- Features
    enable_barcode_scanning boolean default true,
    enable_loyalty_points boolean default false,
    points_per_rupee numeric(4,2) default 0.10,
    
    -- Notifications
    low_stock_alert boolean default true,
    daily_summary_email boolean default false,
    
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) - MULTI-TENANT ISOLATION
-- ============================================================================

-- Enable RLS on all tables
alter table shops enable row level security;
alter table products enable row level security;
alter table stock_transactions enable row level security;
alter table suppliers enable row level security;
alter table customers enable row level security;
alter table settings enable row level security;

-- Helper function: Get current user's shop_id from JWT metadata
create or replace function current_shop_id()
returns uuid language sql stable as $$
    select (auth.jwt() -> 'user_metadata' ->> 'shop_id')::uuid
$$;

-- Helper function: Get current user's role
create or replace function current_user_role()
returns text language sql stable as $$
    select auth.jwt() -> 'user_metadata' ->> 'role'
$$;

-- Helper function: Check if user is shop owner/admin
create or replace function is_shop_admin()
returns boolean language sql stable as $$
    select current_user_role() in ('owner', 'manager')
$$;

-- ============================================================================
-- RLS POLICIES
-- ============================================================================

-- SHOPS: Users can only see their own shop (unless admin)
create policy "shops_select_own" on shops
    for select using (
        id = current_shop_id() 
        or auth.jwt() -> 'user_metadata' ->> 'role' = 'super_admin'
    );

create policy "shops_insert_owner" on shops
    for insert with check (
        owner_id = auth.uid()
        or auth.jwt() -> 'user_metadata' ->> 'role' = 'super_admin'
    );

create policy "shops_update_owner" on shops
    for update using (
        owner_id = auth.uid() 
        or is_shop_admin()
        or auth.jwt() -> 'user_metadata' ->> 'role' = 'super_admin'
    );

-- PRODUCTS: Full isolation by shop_id
create policy "products_all" on products
    for all using (
        shop_id = current_shop_id()
    ) with check (
        shop_id = current_shop_id()
    );

-- STOCK TRANSACTIONS: Full isolation by shop_id
create policy "transactions_all" on stock_transactions
    for all using (
        shop_id = current_shop_id()
    ) with check (
        shop_id = current_shop_id()
    );

-- SUPPLIERS: Full isolation by shop_id
create policy "suppliers_all" on suppliers
    for all using (
        shop_id = current_shop_id()
    ) with check (
        shop_id = current_shop_id()
    );

-- CUSTOMERS: Full isolation by shop_id
create policy "customers_all" on customers
    for all using (
        shop_id = current_shop_id()
    ) with check (
        shop_id = current_shop_id()
    );

-- SETTINGS: Full isolation by shop_id
create policy "settings_all" on settings
    for all using (
        shop_id = current_shop_id()
    ) with check (
        shop_id = current_shop_id()
    );

-- ============================================================================
-- REALTIME PUBLICATION (for live sync)
-- ============================================================================
alter publication supabase_realtime add table products, stock_transactions, suppliers, customers;

-- ============================================================================
-- HELPER VIEWS (for reporting)
-- ============================================================================

-- Current stock value per shop
create view shop_stock_summary as
select 
    p.shop_id,
    count(*) as total_products,
    sum(case when p.current_stock = 0 then 1 else 0 end) as out_of_stock_count,
    sum(case when p.current_stock <= p.min_stock_level and p.current_stock > 0 then 1 else 0 end) as low_stock_count,
    sum(p.current_stock * p.purchase_price) as total_cost_value,
    sum(p.current_stock * p.selling_price) as total_retail_value
from products p
where p.is_active = true
group by p.shop_id;

-- Daily transaction summary
create view daily_transaction_summary as
select 
    st.shop_id,
    date(st.created_at) as transaction_date,
    st.transaction_type,
    count(*) as transaction_count,
    sum(st.quantity) as total_quantity,
    sum(st.quantity * st.unit_cost) as total_cost,
    sum(st.quantity * st.unit_price) as total_value
from stock_transactions st
where st.status = 'completed'
group by st.shop_id, date(st.created_at), st.transaction_type;

-- Monthly profit report
create view monthly_profit_report as
select 
    st.shop_id,
    date_trunc('month', st.created_at) as month,
    p.name as product_name,
    p.sku,
    sum(case when st.transaction_type = 'stock_in' then st.quantity * st.unit_cost else 0 end) as total_cost,
    sum(case when st.transaction_type = 'stock_out' then abs(st.quantity) * st.unit_price else 0 end) as total_revenue,
    sum(case when st.transaction_type = 'stock_out' then abs(st.quantity) * (st.unit_price - st.unit_cost) else 0 end) as gross_profit
from stock_transactions st
join products p on st.product_id = p.id
where st.status = 'completed'
  and st.transaction_type in ('stock_in', 'stock_out')
group by st.shop_id, date_trunc('month', st.created_at), p.id, p.name, p.sku;

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================
create or replace function update_updated_at_column()
returns trigger language plpgsql as $$
begin
    new.updated_at = now();
    return new;
end $$;

create trigger update_shops_updated_at before update on shops for each row execute function update_updated_at_column();
create trigger update_products_updated_at before update on products for each row execute function update_updated_at_column();
create trigger update_suppliers_updated_at before update on suppliers for each row execute function update_updated_at_column();
create trigger update_customers_updated_at before update on customers for each row execute function update_updated_at_column();
create trigger update_settings_updated_at before update on settings for each row execute function update_updated_at_column();

-- ============================================================================
-- STORAGE BUCKET FOR PRODUCT IMAGES
-- ============================================================================
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('product-images', 'product-images', true, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do nothing;

-- Storage policies
create policy "product_images_select" on storage.objects for select using (bucket_id = 'product-images');
create policy "product_images_insert" on storage.objects for insert with check (
    bucket_id = 'product-images' 
    and (storage.foldername(name))[1] = current_shop_id()::text
);
create policy "product_images_update" on storage.objects for update using (
    bucket_id = 'product-images' 
    and (storage.foldername(name))[1] = current_shop_id()::text
);
create policy "product_images_delete" on storage.objects for delete using (
    bucket_id = 'product-images' 
    and (storage.foldername(name))[1] = current_shop_id()::text
);

-- ============================================================================
-- SAMPLE DATA (for testing - remove in production)
-- ============================================================================
-- Insert a test shop
-- insert into shops (name, slug, owner_id) values ('Demo Mobile Shop', 'demo-mobile', auth.uid());
-- Insert settings for the shop
-- insert into settings (shop_id) select id from shops where slug = 'demo-mobile';