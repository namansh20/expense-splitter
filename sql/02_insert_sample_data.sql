-- Sample Data for Expense Splitter Application
USE expense_splitter;

-- Insert default expense categories
INSERT INTO expense_categories (category_name, description, color_code, icon, is_default) VALUES
('Food & Dining', 'Restaurant meals, groceries, takeout', '#FF6B6B', '🍽️', TRUE),
('Transportation', 'Gas, parking, public transport, ride-sharing', '#4ECDC4', '🚗', TRUE),
('Entertainment', 'Movies, concerts, games, activities', '#45B7D1', '🎬', TRUE),
('Shopping', 'Clothing, electronics, household items', '#96CEB4', '🛍️', TRUE),
('Utilities', 'Electricity, water, internet, phone bills', '#FECA57', '⚡', TRUE),
('Healthcare', 'Medical expenses, pharmacy, insurance', '#FF9FF3', '🏥', TRUE),
('Travel', 'Hotels, flights, vacation expenses', '#54A0FF', '✈️', TRUE),
('Education', 'Books, courses, school supplies', '#5F27CD', '📚', TRUE),
('Personal Care', 'Haircuts, cosmetics, gym memberships', '#00D2D3', '💅', TRUE),
('Other', 'Miscellaneous expenses', '#C7ECEE', '📦', TRUE);

