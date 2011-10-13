/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 *
 * @author Michael
 */
public class SenderGateway extends Channel implements ISender {
    
    private MessageProducer producer;    
    
    public SenderGateway(String connectionName, String destinationName) throws NamingException, JMSException
    {
        super(connectionName, destinationName);       
        producer = session.createProducer(destination);
    }

    public TextMessage createMessage(String body) throws JMSException {
        return session.createTextMessage(body);
    }

    public boolean sendMessage(Message msg) {
        try {
            producer.send(msg);
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(SenderGateway.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean sendMessage(Message msg, Destination dest) {
         try {
            producer.send(dest, msg);
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(SenderGateway.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }    
}
