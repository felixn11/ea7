package messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 *
 * @author Ronny
 */
public class MessagingGateway {

    private IReceiver receiver;
    private ISender sender;

    public MessagingGateway(String sendQueue, String receiveQueue) throws NamingException, JMSException {
        // connecting to the JMS 
        receiver = MessagingFactory.createReceiver(receiveQueue);
        sender = MessagingFactory.createSender(sendQueue);
    }

    public MessagingGateway(String receiveQueue) throws NamingException, JMSException {
        receiver = MessagingFactory.createReceiver(receiveQueue);
    }

    public boolean sendMessage(Message msg) {
        try {
            sender.sendMessage(msg);
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean sendMessage(Destination destination, Message msg) {
        try {
            sender.sendMessage(msg, destination);
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public TextMessage createMessage(String body) throws JMSException {
        return sender.createMessage(body);
    }

    public void setReceivedMessageListener(MessageListener listener) throws JMSException {
        receiver.setMessageListener(listener);
    }

    public Destination getReceiverDestination() {
        return receiver.getDestination();
    }

    public Destination getSenderDestination() {
        return sender.getDestination();
    }

    public void openConnection() throws JMSException {
        receiver.openConnection();
        if (sender != null) {
            sender.openConnection();
        }
    }
}
