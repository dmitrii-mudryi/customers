-- Create the orders table
CREATE TABLE IF NOT EXISTS orders (
                        order_number BIGINT AUTO_INCREMENT PRIMARY KEY,
                        first_name VARCHAR(255) NOT NULL,
                        last_name VARCHAR(255) NOT NULL,
                        telephone_number VARCHAR(10) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        delivery_address VARCHAR(255) NOT NULL,
                        number_of_customers INT NOT NULL,
                        order_total DECIMAL(10, 2),
                        order_date TIMESTAMP
);