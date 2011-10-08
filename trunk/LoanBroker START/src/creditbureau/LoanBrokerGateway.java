package creditbureau;

import java.util.logging.*;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;

/**
 *
 * @author Ronny
 */
abstract class LoanBrokerGateway {

    private AsynchronousReplier<CreditRequest, CreditReply> msgGateway;
    private CreditSerializer serializer; // serializer CreditRequest CreditReply to/from XML:

    public LoanBrokerGateway(String creditRequestQueue, String creditReplyQueue) throws Exception {
        // create the serializer
        serializer = new CreditSerializer();
        msgGateway = new AsynchronousReplier<CreditRequest, CreditReply>(creditReplyQueue, serializer);
        msgGateway.setRequestListener(new IRequestListener<CreditRequest>() {

            public void receivedRequest(CreditRequest request) {
                receivedCreditRequest(request);
            }
        });
    }

    public void start() {
        msgGateway.start();
    }

    protected void sendCreditHistory(CreditRequest request, CreditReply reply){
        try {
            msgGateway.sendReply(request, reply);
        } catch (Exception ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void receivedCreditRequest(CreditRequest request);
}
