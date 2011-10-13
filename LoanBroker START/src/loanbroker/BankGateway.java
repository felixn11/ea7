/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import javax.jms.JMSException;
import loanbroker.bank.BankQuoteAggregate;
import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import java.util.Hashtable;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import loanbroker.bank.BanksSenderRouter;
import messaging.IReceiver;
import messaging.MessagingFactory;
import messaging.requestreply.IReplyListener;


/**
 * 
 * @author Maja Pesic
 */
public class BankGateway {

    private static final String AGGREGATION_CORRELATION = "aggregation";
    private int aggregateCounter = 0; // counting bank requests

    private BanksSenderRouter sender; // separate sender for each bank
    private IReceiver receiver; // one receiver for all banks

    private BankSerializer serializer; // serializing bank requests and replies

    private Hashtable<Integer, BankQuoteAggregate> replyAggregate; // storing one aggregate (of replies) for each BankQuoteRequests

    /**
     * initialize attributes,register as receiver listener.
     * @param connectionName
     * @param receiveDestination
     */
    public BankGateway(String receiveDestination) {
        super();
        try {
            sender = new BanksSenderRouter();
            receiver = MessagingFactory.createReceiver(receiveDestination);
            receiver.setMessageListener(new MessageListener() {

                public void onMessage(Message message) {
                    messageReceived((TextMessage) message);
                }
            });
            serializer = new BankSerializer();
            replyAggregate = new Hashtable<Integer, BankQuoteAggregate>();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addBank(String destination, String expression) {
        sender.addBank(destination, expression);
    }

    /**
     * @todo Implement the following method:
     * 1. get the AGGREGATION_CORRELATION of the received message
     * 2. get the BankQuoteAggregate that is registered for this AGGREGATION_CORRELATION
     * 3. de-serialize the message into a BankQuoteReply
     * 4. add the  BankQuoteReply to the BankQuoteAggregate
     * 5. if this is the last expected reply,
     *   5.a. notify the BankQuoteAggregate listener and u
     *   5.b. unregister the BankQuoteAggregate
     * @param msg the message that has just been received
     */
    private synchronized void messageReceived(TextMessage msg) {
       
    }

    /**
     * @todo Implement the following method:
     * 1. serialize the request into a string
     * 2. get all eligible banks (ISender) for this request and count them
     * 3. for each eligible bank:
     *    3a. create a new message  for the request
     *    3b. set the JMSReplyTo to the receiver's destination
     *    3c. set the AGGREGATION_CORRELATION to the current aggregateCounter
     *    3d. let the bank send the message
     * 4. if the message was sent to at least one bank: 
     *    4a. create and register a new BankQuoteAggregate for the current value of the aggregateCounter
     *    4b. increase the aggregateCounter
     * 5. if there was no eligible banks (no message was sent), create a
     *     new BankQuoteReply(0, "There are no eligible banks for this loan.", 10)
     *     and notify the listener about its 'arrival'.
     * @param request
     * @param replyListener
     */
    public synchronized void sendRequest(BankQuoteRequest request, IReplyListener<BankQuoteRequest, BankQuoteReply> listener) {
      
    }

    public void start() throws JMSException {
        receiver.openConnection();
        sender.openConnection();
    }
}
