/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 *
 * @author Michael
 */
public class MessagingFactory {
    
    private static String connectionName = JMSSettings.CONNECTION;
    
    public static IReceiver createReceiver(String destinationName) throws NamingException, JMSException
    {
        return new ReceiverGateway(connectionName, destinationName);
    }
    
    public static ISender createSender(String destinationName) throws NamingException, JMSException
    {
        return new SenderGateway(connectionName, destinationName);
    }
    
}
