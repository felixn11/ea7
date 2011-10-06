package bank;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.JMSSettings;
import messaging.MessagingGateway;
import messaging.requestreply.AsynchronousReplier;

/**
 *
 * @author Ronny
 */
abstract class LoanBrokerGateway {

    private AsynchronousReplier<BankQuoteRequest, BankQuoteReply> msgGateway;
    private BankSerializer serializer; // serializer BankQuoteRequest BankQuoteReply to/from XML:

    public LoanBrokerGateway(String bankRequestQueue, String bankReplyQueue) throws Exception {

        // create the serializer
        serializer = new BankSerializer();
        msgGateway = new AsynchronousReplier<BankQuoteRequest, BankQuoteReply>(bankReplyQueue, serializer);        
    }

    public void start() {
        msgGateway.start();
    }

    protected void sendQuoteOffer(BankQuoteRequest request, BankQuoteReply reply) throws JMSException {
        msgGateway.sendReply(request, reply); 
    }        
    
    public abstract void receivedQuoteRequest(BankQuoteRequest request);
}
