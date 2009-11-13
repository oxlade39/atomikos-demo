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
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.JmsException;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.doxla.testing.hibernate.HibernateSessionTestUtil;
import com.doxla.testing.transaction.AtomikosTransactionManagerTestUtil;
import com.doxla.testing.jms.JmsTestUtil;

import javax.jms.JMSException;
import javax.jms.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/spring/*-context.xml"
})
@Transactional
@TransactionConfiguration(
        transactionManager = "jtaTransactionManager",
        defaultRollback = false
)
public class MultiResourceTransactionFailsTest extends TransactionCallbackWithoutResult{

    @Autowired private HibernateSessionTestUtil hibernate;
    @Autowired private AtomikosTransactionManagerTestUtil tx;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private FailingTestListener testListener;
    @Autowired private JmsTestUtil jms;

    private static final String Q_NAME = "failing.q";
    private Integer random = new Random().nextInt();

    @Before
    public void cleanDB(){
        tx.registerShutdownHook();
        hibernate.deleteAllEntities(Domain.class);
    }

    @Test
    public void testJmsTransactions() throws Exception {
        jmsTemplate.convertAndSend(Q_NAME, random);
    }

    @AfterTransaction
    public void verifyMessageNotReceived() throws Exception {
        transactionTemplate.execute(this);
    }

    protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(random, testListener.getInteger());
        try{
            Message message = jms.waitForMessage("new.q", 500);
            assertNull("Message was delivered to queue when transaction should have rolled back",
                    message);
        }catch (JMSException e){
            throw new RuntimeException("Waiting for messaged failed", e);
        }

        List<Domain> list = hibernate.allOf(Domain.class);
        assertEquals(0, list.size());
    }
}