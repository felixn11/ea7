package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import messaging.requestreply.AsynchronousRequestor;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Ronny
 */
public class BankGateway {

    private BankSerializer serializer;
    private AsynchronousRequestor<BankQuoteRequest, BankQuoteReply> msgGateway;

    public BankGateway(String requestQueue, String replyQueue) throws Exception {
        // create the serializer
        serializer = new BankSerializer();
        msgGateway = new AsynchronousRequestor<BankQuoteRequest, BankQuoteReply>(requestQueue, replyQueue, serializer);
    }

    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    public void start() {
        msgGateway.start();
    }

    public void getBankQuote(BankQuoteRequest request, IReplyListener<BankQuoteRequest, BankQuoteReply> listener) {
        try {
            msgGateway.sendRequest(request, listener);
        } catch (Exception ex) {
            Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
