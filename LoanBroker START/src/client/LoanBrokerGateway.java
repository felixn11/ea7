package client;

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

    /*
     * Connection to JMS
     */
    private LoanTestClient client;
    private Connection connection; // to connect to the JMS
    protected Session session; // session for making messages, producers and consumers
    private ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML
    private MessageProducer producer; // for sending messages
    private MessageConsumer consumer; // for receiving messages

    public LoanBrokerGateway(LoanTestClient client, String requestQueue, String replyQueue) throws Exception {
        this.client = client;
        // connecting to the JMS
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // create the serializer
        serializer = new ClientSerializer();

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
    }

    public void start() {
        try {
            connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void applyForLoan(ClientRequest request) {
        try {
            //TODO messaginggateway.send
            producer.send(session.createTextMessage(serializer.requestToString(request)));
            client.processRequest(request);
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void loanOfferArrived(ClientReply reply);

    private void processLoanOffer(TextMessage message) {
        try {
            ClientReply reply = serializer.replyFromString(((TextMessage) message).getText());
            loanOfferArrived(reply);
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
