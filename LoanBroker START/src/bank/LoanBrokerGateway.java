package bank;

import java.util.logging.*;
import javax.jms.JMSException;
import javax.jms.Message;
import loanbroker.BankGateway;
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
        msgGateway = new AsynchronousReplier<BankQuoteRequest, BankQuoteReply>(bankRequestQueue, serializer) {

            @Override
            public void beforeSendReply(Message request, Message reply) {
                try {
                    String AGGREGATION_CORRELATION = "aggregation";
                    int agrcor = request.getIntProperty(AGGREGATION_CORRELATION);
                    reply.setIntProperty(AGGREGATION_CORRELATION, agrcor);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        msgGateway.setRequestListener(new IRequestListener<BankQuoteRequest>() {

            public void receivedRequest(BankQuoteRequest request) {
                System.out.println("Bank received request from Loanbroaker");
                receivedQuoteRequest(request);
            }
        });
    }

    public void start() throws JMSException {
        msgGateway.start();
    }

    protected void sendQuoteOffer(BankQuoteRequest request, BankQuoteReply reply) {
        try {
            msgGateway.sendReply(request, reply);
        } catch (Exception ex) {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract void receivedQuoteRequest(BankQuoteRequest request);
}
