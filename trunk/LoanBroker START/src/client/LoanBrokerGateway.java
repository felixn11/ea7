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

    private AsynchronousRequestor<ClientRequest, ClientReply> msgGateway;
    //private MessagingGateway msgGateway;
    private ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML

    public LoanBrokerGateway(String requestQueue, String replyQueue) throws Exception {
        // create the serializer
        serializer = new ClientSerializer();
        msgGateway = new AsynchronousRequestor<ClientRequest, ClientReply>(requestQueue, replyQueue, serializer);
    }

    public void start() {
        msgGateway.start();
    }

    protected void applyForLoan(ClientRequest request) throws JMSException {
        msgGateway.sendRequest(request, new IReplyListener<ClientRequest, ClientReply>() {

            public void onReply(ClientRequest request, ClientReply reply) {
                System.out.println("The loanbroker received a loan offer from the Creditbank");
                loanOfferArrived(request, reply);
            }
        });
    }

    public abstract void loanOfferArrived(ClientRequest request, ClientReply reply);
}
