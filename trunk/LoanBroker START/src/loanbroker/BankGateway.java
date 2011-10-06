package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import javax.jms.JMSException;
import javax.naming.NamingException;
import bank.BankSerializer;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Ronny
 */
public class BankGateway {

    private AsynchronousRequestor msgGateway;
    private BankSerializer serializer;

    public BankGateway(String requestQueue, String replyQueue) throws NamingException, JMSException, Exception {
        serializer = new BankSerializer();
        msgGateway = new AsynchronousRequestor<BankQuoteRequest, BankQuoteReply>(requestQueue, replyQueue, serializer);        
    }

    protected void getBankQuote(BankQuoteRequest request, IReplyListener<BankQuoteRequest, BankQuoteReply> listener) throws JMSException {
       msgGateway.sendRequest(request, listener);
    }
    
    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    protected void start() throws JMSException {
       msgGateway.start();
    }    
}
