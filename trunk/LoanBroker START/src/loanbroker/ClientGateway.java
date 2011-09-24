package loanbroker;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
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
public abstract class ClientGateway {

    private MessagingGateway msgGateway;
    private ClientSerializer serializer;

    public ClientGateway(String requestQueue) throws NamingException, JMSException {
        serializer = new ClientSerializer();
        msgGateway = new MessagingGateway(JMSSettings.LOAN_REPLY, JMSSettings.LOAN_REQUEST);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    abstract void onClientRequest(ClientRequest request);

    public void start() {
        msgGateway.openConnection();
    }

    public void offerLoan(ClientReply reply) throws JMSException {
        msgGateway.sendMessage(msgGateway.createMessage(serializer.replyToString(reply)));
    }

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    ClientRequest request = serializer.requestFromString(((TextMessage) message).getText());
                    onClientRequest(request);
                } catch (JMSException ex) {
                    Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
    }
}
