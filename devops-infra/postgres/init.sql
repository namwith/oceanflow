CREATE DATABASE seafooddb;

\connect seafooddb;

CREATE TABLE product (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  batch_code VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  price NUMERIC(12,2) NOT NULL
);

INSERT INTO product (name, batch_code, quantity, price) VALUES
('Tôm sú', 'BATCH-A1', 120, 250000.00),
('Cá hồi', 'BATCH-B3', 80, 320000.00);
