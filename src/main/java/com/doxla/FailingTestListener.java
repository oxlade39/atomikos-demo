package com.doxla;

import org.hibernate.SessionFactory;
import org.springframework.jms.core.JmsTemplate;

public class FailingTestListener {

    private SessionFactory sessionFactory;
    private Integer integer;
    private JmsTemplate t;

    public FailingTestListener(SessionFactory sessionFactory, JmsTemplate t) {
        this.sessionFactory = sessionFactory;
        this.t = t;
    }

    public Integer getInteger() {
        Integer toReturn = integer;
        this.integer = null;
        return toReturn;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
        sessionFactory.getCurrentSession().save(new Domain(""+integer));
        t.convertAndSend("new.q", "sent-on"+integer);
        throw new RuntimeException("oops");
    }
}