package bank;

import bank.gui.BankFrame;
import java.text.DecimalFormat;
import java.util.Random;
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
 * This class represents one Bank Application.
 * It sould eventually:
 *  1. Receive BankQuoteRequest-s for a loan from the LoanBroker Messaging-Orianted Middleware (MOM).
 *  2. Randomly create BankQuoteReply for each request (use method "computeBankReply").
 *  3. Send the BankQuoteReply from the LoanBroker MOM.
 */
public class Bank {

    private static final int ERROR_CODE = 1;
    private static final int NO_ERROR_CODE = 0;
    private final double primeRate = 3.5;
    private String name;
    private double ratePremium = 0.5;
    private int maxLoanTerm = 10000;
    private int quoteCounter = 0;
    protected Random random = new Random();
    private BankFrame frame; // GUI
    
    private boolean debug_mode;

    private BankSerializer serializer; // serializer BankQuoteRequest BankQuoteReply to/from XML:
    /**
     * attributes for connection to JMS
     */
    private Connection connection; // connection to the JMS server
    protected Session session; // JMS session for creating producers, consumers and messages
    private MessageProducer producer; // producer for sending messages
    private MessageConsumer consumer; // consumer for receiving messages

    public Bank(String bankName, String bankRequestQueue, String bankReplyQueue, boolean debug_mode) throws Exception {
        super();
        this.name = bankName;
        this.debug_mode = debug_mode;
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
        frame = new BankFrame(name, debug_mode) {

            @Override
            public boolean sendBankReply(BankQuoteRequest request, double interest, int error) {
                String quoteID = name + "-" + String.valueOf(quoteCounter);
                quoteCounter++;
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
     * Processes a new request message. Only if the debug_mode is true, this method
     * randomly generates a reply and sends it back.
     * @param message
     */
    private void onBankQuoteRequest(TextMessage message) {
        try {
            BankQuoteRequest request = serializer.requestFromString(message.getText());
            frame.addRequest(request);
            if (debug_mode) { // only in debug mode send immediately random reply

                BankQuoteReply reply = computeReply(request);
                Bank.this.sendReply(request, reply);

            }
        } catch (Exception ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
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
     * Randomly generates a reply for the given request.
     * @param request for which the reply should be generaged.
     * @return randomly generated reply
     */
    private BankQuoteReply computeReply(BankQuoteRequest request) {

        double interest = 0.0;
        int error = ERROR_CODE;
        if (request.getTime() <= maxLoanTerm) {
            Double interestTemp = new Double(primeRate + ratePremium + (double) (request.getTime() / 12) / 10 + (double) random.nextInt(10) / 10);
            DecimalFormat niceFormat = new DecimalFormat("#.#"); // round to two decimals
            interest = Double.valueOf(niceFormat.format(interestTemp));

            error = NO_ERROR_CODE;
        }
        String quoteID = name + "-" + String.valueOf(quoteCounter);
        quoteCounter++;
        BankQuoteReply replyStruct = new BankQuoteReply(interest, quoteID, error);
        return replyStruct;

    }
    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
