package messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Ronny
 */
public class MessagingGateway {
    
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private Destination destinationSender;
    public Destination destinationReceiver;
    
    public MessagingGateway(String sendQueue, String receiveQueue) throws NamingException, JMSException {
        // connecting to the JMS 
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        destinationSender = (Destination) jndiContext.lookup(sendQueue);
        destinationReceiver = (Destination) jndiContext.lookup(receiveQueue);
        
        producer = session.createProducer(destinationSender);
        consumer = session.createConsumer(destinationReceiver);
    }
    
    public MessagingGateway(String receiveQueue) throws NamingException, JMSException {
        // connecting to the JMS 
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        destinationReceiver = (Destination) jndiContext.lookup(receiveQueue);
        
        producer = session.createProducer(null);
        consumer = session.createConsumer(destinationReceiver);
    }
    
    public boolean sendMessage(Message msg) {
        try {
            producer.send(msg);
            return true;
        } catch (JMSException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public boolean sendMessage(Destination destination, Message msg) {
        try {
            producer.send(destination, msg);
            return true;
        } catch (JMSException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public TextMessage createMessage(String body) throws JMSException {
        return session.createTextMessage(body);
    }
    
    public void setReceivedMessageListener(MessageListener listener) throws JMSException {
        consumer.setMessageListener(listener);
    }
    
    public void openConnection() {
        try {
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
