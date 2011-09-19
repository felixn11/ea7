package creditbureau;

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
    private CreditSerializer serializer; // serializer CreditRequest CreditReply to/from XML:

    public LoanBrokerGateway(String creditRequestQueue, String creditReplyQueue) throws Exception {

        // create the serializer
        serializer = new CreditSerializer();
        msgGateway = new MessagingGateway(JMSSettings.CREDIT_REPLY, JMSSettings.CREDIT_REQUEST);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    public void start() {
        msgGateway.openConnection();
    }

    public void sendCreditHistory(CreditRequest request, CreditReply reply) {
        try {
            msgGateway.sendMessage(msgGateway.createMessage(serializer.replyToString(reply)));
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void receivedCreditRequest(CreditRequest request);

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    CreditRequest request = serializer.requestFromString(((TextMessage) message).getText());
                    receivedCreditRequest(request);
                } catch (Exception ex) {
                    Logger.getLogger(CreditBureau.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
}
