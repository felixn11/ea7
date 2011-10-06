package loanbroker;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import javax.jms.JMSException;
import javax.naming.NamingException;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Ronny
 */
public class CreditGateway {

    private AsynchronousRequestor msgGateway;
    private CreditSerializer serializer;

    public CreditGateway(String requestQueue, String replyQueue) throws NamingException, JMSException, Exception {
        serializer = new CreditSerializer();
        msgGateway = new AsynchronousRequestor<CreditRequest, CreditReply>(requestQueue, replyQueue, serializer);        
    }

     protected void getCreditHistory(CreditRequest request, IReplyListener<CreditRequest, CreditReply> listener) throws JMSException {
        msgGateway.sendRequest(request, listener);        
    }
    
    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    protected void start() throws JMSException {
       msgGateway.start();
    }
}
