-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, first_name, last_name, role, is_active) VALUES
                                                                                          ('admin', 'admin@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Admin', 'User', 'ADMIN', true),
                                                                                          ('manager', 'manager@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Manager', 'User', 'MANAGER', true),
                                                                                          ('receptionist', 'receptionist@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Receptionist', 'User', 'RECEPTIONIST', true),
                                                                                          ('technician', 'technician@tvboot.com', '$2a$10$xDBKoM67grR.QBlMrb6BOOS8z1MtHZfkjE6m3Vl2jXr.HXy7/7Y.O', 'Technician', 'User', 'TECHNICIAN', true);

-- Create indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);