package loanbroker;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Ronny
 */
public class CreditGateway {

    private CreditSerializer serializer;
    private AsynchronousRequestor<CreditRequest, CreditReply> msgGateway;

    public CreditGateway(String requestQueue, String replyQueue) throws Exception {
        // create the serializer
        serializer = new CreditSerializer();
        msgGateway = new AsynchronousRequestor<CreditRequest, CreditReply>(requestQueue, replyQueue, serializer);
    }

    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    public void start() throws JMSException {
        msgGateway.start();
    }

    public void getCreditHistory(CreditRequest request, IReplyListener<CreditRequest, CreditReply> listener) {
        try {
            msgGateway.sendRequest(request, listener);
        } catch (Exception ex) {
            Logger.getLogger(CreditGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
