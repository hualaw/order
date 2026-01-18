CREATE TABLE `orders` (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Auto increment id, Order id',
    product_name VARCHAR(255) COMMENT 'Product name',
    customer VARCHAR(255) COMMENT 'Who buy it',
    `total_amount` FLOAT COMMENT 'Total amount of money the order paid',
    currency VARCHAR(10) DEFAULT 'RMB' COMMENT 'Currency,default is RMB',
    status TINYINT COMMENT 'Order status: 1, CREATED; 2, COMPLETED; 3, CANCELED',
    create_time TIMESTAMP COMMENT 'Order creation time',
    update_time TIMESTAMP COMMENT 'Order update time',
    INDEX idx_product_name (product_name),
    INDEX idx_customer (customer),
    INDEX idx_create_time (create_time),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='record orders, provide CRUD functions';


-- 为 Eric 创建 4 条订单记录
INSERT INTO `orders` (product_name, customer, total_amount, currency, status, create_time, update_time) VALUES
('Laptop', 'Eric', 8999.99, 'RMB', 2, '2026-01-02 10:15:30', '2026-01-02 10:15:30'),
('Mouse', 'Eric', 299.50, 'RMB', 1, '2026-01-05 14:20:45', '2026-01-05 14:20:45'),
('Keyboard', 'Eric', 599.00, 'RMB', 2, '2026-01-10 09:05:20', '2026-01-10 09:05:20'),
('Monitor', 'Eric', 2499.00, 'USD', 3, '2026-01-15 16:40:10', '2026-01-15 16:40:10');

-- 为 Howard 创建 5 条订单记录
INSERT INTO `orders` (product_name, customer, total_amount, currency, status, create_time, update_time) VALUES
('Smartphone', 'Howard', 4599.00, 'RMB', 2, '2026-01-03 11:30:15', '2026-01-03 11:30:15'),
('Tablet', 'Howard', 3299.00, 'RMB', 1, '2026-01-06 13:45:25', '2026-01-06 13:45:25'),
('Headphones', 'Howard', 899.00, 'RMB', 2, '2026-01-09 15:20:35', '2026-01-09 15:20:35'),
('Smartwatch', 'Howard', 1999.00, 'RMB', 2, '2026-01-12 17:10:50', '2026-01-12 17:10:50'),
('Printer', 'Howard', 1599.00, 'USD', 3, '2026-01-18 08:55:40', '2026-01-18 08:55:40');

-- 为 Ying 创建 6 条订单记录
INSERT INTO `orders` (product_name, customer, total_amount, currency, status, create_time, update_time) VALUES
('Camera', 'Ying', 6899.00, 'RMB', 2, '2026-01-01 09:10:20', '2026-01-01 09:10:20'),
('Speaker', 'Ying', 1299.00, 'RMB', 1, '2026-01-04 12:25:30', '2026-01-04 12:25:30'),
('Drone', 'Ying', 4599.00, 'RMB', 1, '2026-01-07 14:35:45', '2026-01-07 14:35:45'),
('Gaming Console', 'Ying', 3999.00, 'RMB', 2, '2026-01-11 16:50:15', '2026-01-11 16:50:15'),
('Laptop', 'Ying', 10999.00, 'USD', 2, '2026-01-14 10:15:25', '2026-01-14 10:15:25'),
('Monitor', 'Ying', 3299.00, 'RMB', 3, '2026-01-19 13:40:35', '2026-01-19 13:40:35');

CREATE TABLE `notification` (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Auto increment id, Notification id',
    type VARCHAR(10) COMMENT 'SMS or Email',
    status TINYINT COMMENT 'Notification status: 1, SUCCESS; 2, FAILED',
    createtime TIMESTAMP COMMENT 'Notification creation time',
    updatetime TIMESTAMP COMMENT 'Notification update time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='record notification actions and results';