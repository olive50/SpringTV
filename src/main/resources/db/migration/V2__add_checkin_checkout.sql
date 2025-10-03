-- Add checkin/checkout status to Guest
ALTER TABLE guests ADD COLUMN checkin_status VARCHAR(20) DEFAULT 'RESERVED';
ALTER TABLE guests ADD COLUMN checkin_time TIMESTAMP;
ALTER TABLE guests ADD COLUMN checkout_time TIMESTAMP;

-- Add room reference to Guest (already exists)
-- Ensure we have index
CREATE INDEX idx_guest_room ON guests(current_room_id);

-- Optional: Add checkin audit table for history
CREATE TABLE checkin_history (
                                 id BIGSERIAL PRIMARY KEY,
                                 guest_id BIGINT REFERENCES guests(id),
                                 room_id BIGINT REFERENCES rooms(id),
                                 action VARCHAR(20) NOT NULL, -- CHECK_IN, CHECK_OUT
                                 performed_by BIGINT REFERENCES users(id),
                                 performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 notes TEXT
);

CREATE INDEX idx_checkin_history_guest ON checkin_history(guest_id);
CREATE INDEX idx_checkin_history_room ON checkin_history(room_id);
CREATE INDEX idx_checkin_history_date ON checkin_history(performed_at);