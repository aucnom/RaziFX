-- MySQL 8.0
--
-- Host: Real server    Database: RaziFX2
-- ------------------------------------------------------
-- Version	2.0


--
-- Database structure for Database `RaziFX`
--
show databases;
DROP DATABASE IF EXISTS `razifx2`;
-- Create the RaziFX DB Which uses the UTF-8 character set (which includes the Persian language)
CREATE DATABASE `razifx2` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE razifx2;

SET FOREIGN_KEY_CHECKS = 0; 

--
-- Table structure for table `users`
-- This table is for managing users and their information.
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL, 
    `email` VARCHAR(255) UNIQUE,
    `first_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `last_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `company_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `login_logs`
-- This table is created for save user logs (Time & Public IP).
--

DROP TABLE IF EXISTS `login_logs`;
CREATE TABLE `login_logs` (
    `log_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255),
    `login_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `ip_address` VARCHAR(255)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `delete_account_requests`
-- submit request for delete account
--

DROP TABLE IF EXISTS `delete_account_requests`;
CREATE TABLE `delete_account_requests` (
    `request_id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT,
    `request_datetime` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `employees`
-- This table is created to store employee information.
--

DROP TABLE IF EXISTS `employees`;
CREATE TABLE `employees` (
    `employee_id` INT AUTO_INCREMENT PRIMARY KEY,
    `job_id` INT,
    `first_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `last_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `national_id` VARCHAR(255) NOT NULL,
    `birthdate` DATE,
    `phone_number` VARCHAR(255),
    `address` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `gender` ENUM('MALE', 'FEMALE'),
    `user_id` INT,
     FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
     FOREIGN KEY (`job_id`) REFERENCES `jobs` (`job_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `jobs`
-- This table is created to store job information including base salary and title for employee registration.
--

DROP TABLE IF EXISTS `jobs`;
CREATE TABLE `jobs` (
    `job_id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `base_salary` DECIMAL(13, 0),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `salaries`
-- This table is created to store employee salary information.
--

DROP TABLE IF EXISTS `salaries`;
CREATE TABLE `salaries` (
    `salary_id` INT AUTO_INCREMENT PRIMARY KEY,
    `employee_id` INT,
    `amount` DECIMAL(13, 0),
    `pay_date` DATE,
    `user_id` INT,
    `transaction_id` INT,
    FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `leaves`
-- This table is created to store employee leave information.
--

DROP TABLE IF EXISTS `leaves`;
CREATE TABLE `leaves` (
    `leave_id` INT AUTO_INCREMENT PRIMARY KEY,
    `employee_id` INT,
    `start_date` DATE,
    `end_date` DATE,
    `leave_type` ENUM('SICK', 'CASUAL', 'OTHER'),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `customers`
-- This table is built to manage customers and store their information.
--

DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
    `customer_id` INT AUTO_INCREMENT PRIMARY KEY,
    `first_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `last_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `national_id` VARCHAR(255) NOT NULL,
    `birthdate` DATE,
    `phone_number` VARCHAR(255),
    `address` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `gender` ENUM('MALE', 'FEMALE'),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `products`
-- This table is built to store product information and manages sales and orders.
--

DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
    `product_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `price` DECIMAL(13, 0),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `orders`
-- This table tracks orders placed by customers.
--

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `order_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT,
    `order_date` DATETIME,
    `status` ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `order_details`
-- This table acts as a shopping cart for customers and manages the final order amount.
--

DROP TABLE IF EXISTS `order_details`;
CREATE TABLE `order_details` (
    `order_detail_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT,
    `product_id` INT,
    `quantity` INT,
    `unit_price` DECIMAL(13, 0),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
    FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `payments`
-- Customer payment information is stored in this table.
--

DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT,
    `payment_date` DATETIME,
    `amount` DECIMAL(13, 0),
    `discount` DECIMAL(13, 0),
    `payment_method` ENUM('CREDIT_CARD', 'DEBIT_CARD', 'CASH', 'BANK_TRANSFER'),
    `user_id` INT,
    `transaction_id` INT,
    FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `checks_received`
-- Information about received checks is stored in this table.
--

DROP TABLE IF EXISTS `checks_received`;
CREATE TABLE `checks_received` (
    `check_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT,
    `check_number` VARCHAR(255) NOT NULL,
    `check_date` DATE,
    `amount` DECIMAL(13, 0),
    `deposit_date` DATE,
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `checks_payee`
-- Information about paid checks is stored in this table.
--

DROP TABLE IF EXISTS `checks_payee`;
CREATE TABLE `checks_payee` (
    `check_id` INT AUTO_INCREMENT PRIMARY KEY,
    `check_payee` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `check_number` VARCHAR(255) NOT NULL,
    `check_date` DATE,
    `amount` DECIMAL(13, 0),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `suppliers`
-- Supplier information is stored in this table.
--

DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers` (
    `supplier_id` INT AUTO_INCREMENT PRIMARY KEY,
    `first_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `last_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `national_id` VARCHAR(255) NOT NULL,
    `birthdate` DATE,
    `phone_number` VARCHAR(255),
    `address` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `gender` ENUM('MALE', 'FEMALE'),
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `purchase_payments`
-- Purchase information for the workshop or business is stored in this table.
--

DROP TABLE IF EXISTS `purchase_payments`;
CREATE TABLE `purchase_payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `supplier_id` INT,
    `payment_date` DATETIME,
    `amount` DECIMAL(13, 0),
    `discount` DECIMAL(13, 0),
    `payment_method` ENUM('CASH', 'CHECK', 'BANK_TRANSFER'),
    `user_id` INT,
    `transaction_id` INT,
    FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `expenses`
-- Cost information is stored in this table.
--

DROP TABLE IF EXISTS `expenses`;
CREATE TABLE `expenses` (
    `expense_id` INT AUTO_INCREMENT PRIMARY KEY,
    `expense_date` DATETIME,
    `amount` DECIMAL(13, 0),
    `expense_type` ENUM('REPAIRS', 'BILLS', 'OTHER'),
    `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `user_id` INT,
    `transaction_id` INT,
    FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `bank_accounts`
-- Bank account information is stored in this table.
--

DROP TABLE IF EXISTS `bank_accounts`;
CREATE TABLE `bank_accounts` (
    `bank_account_id` INT AUTO_INCREMENT PRIMARY KEY,
    `bank_name` VARCHAR(99) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `account_number` VARCHAR(50) NOT NULL,
    `card_number` VARCHAR(20),
    `account_holder_name` VARCHAR(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `balance` DECIMAL(13, 0) DEFAULT 0.00,
    `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
 

--
-- Table structure for table `assets`
-- Asset information is stored in this table.
--

DROP TABLE IF EXISTS `assets`;
CREATE TABLE `assets` (
    `asset_id` INT AUTO_INCREMENT PRIMARY KEY,
    `asset_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `asset_type` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `purchase_date` DATE NOT NULL,
    `purchase_price` DECIMAL(13, 0) NOT NULL,
    `storage_location` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    `status` ENUM('Active', 'Inactive', 'Depreciated', 'Disposed') DEFAULT 'Active',
    `user_id` INT,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `transactions`
-- transactions information is stored in this table.
--

DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
    `transaction_id` INT PRIMARY KEY AUTO_INCREMENT,
    `transaction_date` DATE NOT NULL,
    `transaction_type` ENUM('salary', 'expense', 'payment', 'purchase_payment'),
    `amount` DECIMAL(13, 0) NOT NULL,
    `user_id` INT,
    `account_id` INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    FOREIGN KEY (`account_id`) REFERENCES `bank_accounts`(`bank_account_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


show tables;

SET FOREIGN_KEY_CHECKS = 1;

--
-- mahdihoseinzade.jk@yahoo.com
-- Copyright (c) 2025 Mahdi HOSEIN ZADE.
--