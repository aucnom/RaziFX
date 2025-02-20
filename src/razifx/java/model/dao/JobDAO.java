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
import razifx.java.model.entity.Jobs;

/**
 * JobDAO.java
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class JobDAO {
    
    @PersistenceContext
    private static EntityManager entityManager;
    
    private static Session session;
       
    private JobDAO() {}
    
    public static void openSession() {
        session = HibernateUtil.getSessionFactory().openSession();
        RaziLogger.info("create session instance");
    }
    
    public static void save(Jobs job) {
        
        try {
            openSession();
            session.beginTransaction();
            session.save(job); 
            session.getTransaction().commit();
            RaziLogger.info("new job saved on database.");
        } catch (HibernateException e) {
            RaziLogger.error("JobDAO class: ", e);
        }finally {
            closeSession();
        }
       
    }
    
    public static void update(Jobs job) {
       
        try {
            openSession();
            session.beginTransaction();
            session.update(job); 
            session.getTransaction().commit();
            RaziLogger.info("update job on database.");
        } catch (HibernateException e) {
            RaziLogger.error("JobDAO class: ", e);
        }finally {
            closeSession();
        }
    }
    
    public static void delete(Jobs job) {
        openSession();
        try {
            session.beginTransaction();
            session.delete(job); 
            session.getTransaction().commit();
            RaziLogger.info("The job delete from database.");
        } catch (HibernateException e) {
            RaziLogger.error("JobDAO class: ", e);
        }finally {
            closeSession();
        }
    }
    
    public static Jobs getJobById(Long id) {
        openSession();
        Jobs j =  session.get(Jobs.class, id);
        closeSession();
        return j;
    }
    
    public static void closeSession() {
        if(session != null) {
            session.close();
            session = null;
            RaziLogger.info("Session closed.");
        }
    }
}