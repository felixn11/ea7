package bank;

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
    private BankSerializer serializer; // serializer BankQuoteRequest BankQuoteReply to/from XML:

    public LoanBrokerGateway(String bankRequestQueue, String bankReplyQueue) throws Exception {

        // create the serializer
        serializer = new BankSerializer();
        msgGateway = new MessagingGateway(JMSSettings.BANK_REPLY, JMSSettings.BANK_1);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    public void start() {
        msgGateway.openConnection();
    }

    public boolean sendBankReply(BankQuoteReply reply) {
        try {
            msgGateway.sendMessage(msgGateway.createMessage(serializer.replyToString(reply)));
            return true;
        } catch (JMSException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    abstract void receivedQuoteRequest(BankQuoteRequest request);

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    BankQuoteRequest request = serializer.requestFromString(((TextMessage) message).getText());
                    receivedQuoteRequest(request);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
}
