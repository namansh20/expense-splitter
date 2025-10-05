-- Expense Splitter Database Schema Creation Script
-- This script creates the database and all required tables

CREATE DATABASE IF NOT EXISTS expense_splitter
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE expense_splitter;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
);

-- Groups table
CREATE TABLE IF NOT EXISTS expense_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_group_name (group_name),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
);

-- Group membership table
CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role ENUM('ADMIN', 'MEMBER') DEFAULT 'MEMBER',
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (group_id) REFERENCES expense_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_user (group_id, user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id)
);

-- Expense categories table
CREATE TABLE IF NOT EXISTS expense_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    color_code VARCHAR(7) DEFAULT '#007bff',
    icon VARCHAR(50),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_category_name (category_name),
    INDEX idx_category_name (category_name)
);

-- Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    paid_by BIGINT NOT NULL,
    category_id BIGINT,
    expense_title VARCHAR(200) NOT NULL,
    description TEXT,
    total_amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    expense_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    split_type ENUM('EQUAL', 'PERCENTAGE', 'CUSTOM') DEFAULT 'EQUAL',
    receipt_url VARCHAR(500),
    is_settled BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (group_id) REFERENCES expense_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (paid_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE SET NULL,
    INDEX idx_group_id (group_id),
    INDEX idx_paid_by (paid_by),
    INDEX idx_expense_date (expense_date),
    INDEX idx_created_at (created_at),
    INDEX idx_category_id (category_id),
    INDEX idx_is_settled (is_settled)
);

-- Expense splits table (stores how each expense is split among users)
CREATE TABLE IF NOT EXISTS expense_splits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount_owed DECIMAL(12, 2) NOT NULL,
    percentage DECIMAL(5, 2),
    is_settled BOOLEAN DEFAULT FALSE,
    settled_at TIMESTAMP NULL,
    notes VARCHAR(200),
    
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_expense_user (expense_id, user_id),
    INDEX idx_expense_id (expense_id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_settled (is_settled)
);

-- Settlements table (tracks payments between users)
CREATE TABLE IF NOT EXISTS settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    payer_id BIGINT NOT NULL,
    payee_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    settlement_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    payment_method VARCHAR(50),
    
    FOREIGN KEY (group_id) REFERENCES expense_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payee_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_group_id (group_id),
    INDEX idx_payer_id (payer_id),
    INDEX idx_payee_id (payee_id),
    INDEX idx_settlement_date (settlement_date),
    CHECK (payer_id != payee_id)
);

-- User balances view for quick balance calculations
CREATE OR REPLACE VIEW user_balances AS
SELECT 
    gm.group_id,
    gm.user_id,
    u.username,
    u.full_name,
    COALESCE(paid.total_paid, 0) - COALESCE(owed.total_owed, 0) + COALESCE(received.total_received, 0) - COALESCE(sent.total_sent, 0) AS net_balance
FROM group_members gm
JOIN users u ON gm.user_id = u.id
LEFT JOIN (
    SELECT paid_by, group_id, SUM(total_amount) as total_paid
    FROM expenses 
    GROUP BY paid_by, group_id
) paid ON paid.paid_by = gm.user_id AND paid.group_id = gm.group_id
LEFT JOIN (
    SELECT es.user_id, e.group_id, SUM(es.amount_owed) as total_owed
    FROM expense_splits es
    JOIN expenses e ON es.expense_id = e.id
    WHERE es.is_settled = FALSE
    GROUP BY es.user_id, e.group_id
) owed ON owed.user_id = gm.user_id AND owed.group_id = gm.group_id
LEFT JOIN (
    SELECT payee_id, group_id, SUM(amount) as total_received
    FROM settlements
    GROUP BY payee_id, group_id
) received ON received.payee_id = gm.user_id AND received.group_id = gm.group_id
LEFT JOIN (
    SELECT payer_id, group_id, SUM(amount) as total_sent
    FROM settlements
    GROUP BY payer_id, group_id
) sent ON sent.payer_id = gm.user_id AND sent.group_id = gm.group_id
WHERE gm.is_active = TRUE;