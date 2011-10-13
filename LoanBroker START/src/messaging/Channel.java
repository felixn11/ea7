/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Michael
 */
public class Channel implements IChannel {

    protected Connection connection;
    protected Session session;
    protected Destination destination;

    public Channel(String connectionName, String destinationName) throws NamingException, JMSException {
        Context jndiContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        if (destinationName.equals("")) {
            destination = null;
        } else {
            destination = (Destination) jndiContext.lookup(destinationName);
        }
    }

    public Destination getDestination() {
        return destination;
    }

    public void openConnection() throws JMSException {

        connection.start();

    }
}
