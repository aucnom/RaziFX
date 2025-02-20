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
package razifx.java.model.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import razifx.core.HibernateUtil;
import razifx.core.RaziLogger;
import razifx.java.model.entity.Employee;

/**
 * EmployeeDAO.java
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class EmployeeDAO {
    
    @PersistenceContext
    private static EntityManager entityManager;
    
    private static Session session;
       
    private EmployeeDAO() {}
    
    public static void openSession() {
        session = HibernateUtil.getSessionFactory().openSession();
        
        RaziLogger.info("create session instance");
    }
    
    public static void save(Employee employee) {
        try {
            openSession();
            session.beginTransaction();
            session.save(employee); 
            session.getTransaction().commit();
            RaziLogger.info("new employee saved on database.");
        } catch (HibernateException e) {
            RaziLogger.error("EmployeeDAO class: ", e);
        } finally {
            closeSession();
        }
    }
    
    public static void update(Employee employee) {
        try {
            openSession();
            session.beginTransaction();
            session.update(employee); 
            session.getTransaction().commit();
            RaziLogger.info("update employee on database.");
        } catch (HibernateException e) {
            RaziLogger.error("EmployeeDAO class: ", e);
        } finally {
            closeSession();
        }
    }
    
    public static void delete(Employee employee) {
        
        try {
            openSession();
            session.beginTransaction();
            session.delete(employee); 
            session.getTransaction().commit();
            RaziLogger.info("The employee delete from database.");
        } catch (HibernateException e) {
            RaziLogger.error("EmployeeDAO class: ", e);
        } finally {
            closeSession();
        }
    }
    
    public static Employee retreiveByID(Long id) {
        openSession();
        Employee e = session.get(Employee.class, id);
        closeSession();
        return e;
    }
    
    public static void closeSession() {
        if(session != null) {
            session.close();
            session = null;
            RaziLogger.info("Session closed.");
        }
    }
}