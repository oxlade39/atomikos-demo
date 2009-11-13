package com.doxla;

import com.doxla.testing.hibernate.HibernateSessionTestUtil;
import com.doxla.testing.transaction.AtomikosTransactionManagerTestUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.TransactionStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/spring/*-context.xml"
})
@Transactional
@TransactionConfiguration(
        transactionManager = "jtaTransactionManager",
        defaultRollback = false
)
public class HibernateTransactionSucceedsTest extends TransactionCallbackWithoutResult {

    @Autowired private HibernateSessionTestUtil hibernate;
    @Autowired private AtomikosTransactionManagerTestUtil tx;
    @Autowired private TransactionTemplate transactionTemplate;

    @Before
    public void cleanDataBase(){
        tx.registerShutdownHook();
        hibernate.deleteAllEntities(Domain.class);
    }

    @Test
    public void testSave(){
        Domain domain = new Domain("123456789");
        hibernate.save(domain);

        Domain loaded = hibernate.get(Domain.class, domain.getId());
        assertNotNull(loaded);
        assertEquals(1, hibernate.allOf(Domain.class).size());

    }

    @AfterTransaction
    public void ensureTransactionPropagated(){
        transactionTemplate.execute(this);
    }

    protected void doInTransactionWithoutResult(TransactionStatus status) {
        assertEquals(1, hibernate.allOf(Domain.class).size());
        cleanDataBase();
    }
}