-- Insert sample users
INSERT INTO users (username, email, full_name, password_hash, phone_number) VALUES
('alice_smith', 'alice.smith@email.com', 'Alice Smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+1-555-0101'),
('bob_johnson', 'bob.johnson@email.com', 'Bob Johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+1-555-0102'),
('charlie_brown', 'charlie.brown@email.com', 'Charlie Brown', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+1-555-0103'),
('diana_wilson', 'diana.wilson@email.com', 'Diana Wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+1-555-0104'),
('eve_davis', 'eve.davis@email.com', 'Eve Davis', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+1-555-0105');

-- Insert sample expense groups
INSERT INTO expense_groups (group_name, description, created_by) VALUES
('Roommates Apartment 4B', 'Shared expenses for apartment 4B', 1),
('Weekend Trip to Mountains', 'Trip expenses for mountain cabin weekend', 2),
('Office Lunch Group', 'Regular lunch expenses with colleagues', 3),
('Book Club Monthly', 'Monthly book club meeting expenses', 4);

-- Insert group members
INSERT INTO group_members (group_id, user_id, role) VALUES
-- Roommates group
(1, 1, 'ADMIN'),
(1, 2, 'MEMBER'),
(1, 3, 'MEMBER'),
-- Weekend trip group
(2, 2, 'ADMIN'),
(2, 1, 'MEMBER'),
(2, 4, 'MEMBER'),
(2, 5, 'MEMBER'),
-- Office lunch group
(3, 3, 'ADMIN'),
(3, 1, 'MEMBER'),
(3, 2, 'MEMBER'),
-- Book club group
(4, 4, 'ADMIN'),
(4, 3, 'MEMBER'),
(4, 5, 'MEMBER');

-- Insert sample expenses
INSERT INTO expenses (group_id, paid_by, category_id, expense_title, description, total_amount, expense_date, split_type) VALUES
-- Roommates expenses
(1, 1, 5, 'Electricity Bill - March', 'Monthly electricity bill for apartment', 125.50, '2024-03-15', 'EQUAL'),
(1, 2, 1, 'Grocery Shopping', 'Weekly groceries from Whole Foods', 89.75, '2024-03-10', 'EQUAL'),
(1, 3, 5, 'Internet Bill', 'Monthly internet service', 79.99, '2024-03-01', 'EQUAL'),

-- Weekend trip expenses
(2, 2, 7, 'Mountain Cabin Rental', '3-night cabin rental for weekend trip', 480.00, '2024-03-08', 'EQUAL'),
(2, 1, 1, 'Groceries for Trip', 'Food and drinks for the weekend', 156.30, '2024-03-09', 'EQUAL'),
(2, 4, 2, 'Gas for Road Trip', 'Gas for driving to mountains', 85.40, '2024-03-08', 'EQUAL'),
(2, 5, 3, 'Board Games', 'New board games for the trip', 67.95, '2024-03-07', 'EQUAL'),

-- Office lunch expenses
(3, 3, 1, 'Pizza Lunch', 'Team pizza lunch at Tony''s', 45.60, '2024-03-12', 'EQUAL'),
(3, 1, 1, 'Thai Food', 'Thai takeout for lunch', 38.25, '2024-03-14', 'EQUAL'),

-- Book club expenses
(4, 4, 8, 'March Book Purchase', 'Book of the month: "The Silent Patient"', 24.99, '2024-03-01', 'EQUAL'),
(4, 5, 1, 'Coffee and Snacks', 'Refreshments for book club meeting', 31.45, '2024-03-15', 'EQUAL');

-- Insert expense splits (automatically calculated for EQUAL splits)
-- Roommates group expenses (3 people)
INSERT INTO expense_splits (expense_id, user_id, amount_owed) VALUES
-- Electricity bill split
(1, 1, 0.00), -- Paid by user 1, so owes nothing
(1, 2, 41.83),
(1, 3, 41.83),
-- Grocery shopping split  
(2, 1, 29.92),
(2, 2, 0.00), -- Paid by user 2
(2, 3, 29.92),
-- Internet bill split
(3, 1, 26.66),
(3, 2, 26.66),
(3, 3, 0.00); -- Paid by user 3

-- Weekend trip expenses (4 people)
INSERT INTO expense_splits (expense_id, user_id, amount_owed) VALUES
-- Cabin rental split
(4, 1, 120.00),
(4, 2, 0.00), -- Paid by user 2
(4, 4, 120.00),
(4, 5, 120.00),
-- Trip groceries split
(5, 1, 0.00), -- Paid by user 1
(5, 2, 39.08),
(5, 4, 39.08),
(5, 5, 39.08),
-- Gas split
(6, 1, 21.35),
(6, 2, 21.35),
(6, 4, 0.00), -- Paid by user 4
(6, 5, 21.35),
-- Board games split
(7, 1, 16.99),
(7, 2, 16.99),
(7, 4, 16.99),
(7, 5, 0.00); -- Paid by user 5

-- Office lunch expenses (3 people)
INSERT INTO expense_splits (expense_id, user_id, amount_owed) VALUES
-- Pizza lunch split
(8, 1, 15.20),
(8, 2, 15.20),
(8, 3, 0.00), -- Paid by user 3
-- Thai food split
(9, 1, 0.00), -- Paid by user 1
(9, 2, 12.75),
(9, 3, 12.75);

-- Book club expenses (3 people)
INSERT INTO expense_splits (expense_id, user_id, amount_owed) VALUES
-- Book purchase split
(10, 3, 8.33),
(10, 4, 0.00), -- Paid by user 4
(10, 5, 8.33),
-- Coffee and snacks split
(11, 3, 10.48),
(11, 4, 10.48),
(11, 5, 0.00); -- Paid by user 5

-- Insert some sample settlements
INSERT INTO settlements (group_id, payer_id, payee_id, amount, settlement_date, notes, payment_method) VALUES
(1, 2, 1, 41.83, '2024-03-16', 'Venmo payment for electricity bill', 'Venmo'),
(2, 4, 2, 120.00, '2024-03-10', 'Cash payment for cabin rental share', 'Cash'),
(3, 2, 3, 15.20, '2024-03-13', 'PayPal payment for pizza lunch', 'PayPal');

-- Update some expense splits as settled based on settlements
UPDATE expense_splits SET is_settled = TRUE, settled_at = '2024-03-16 10:30:00' 
WHERE expense_id = 1 AND user_id = 2;

UPDATE expense_splits SET is_settled = TRUE, settled_at = '2024-03-10 14:15:00'
WHERE expense_id = 4 AND user_id = 4;

UPDATE expense_splits SET is_settled = TRUE, settled_at = '2024-03-13 18:45:00'
WHERE expense_id = 8 AND user_id = 2;