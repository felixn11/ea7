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
public class LoanBrokerGateway {

    private boolean debug_mode;
    private BankSerializer serializer; // serializer BankQuoteRequest BankQuoteReply to/from XML:
    /**
     * attributes for connection to JMS
     */
    private Connection connection; // connection to the JMS server
    protected Session session; // JMS session for creating producers, consumers and messages
    private MessageProducer producer; // producer for sending messages
    private MessageConsumer consumer; // consumer for receiving messages
    private BankFrame frame; // GUI
    private String name;
    private Bank bank;

    public LoanBrokerGateway(final Bank bank, String bankName, String bankRequestQueue, String bankReplyQueue, boolean debug_mode) throws Exception {
        this.bank = bank;
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

        // create GUI
        this.name = bankName;
        frame = new BankFrame(bankName, debug_mode) {

            @Override
            public boolean sendBankReply(BankQuoteRequest request, double interest, int error) {
                String quoteID = name + "-" + String.valueOf(bank.quoteCounter++);
                BankQuoteReply reply = new BankQuoteReply(interest, quoteID, error);
                return sendReply(request, reply);
            }
        };
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }

    /**
     * Sends the reply for one request.
     * @param request for which the reply is sent
     * @param reply
     * @return true if the reply is successfully sent, false if sending fails
     */
    private boolean sendReply(BankQuoteRequest request, BankQuoteReply reply) {
        try {
            Message replyMessage = session.createTextMessage(serializer.replyToString(reply));
            producer.send(replyMessage);
            frame.addReply(request, reply);
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Processes a new request message. Only if the debug_mode is true, this method
     * randomly generates a reply and sends it back.
     * @param message
     */
    private void onBankQuoteRequest(TextMessage message) {
        try {
            BankQuoteRequest request = serializer.requestFromString(message.getText());
            frame.addRequest(request);
            if (debug_mode) { // only in debug mode send immediately random reply

                BankQuoteReply reply = bank.computeReply(request);
                sendReply(request, reply);

            }
        } catch (Exception ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receivedQuoteRequest(BankQuoteRequest request) {
    }

    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendQuoteOffer(BankQuoteRequest request, BankQuoteReply reply) {
    }
}
