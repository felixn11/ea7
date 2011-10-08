package client;

import java.util.logging.*;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Ronny
 */
abstract class LoanBrokerGateway {

    private ClientSerializer serializer; // for serializing ClientRequest and ClientReply to/from XML
    private AsynchronousRequestor<ClientRequest, ClientReply> msgGateway;

    public LoanBrokerGateway(String requestQueue, String replyQueue) throws Exception {
        // create the serializer
        serializer = new ClientSerializer();
        msgGateway = new AsynchronousRequestor<ClientRequest, ClientReply>(requestQueue, replyQueue, serializer);
    }

    public void start() {
        msgGateway.start();
    }

    public void applyForLoan(ClientRequest request) {
        try {
            msgGateway.sendRequest(request, new IReplyListener<ClientRequest, ClientReply>() {

                public void onReply(ClientRequest request, ClientReply reply) {
                    loanOfferArrived(request, reply);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void loanOfferArrived(ClientRequest request, ClientReply reply);
}
