package bank;

import java.util.logging.*;
import javax.jms.JMSException;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;

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
        msgGateway.setRequestListener(new IRequestListener<BankQuoteRequest>() {

            public void receivedRequest(BankQuoteRequest request) {
                receivedQuoteRequest(request);
            }
        });
    }

    public void start() throws JMSException{
        msgGateway.start();
    }

    protected void sendQuoteOffer(BankQuoteRequest request, BankQuoteReply reply){
        try {
            msgGateway.sendReply(request, reply);
        } catch (Exception ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }        
    
    abstract void receivedQuoteRequest(BankQuoteRequest request);
}
