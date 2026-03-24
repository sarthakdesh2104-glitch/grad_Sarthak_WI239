CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL CHECK (role IN ('admin', 'user')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);

CREATE TABLE sites (
    site_id SERIAL PRIMARY KEY,
    site_type VARCHAR(20) NOT NULL CHECK (site_type IN ('Villa', 'Apartment', 'Independent House', 'Open Site')),
    length INT NOT NULL CHECK (length > 0),
    width INT NOT NULL CHECK (width > 0),
    size_sqft INT NOT NULL CHECK (size_sqft > 0),
    price_per_sqft DECIMAL(10, 2) NOT NULL CHECK (price_per_sqft >= 0),
    is_owned BOOLEAN DEFAULT FALSE,
    owner_id INT REFERENCES users(user_id) ON DELETE SET NULL,
    remaining_maintenance DECIMAL(10, 2) DEFAULT 0.00 CHECK (remaining_maintenance >= 0),
    status VARCHAR(20) DEFAULT 'Approved' CHECK (status IN ('Approved', 'Pending Approval')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sites_owner_id ON sites(owner_id);
CREATE INDEX idx_sites_is_owned ON sites(is_owned);
CREATE INDEX idx_sites_site_type ON sites(site_type);

CREATE TABLE site_requests (
    request_id SERIAL PRIMARY KEY,
    site_id INT NOT NULL REFERENCES sites(site_id) ON DELETE CASCADE,
    owner_id INT NOT NULL REFERENCES users(user_id),
    request_type VARCHAR(50) NOT NULL CHECK (request_type IN ('Update Details', 'Payment Confirmation', 'Vacating Site')),
    paid_amount DECIMAL(10, 2) CHECK (paid_amount > 0),
    requested_data JSONB,
    request_status VARCHAR(20) DEFAULT 'Pending' CHECK (request_status IN ('Pending', 'Approved', 'Rejected')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_remarks TEXT
);

CREATE INDEX idx_site_requests_site_id ON site_requests(site_id);
CREATE INDEX idx_site_requests_owner_id ON site_requests(owner_id);
CREATE INDEX idx_site_requests_status ON site_requests(request_status);
