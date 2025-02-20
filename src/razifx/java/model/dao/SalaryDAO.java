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
import razifx.java.model.entity.Salary;

/**
 * SalaryDAO.java
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class SalaryDAO {
    
    @PersistenceContext
    private static EntityManager entityManager;
    
    private static Session session;
       
    private SalaryDAO() {}
    
    public static void openSession() {
        session = HibernateUtil.getSessionFactory().openSession();
        RaziLogger.info("create session instance when class loaded by ClassLoade");
    }
    
    public static void save(Salary salary) {
        try {
            openSession();
            session.beginTransaction();
            session.save(salary); 
            session.getTransaction().commit();
            RaziLogger.info("new salary saved on database.");
        } catch (HibernateException e) {
            RaziLogger.error("SalaryDAO class: ", e);
        } finally {
            closeSession();
        }
    }
    
    public static void update(Salary salary) {
        try {
            openSession();
            session.beginTransaction();
            session.update(salary); 
            session.getTransaction().commit();
            RaziLogger.info("update salary on database.");
        } catch (HibernateException e) {
            RaziLogger.error("SalaryDAO class: ", e);
        } finally {
            closeSession();
        }
    }
    
    public static void delete(Salary salary) {
        
        try {
            openSession();
            session.beginTransaction();
            session.delete(salary); 
            session.getTransaction().commit();
            RaziLogger.info("The salary delete from database.");
        } catch (HibernateException e) {
            RaziLogger.error("SalaryDAO class: ", e);
        } finally {
            closeSession();
        }
    }
    
    public static Salary retreiveByID(Long id) {
        openSession();
        Salary e = session.get(Salary.class, id);
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