package loanbroker;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import messaging.JMSSettings;
import messaging.MessagingGateway;

/**
 *
 * @author Ronny
 */
public abstract class CreditGateway {

    private MessagingGateway msgGateway;
    private CreditSerializer serializer;

    public CreditGateway() throws NamingException, JMSException {
        serializer = new CreditSerializer();
        msgGateway = new MessagingGateway(JMSSettings.CREDIT_REQUEST, JMSSettings.CREDIT_REPLY);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    abstract void onCreditReply(CreditReply reply);

    private void start() {
        msgGateway.openConnection();
    }

    public void getCreditHistory(CreditRequest request) throws JMSException {
        msgGateway.sendMessage(msgGateway.createMessage(serializer.requestToString(request)));
    }

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    CreditReply reply = serializer.replyFromString(((TextMessage) message).getText());
                    onCreditReply(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
    }
}
