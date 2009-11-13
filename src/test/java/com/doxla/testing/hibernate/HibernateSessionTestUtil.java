package com.doxla.testing.hibernate;

import com.doxla.Domain;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Utility class to simplify some common uses of hibernate in test cases.
 */
public final class HibernateSessionTestUtil {

    private final SessionFactory sessionFactory;

    public HibernateSessionTestUtil(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return get the current hibernate session
     */
    public Session session(){
        return sessionFactory.getCurrentSession();
    }

    /**
     * remove all persistent entities of type clazz
     * @param clazz type of entity to remove
     */
    public void deleteAllEntities(Class<?> clazz){
        for (Object entity : allOf(clazz)) {
            session().delete(entity);
        }
    }

    /**
     *
     * @param domainClass type of the persistent entity
     * @param <T>
     * @return Return all peristent entities of type domainClass
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> allOf(Class<T> domainClass) {
        return session().createCriteria(domainClass).list();
    }

    public Serializable save(Domain object) {
        return session().save(object);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> domainClass, Serializable id) {
        return (T) session().get(domainClass, id);
    }
}
