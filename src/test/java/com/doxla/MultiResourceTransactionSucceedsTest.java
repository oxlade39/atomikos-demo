package com.doxla;

import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.doxla.testing.hibernate.HibernateSessionTestUtil;
import com.doxla.testing.transaction.AtomikosTransactionManagerTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/spring/*-context.xml"
})
@Transactional
@TransactionConfiguration(
        transactionManager = "jtaTransactionManager",
        defaultRollback = false
)
public class MultiResourceTransactionSucceedsTest extends TransactionCallbackWithoutResult{

    @Autowired private HibernateSessionTestUtil hibernate;
    @Autowired private AtomikosTransactionManagerTestUtil tx;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private TestListener testListener;

    private static final String Q_NAME = "q.name";
    private Integer random = new Random().nextInt();

    @Before
    public void registerAtomikosShutdown(){
        tx.registerShutdownHook();
    }

    @Test
    public void testJmsTransactions() throws Exception {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        jmsTemplate.convertAndSend(Q_NAME, random);
    }

    @AfterTransaction
    public void verifyMessageReceived() throws Exception {
        transactionTemplate.execute(this);
    }

    protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(random, testListener.getInteger());

        List<Domain> list = hibernate.allOf(Domain.class);
        assertEquals(1, list.size());

        hibernate.deleteAllEntities(Domain.class);
    }
}
