-- Database initialization script for delivery application
-- Run this script after the application schema is created to add performance indexes

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Slots indexes
CREATE INDEX IF NOT EXISTS idx_slots_mode_date ON slots(delivery_mode, date);
CREATE INDEX IF NOT EXISTS idx_slots_date ON slots(date);
CREATE INDEX IF NOT EXISTS idx_slots_available ON slots(delivery_mode, date) WHERE booked_count < capacity;

-- Bookings indexes
CREATE INDEX IF NOT EXISTS idx_bookings_slot ON bookings(slot_id);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
