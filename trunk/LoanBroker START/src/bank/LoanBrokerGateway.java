package bank;

import bank.gui.BankFrame;
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

    private BankSerializer serializer; // serializer BankQuoteRequest BankQuoteReply to/from XML:
    /**
     * attributes for connection to JMS
     */
    private Connection connection; // connection to the JMS server
    protected Session session; // JMS session for creating producers, consumers and messages
    private MessageProducer producer; // producer for sending messages
    private MessageConsumer consumer; // consumer for receiving messages

    public LoanBrokerGateway(String bankRequestQueue, String bankReplyQueue) throws Exception {
        // connect to JMS
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // connect to the sender channel
        Destination senderDestination = (Destination) jndiContext.lookup(bankReplyQueue);
        producer = session.createProducer(senderDestination);

        // connect to the receiver channel and register as a listener on it
        Destination receiverDestination = (Destination) jndiContext.lookup(bankRequestQueue);
        consumer = session.createConsumer(receiverDestination);
        consumer.setMessageListener(new MessageListener() {

            public void onMessage(Message msg) {
                onBankQuoteRequest((TextMessage) msg);
            }
        });

        // create the serializer
        serializer = new BankSerializer();
    }

    /**
     * Processes a new request message. Only if the debug_mode is true, this method
     * randomly generates a reply and sends it back.
     * @param message
     */
    private void onBankQuoteRequest(TextMessage message) {
        try {
            BankQuoteRequest request = serializer.requestFromString(message.getText());
            receivedQuoteRequest(request);
        } catch (Exception ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void receivedQuoteRequest(BankQuoteRequest request);

    public boolean sendBankReply(BankQuoteReply reply) {
        try {
            producer.send(session.createTextMessage(serializer.replyToString(reply)));
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
