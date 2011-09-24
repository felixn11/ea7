package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;
import messaging.JMSSettings;
import messaging.MessagingGateway;
import bank.BankSerializer;
import javax.jms.TextMessage;

/**
 *
 * @author Ronny
 */
public abstract class BankGateway {

    private MessagingGateway msgGateway;
    private BankSerializer serializer;

    public BankGateway(String requestQueue, String replyQueue) throws NamingException, JMSException {
        serializer = new BankSerializer();
        msgGateway = new MessagingGateway(JMSSettings.BANK_1, JMSSettings.BANK_REPLY);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    abstract void onBankReply(BankQuoteReply reply);

    public void start() {
        msgGateway.openConnection();
    }

    public void getBankQuote(BankQuoteRequest request) throws JMSException {
        msgGateway.sendMessage(msgGateway.createMessage(serializer.requestToString(request)));
    }

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    BankQuoteReply reply = serializer.replyFromString(((TextMessage) message).getText());
                    onBankReply(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
    }
}
