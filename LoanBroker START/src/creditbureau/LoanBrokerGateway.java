package creditbureau;

import javax.jms.JMSException;
import messaging.requestreply.AsynchronousReplier;

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
    }

    public void start() {
        msgGateway.start();
    }

    protected void sendCreditHistory(CreditRequest request, CreditReply reply) throws JMSException {
        msgGateway.sendReply(request, reply);
    }

    public abstract void receivedCreditRequest(CreditRequest request);
}
