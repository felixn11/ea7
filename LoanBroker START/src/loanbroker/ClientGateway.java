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

    public ClientGateway() throws NamingException, JMSException {
        serializer = new ClientSerializer();
        msgGateway = new MessagingGateway(JMSSettings.LOAN_REQUEST, JMSSettings.LOAN_REPLY);
        msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    abstract void onClientReply(ClientReply reply);

    public void receivedLoanRequest(ClientRequest request) {
    }

    private void start() {
        msgGateway.openConnection();
    }

    public void offerLoan(ClientRequest request, ClientReply reply) throws JMSException {
        msgGateway.sendMessage(msgGateway.createMessage(request.toString()));
    }

    private MessageListener getNewMessageListener() {
        return new MessageListener() {

            public void onMessage(Message message) {
                try {
                    TextMessage msg = (TextMessage) message;
                    ClientReply reply = serializer.replyFromString(msg.getText());
                    onClientReply(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
    }
}
