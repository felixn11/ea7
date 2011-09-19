package creditbureau;

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
import messaging.JMSSettings;

/**
 *
 * @author Ronny
 */
abstract class LoanBrokerGateway {

    /**
     * attributes for connection to JMS
     */
    private Connection connection; // connection to the JMS server
    protected Session session; // JMS session fro creating producers, consumers and messages
    private MessageProducer producer; // producer for sending messages
    private MessageConsumer consumer; // consumer for receiving messages
    private CreditSerializer serializer; // serializer CreditRequest CreditReply to/from XML:

    public LoanBrokerGateway(String creditRequestQueue, String creditReplyQueue) throws Exception {
        // connect to JMS
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // connect to the sender channel
        Destination senderDestination = (Destination) jndiContext.lookup(creditReplyQueue);
        producer = session.createProducer(senderDestination);

        // connect to the receiver channel and register as a listener on it
        Destination receiverDestination = (Destination) jndiContext.lookup(creditRequestQueue);
        consumer = session.createConsumer(receiverDestination);
        consumer.setMessageListener(new MessageListener() {

            public void onMessage(Message msg) {
                onCreditRequest((TextMessage) msg);
            }
        });
        // create the serializer
        serializer = new CreditSerializer();
    }

    /**
     * Processes a new request message by randomly generating a reply and sending it back.
     * @param message the credit request message
     */
    private void onCreditRequest(TextMessage message) {
        try {
            CreditRequest request = serializer.requestFromString(message.getText());
            receivedCreditRequest(request);
        } catch (Exception ex) {
            Logger.getLogger(CreditBureau.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(CreditBureau.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void receivedCreditRequest(CreditRequest request);

    public void sendCreditHistory(CreditRequest request, CreditReply reply) {
        try {
            Message replyMessage = session.createTextMessage(serializer.replyToString(reply));
            producer.send(replyMessage);
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
