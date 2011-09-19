package client;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.JMSSettings;
import messaging.MessagingGateway;

/**
 *
 * @author Ronny
 */
abstract class LoanBrokerGateway {

    private MessagingGateway msgGateway;
    private ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML

    public LoanBrokerGateway(String requestQueue, String replyQueue) throws Exception {

        // create the serializer
        serializer = new ClientSerializer();
        msgGateway = new MessagingGateway(JMSSettings.LOAN_REQUEST, JMSSettings.LOAN_REPLY);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    public void start() {
        msgGateway.openConnection();
    }

    public void applyForLoan(ClientRequest request) {
        try {
            msgGateway.sendMessage(msgGateway.createMessage(serializer.requestToString(request)));
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void loanOfferArrived(ClientReply reply);

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    ClientReply reply = serializer.replyFromString(((TextMessage) message).getText());
                    loanOfferArrived(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
}
