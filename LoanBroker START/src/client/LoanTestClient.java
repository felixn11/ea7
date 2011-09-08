package client;

import client.gui.ClientFrame;
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
 * This class represents one Clinet Application.
 * It:
 *  1. Creates a ClientRequest for a loan.
 *  2. Sends it to the LoanBroker Messaging-Orianted Middleware (MOM).
 *  3. Receives the reply from the LoanBroker MOM.
 * 
 */
public class LoanTestClient {

    /*
     * Connection to JMS
     */
    private Connection connection; // to connect to the JMS
    protected Session session; // session for making messages, producers and consumers

    private MessageProducer producer; // for sending messages
    private MessageConsumer consumer; // for receiving messages

    private ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML
    
    private ClientFrame frame; // GUI

    public LoanTestClient(String name, String requestQueue, String replyQueue) throws Exception {
        super();
        // connecting to the JMS
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // connect to the sender channel
        Destination senderDestination = (Destination) jndiContext.lookup(requestQueue);
        producer = session.createProducer(senderDestination);
        // connect to the receiver channel
        Destination receiverDestination = (Destination) jndiContext.lookup(replyQueue);
        consumer = session.createConsumer(receiverDestination);
        consumer.setMessageListener(new MessageListener() {

            public void onMessage(Message message) {
                processLoanOffer((TextMessage) message);
            }
        });
         // create the serializer
        serializer = new ClientSerializer();
         // create the GUI
        frame = new ClientFrame(name) {

            @Override
            public void send(ClientRequest request) {
                sendRequest(request);
            }
        };

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }
    /**
     * Sends new loan request to the LoanBroker.
     * @param request
     */
    public void sendRequest(ClientRequest request) {
        try {
            producer.send(session.createTextMessage(serializer.requestToString(request)));
            frame.addRequest(request);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * This message is called whenever a new client reply message arrives.
     *  The message is de-serialized into a ClientReply, and the reply is shown in the GUI.
     * @param message
     */
    private void processLoanOffer(TextMessage message) {
        try {
            ClientReply reply = serializer.replyFromString(((TextMessage) message).getText());
            frame.addReply(null, reply);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}