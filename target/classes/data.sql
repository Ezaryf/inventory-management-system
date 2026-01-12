-- Sample Data for Inventory Management System

-- Categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Apparel and fashion items'),
('Food & Beverages', 'Consumable food and drink products'),
('Office Supplies', 'Office equipment and stationery'),
('Home & Garden', 'Home improvement and garden products'),
('Sports & Outdoors', 'Sports equipment and outdoor gear'),
('Health & Beauty', 'Health, wellness, and beauty products');

-- Suppliers
INSERT INTO suppliers (company_name, contact_person, email, phone, address) VALUES
('Tech Solutions Inc.', 'John Smith', 'john.smith@techsolutions.com', '+1-555-0101', '123 Tech Park, Silicon Valley, CA 94000'),
('Fashion Forward Ltd.', 'Emily Johnson', 'emily@fashionforward.com', '+1-555-0102', '456 Fashion Ave, New York, NY 10001'),
('Fresh Foods Co.', 'Michael Brown', 'michael@freshfoods.com', '+1-555-0103', '789 Farm Road, Austin, TX 78701'),
('Office Pro Supplies', 'Sarah Davis', 'sarah@officepro.com', '+1-555-0104', '321 Business Blvd, Chicago, IL 60601'),
('HomeStyle Distributors', 'David Wilson', 'david@homestyle.com', '+1-555-0105', '654 Home Lane, Seattle, WA 98101'),
('SportMax Global', 'Jessica Martinez', 'jessica@sportmax.com', '+1-555-0106', '987 Sports Center, Denver, CO 80201');

-- Products
INSERT INTO products (name, sku, description, category_id, supplier_id, unit_price, current_stock, reorder_level) VALUES
-- Electronics
('Wireless Bluetooth Headphones', 'ELEC-WBH-001', 'High-quality wireless Bluetooth headphones with noise cancellation', 1, 1, 79.99, 150, 25),
('USB-C Charging Cable 6ft', 'ELEC-UCC-002', 'Durable USB-C to USB-C fast charging cable', 1, 1, 14.99, 500, 100),
('Portable Power Bank 20000mAh', 'ELEC-PPB-003', 'High-capacity portable charger with dual USB ports', 1, 1, 39.99, 200, 50),
('Wireless Mouse', 'ELEC-WM-004', 'Ergonomic wireless mouse with adjustable DPI', 1, 1, 29.99, 8, 20),

-- Clothing
('Cotton T-Shirt Classic', 'CLTH-CTS-001', '100% cotton comfortable t-shirt', 2, 2, 19.99, 300, 50),
('Denim Jeans Regular Fit', 'CLTH-DJR-002', 'Classic regular fit denim jeans', 2, 2, 49.99, 150, 30),
('Winter Jacket Insulated', 'CLTH-WJI-003', 'Warm insulated winter jacket with hood', 2, 2, 89.99, 5, 15),

-- Food & Beverages
('Organic Coffee Beans 1kg', 'FOOD-OCB-001', 'Premium organic Arabica coffee beans', 3, 3, 24.99, 100, 25),
('Green Tea Collection Box', 'FOOD-GTC-002', 'Assorted green tea varieties - 50 bags', 3, 3, 12.99, 200, 40),
('Protein Energy Bars 12-Pack', 'FOOD-PEB-003', 'High-protein energy bars for active lifestyles', 3, 3, 29.99, 3, 20),

-- Office Supplies
('Ballpoint Pens 50-Pack', 'OFFC-BPP-001', 'Smooth writing ballpoint pens - assorted colors', 4, 4, 9.99, 400, 80),
('A4 Copy Paper 500 Sheets', 'OFFC-A4P-002', 'Premium quality A4 printing paper', 4, 4, 7.99, 600, 100),
('Desk Organizer Set', 'OFFC-DOS-003', 'Multi-compartment desk organizer', 4, 4, 24.99, 75, 15),

-- Home & Garden
('LED Desk Lamp Adjustable', 'HOME-LDL-001', 'Energy-efficient LED desk lamp with dimmer', 5, 5, 34.99, 120, 25),
('Garden Tool Set 5-Piece', 'HOME-GTS-002', 'Essential garden tools set with carrying bag', 5, 5, 44.99, 60, 15),

-- Sports & Outdoors
('Yoga Mat Premium', 'SPRT-YMP-001', 'Non-slip premium yoga mat 6mm thick', 6, 6, 29.99, 180, 30),
('Water Bottle Insulated 32oz', 'SPRT-WBI-002', 'Double-wall insulated stainless steel bottle', 6, 6, 24.99, 250, 50),
('Resistance Bands Set', 'SPRT-RBS-003', 'Set of 5 resistance bands with different strengths', 6, 6, 19.99, 140, 25);

-- Sample Inventory Transactions
INSERT INTO inventory_transactions (product_id, transaction_type, quantity, reference_number, notes, created_by) VALUES
(1, 'STOCK_IN', 200, 'PO-2024-001', 'Initial stock from supplier', 'admin'),
(1, 'STOCK_OUT', 50, 'SO-2024-001', 'Sold to customer', 'admin'),
(2, 'STOCK_IN', 600, 'PO-2024-002', 'Bulk purchase', 'admin'),
(2, 'STOCK_OUT', 100, 'SO-2024-002', 'Wholesale order', 'admin'),
(4, 'STOCK_IN', 50, 'PO-2024-003', 'Restocking', 'admin'),
(4, 'STOCK_OUT', 42, 'SO-2024-003', 'Multiple sales', 'admin'),
(7, 'STOCK_IN', 30, 'PO-2024-004', 'Seasonal stock', 'admin'),
(7, 'STOCK_OUT', 25, 'SO-2024-004', 'End of season sale', 'admin'),
(10, 'STOCK_IN', 25, 'PO-2024-005', 'Low stock replenishment', 'admin'),
(10, 'STOCK_OUT', 22, 'SO-2024-005', 'Customer orders', 'admin');

-- Users (passwords are BCrypt encoded - 'password123' for all)
INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@inventory.com', 'ADMIN', TRUE),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user@inventory.com', 'USER', TRUE),
('viewer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'viewer@inventory.com', 'VIEWER', TRUE);
