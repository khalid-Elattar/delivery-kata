-- Delivery Slot Booking System Database Schema

CREATE TABLE IF NOT EXISTS slots (
    id UUID PRIMARY KEY,
    delivery_mode VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity INTEGER NOT NULL,
    booked_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_capacity CHECK (capacity > 0),
    CONSTRAINT chk_booked_count CHECK (booked_count >= 0 AND booked_count <= capacity),
    CONSTRAINT chk_time_order CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_slots_mode_date ON slots(delivery_mode, date);
CREATE INDEX IF NOT EXISTS idx_slots_date ON slots(date);
CREATE INDEX IF NOT EXISTS idx_slots_available ON slots(delivery_mode, date) WHERE booked_count < capacity;

CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY,
    slot_id UUID NOT NULL REFERENCES slots(id),
    customer_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_bookings_slot ON bookings(slot_id);
CREATE INDEX IF NOT EXISTS idx_bookings_customer ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
