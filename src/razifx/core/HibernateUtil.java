/*
 * The MIT License
 *
 * Copyright 2025 mahdihoseinzade.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package razifx.core;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import razifx.java.model.entity.*;

/**
 * HibernateUtil.java: This class will handle the creation of the SessionFactory object.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;
    
    /**
     * private constructor: nobody can create instance this class
     */
    private HibernateUtil() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                configuration.configure("/razifx/hibernate.cfg.xml");
                
                /** Set Entity classes which have mapped with annotations */
                // SuperUser -> `super_user` table
                configuration.addAnnotatedClass(SuperUser.class);
                // Employee -> `employees` table
                configuration.addAnnotatedClass(Employee.class);
                // Asset -> `assets` table
                configuration.addAnnotatedClass(Asset.class);
                // BankAccount -> `bank_accounts` table
                configuration.addAnnotatedClass(BankAccount.class);
                // ChecksReceived -> `checks` table
                configuration.addAnnotatedClass(ChecksReceived.class);
                // CheckPayee -> `checks` table
                configuration.addAnnotatedClass(CheckPayee.class);
                // Customer -> `customers` table
                configuration.addAnnotatedClass(Customer.class);
                // Expense -> `expenses` table
                configuration.addAnnotatedClass(Expense.class);
                // Jobs -> `jobs` table
                configuration.addAnnotatedClass(Jobs.class);
                // Leave -> `leaves` table
                configuration.addAnnotatedClass(Leave.class);
                // Order -> `orders` table
                configuration.addAnnotatedClass(Order.class);
                // OrderDetail -> `order_details` table
                configuration.addAnnotatedClass(OrderDetail.class);
                // Payment -> `payments` table
                configuration.addAnnotatedClass(Payment.class);
                // Product -> `product` table
                configuration.addAnnotatedClass(Product.class);
                // PurchasePayment -> `purchase_payment` table
                configuration.addAnnotatedClass(PurchasePayment.class);
                // Salary -> `salaries` table
                configuration.addAnnotatedClass(Salary.class);
                // Supplier -> `suppliers` table
                configuration.addAnnotatedClass(Supplier.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Throwable ex) {
                System.err.println("Initial SessionFactory creation failed." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        return sessionFactory;
    }
}
