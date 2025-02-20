-- MySQL 8.0
--
-- Host: Real server    Database: RaziFX2
-- ------------------------------------------------------
-- Version	2.0


--
-- PROCEDURES structure for Database `RaziFX`
--
USE razifx2;
-- 
-- Creating PROCEDURES

-- 1
DELIMITER $$
CREATE PROCEDURE calculate_leave_days (
    IN employee_id_param INT
)
BEGIN
    SELECT employee_id, SUM(DATEDIFF(end_date, start_date) + 1) AS total_leave_days
    FROM Leaves WHERE employee_id = employee_id_param
    GROUP BY employee_id;
END $$
DELIMITER ;
--
--
-- 2
DELIMITER $$
CREATE PROCEDURE calculate_total_expenses(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT SUM(amount) AS total_expenses FROM expenses WHERE expense_date BETWEEN startDate AND endDate AND user_id=user_id_param;
END $$
DELIMITER ;
--
-- 
-- 3
DELIMITER $$
CREATE PROCEDURE calculate_total_salaries(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT SUM(amount) AS total_salaries FROM salaries WHERE pay_date BETWEEN startDate AND endDate AND user_id=user_id_param;
END $$
DELIMITER ;
--
--
-- 4
DELIMITER $$
CREATE PROCEDURE calculate_total_purchase_payments(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT SUM(amount) AS total_purchase_payments FROM purchase_payments WHERE payment_date BETWEEN startDate AND endDate AND user_id=user_id_param;
END $$
DELIMITER ;
--
--
-- 5
DELIMITER $$
CREATE PROCEDURE calculate_total_costs(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT (SELECT SUM(amount) FROM expenses WHERE expense_date BETWEEN startDate AND endDate AND user_id=user_id_param) + (SELECT SUM(amount) AS total_payments FROM purchase_payments WHERE payment_date BETWEEN startDate AND endDate AND user_id=user_id_param) + (SELECT SUM(amount) AS total_salaries FROM salaries WHERE pay_date BETWEEN startDate AND endDate AND user_id=user_id_param) AS total_costs;
END $$
DELIMITER ;
--
-- 
-- 6
DELIMITER $$
CREATE PROCEDURE calculate_total_income(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT SUM(order_details.quantity * order_details.unit_price) AS total_amount FROM orders JOIN order_details ON orders.order_id = order_details.order_id WHERE orders.order_date BETWEEN startDate AND endDate AND orders.user_id = user_id_param AND order_details.user_id = user_id_param;
END $$
DELIMITER ;
--
--
-- 7
DELIMITER $$
CREATE PROCEDURE calculate_total_payments(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT SUM(amount) AS total_payments FROM payments WHERE payment_date BETWEEN startDate AND endDate AND user_id=user_id_param;
END $$
DELIMITER ;
--
--
-- 8
DELIMITER $$
CREATE PROCEDURE calculate_Unpaid(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT (SELECT SUM(order_details.quantity * order_details.unit_price) FROM orders JOIN order_details ON orders.order_id = order_details.order_id WHERE orders.order_date BETWEEN startDate AND endDate AND orders.user_id = user_id_param AND order_details.user_id = user_id_param) - (SELECT SUM(amount) FROM payments WHERE payment_date BETWEEN startDate AND endDate AND user_id=user_id_param) AS Unpaid;
END $$
DELIMITER ;
--
--
-- 9
DELIMITER $$
CREATE PROCEDURE calculate_net_income(
    IN user_id_param INT,
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
   SELECT (SELECT (SELECT SUM(order_details.quantity * order_details.unit_price) FROM orders JOIN order_details ON orders.order_id = order_details.order_id WHERE orders.order_date BETWEEN startDate AND endDate AND orders.user_id = user_id_param AND order_details.user_id = user_id_param) + (SELECT (SELECT SUM(order_details.quantity * order_details.unit_price) FROM order_details WHERE user_id=user_id_param) - (SELECT SUM(amount) FROM payments WHERE user_id=user_id_param))) - (SELECT (SELECT SUM(amount) FROM expenses WHERE user_id=user_id_param) + (SELECT SUM(amount) FROM salaries WHERE pay_date BETWEEN startDate AND endDate AND user_id=user_id_param)) AS net_income;
END $$
DELIMITER ;
--
--
-- 10
DELIMITER $$
CREATE EVENT monthly_logs_cleanup
ON SCHEDULE EVERY 1 MONTH
STARTS DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY) -- Start on the first day of the next month
DO
BEGIN
    TRUNCATE TABLE login_logs;
END $$
DELIMITER ;
--

--
-- Delete Account Steps
--
DELIMITER $$
CREATE PROCEDURE delete_account(IN user_id_to_delete INT)
BEGIN
    -- Disable foreign key checks for faster deletion (Important!)
    SET FOREIGN_KEY_CHECKS = 0;

    -- Delete data from related tables
    DELETE FROM transactions WHERE user_id = user_id_to_delete;
    DELETE FROM assets WHERE user_id = user_id_to_delete;
    DELETE FROM bank_accounts WHERE user_id = user_id_to_delete;
    DELETE FROM expenses WHERE user_id = user_id_to_delete;
    DELETE FROM purchase_payments WHERE user_id = user_id_to_delete;
    DELETE FROM suppliers WHERE user_id = user_id_to_delete;
    DELETE FROM checks_payee WHERE user_id = user_id_to_delete;
    DELETE FROM checks_received WHERE user_id = user_id_to_delete;
    DELETE FROM payments WHERE user_id = user_id_to_delete;
    DELETE FROM order_details WHERE user_id = user_id_to_delete;
    DELETE FROM orders WHERE user_id = user_id_to_delete;
    DELETE FROM products WHERE user_id = user_id_to_delete;
    DELETE FROM customers WHERE user_id = user_id_to_delete;
    DELETE FROM leaves WHERE user_id = user_id_to_delete;
    DELETE FROM salaries WHERE user_id = user_id_to_delete; 
    DELETE FROM jobs WHERE user_id = user_id_to_delete; 
    DELETE FROM employees WHERE user_id = user_id_to_delete;
    -- Finally, delete the user from the users table
    DELETE FROM users WHERE user_id = user_id_to_delete;
    -- Re-enable foreign key checks
    SET FOREIGN_KEY_CHECKS = 1;
END $$
DELIMITER ;
--

-- Example of calling PROCEDURES
CALL calculate_leave_days(1);
--


-- Example of access privilage
GRANT EXECUTE ON PROCEDURE razifx2.calculate_leave_days TO 'razif2x_client'@'%' ;
flush privileges;
--

-- Show all PROCEDURES
SHOW PROCEDURE STATUS WHERE db = 'razifx2'; 

--
-- mahdihoseinzade.jk@yahoo.com
-- Copyright (c) 2025 Mahdi HOSEIN ZADE.
--