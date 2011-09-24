package client;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.JMSSettings;
import messaging.MessagingGateway;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Ronny
 */
abstract class LoanBrokerGateway {

    private AsynchronousRequestor<ClientRequest, ClientReply> requestor;
    //private MessagingGateway msgGateway;
    private ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML

    public LoanBrokerGateway(String requestQueue, String replyQueue) throws Exception {
                // create the serializer
        serializer = new ClientSerializer();
        requestor = new AsynchronousRequestor<ClientRequest, ClientReply>(requestQueue, replyQueue, serializer);
       // msgGateway = new MessagingGateway(JMSSettings.LOAN_REQUEST, JMSSettings.LOAN_REPLY);
       // msgGateway.setReceivedMessageListener(getNewMessageListener());
    }

    public void start() {
        requestor.start();
    }

    public void applyForLoan(ClientRequest request) {
        try {
            requestor.sendRequest(request, new IReplyListener<ClientRequest, ClientReply>() {

                public void onReply(ClientRequest request, ClientReply reply) {
                   loanOfferArrived(reply);
                }
            });
            //msgGateway.sendMessage(msgGateway.createMessage(serializer.requestToString(request)));
        } catch (Exception ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void loanOfferArrived(ClientReply reply);

 /*   private MessageListener getNewMessageListener() {
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
    }*/
}
