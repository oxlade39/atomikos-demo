package com.doxla.testing.jms;

import org.springframework.jms.support.JmsUtils;
import org.apache.activemq.ActiveMQXASession;
import org.apache.activemq.TransactionContext;

import javax.jms.*;
import javax.transaction.xa.XAResource;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

public class JmsTestUtil {

    private final ConnectionFactory connectionFactory;

    public JmsTestUtil(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Message waitForMessage(String queueName, TimeUnit unit, long wait) throws JMSException {
        Connection connection = null;
        XASession session = null;
        MessageConsumer messageConsumer = null;
        try{
            System.out.println("******************Creating connection");
            connection = connectionFactory.createConnection();
            connection.start();
            session = (XASession) connection.createSession(true, Session.SESSION_TRANSACTED);
            TransactionContext resource = (TransactionContext) session.getXAResource();
            if(!resource.isInXATransaction()){
                throw new RuntimeException("not in xa");
            }
            messageConsumer = session.createConsumer(session.createQueue(queueName));
            return messageConsumer.receive(unit.toMillis(wait));
        }finally {
            if(session != null){
                JmsUtils.commitIfNecessary(session);
            }
            if(messageConsumer != null){
                JmsUtils.closeMessageConsumer(messageConsumer);
            }
            if(session != null){
                JmsUtils.closeSession(session);
            }
            if(connection != null){
                JmsUtils.closeConnection(connection);
            }
        }
    }

    public Message waitForMessage(String queueName, long wait) throws JMSException {
        return waitForMessage(queueName, TimeUnit.MILLISECONDS, wait);
    }
}
