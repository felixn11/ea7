/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import loanbroker.bank.BankQuoteAggregate;
import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import java.awt.geom.AffineTransform;
import java.util.Hashtable;
import java.util.Iterator;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import loanbroker.bank.BanksSenderRouter;
import messaging.IReceiver;
import messaging.ISender;
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
        
        try {
            int agrcor = msg.getIntProperty(AGGREGATION_CORRELATION);
            BankQuoteAggregate agr = replyAggregate.get(agrcor);
            BankQuoteReply reply = serializer.replyFromString(msg.getText());
            if (agr.addReply(reply)) {
                agr.notifyListener();
                replyAggregate.remove(agrcor);
            }

        } catch (JMSException ex) {
            Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
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

        try {
            String req = serializer.requestToString(request);
            Iterable<ISender> eligibleBanks = sender.getEligibleBanks(request);
            int bankCounter = 0;
            Iterator it = eligibleBanks.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                bankCounter++;
            }
            System.out.println("Banks: " + bankCounter);
            for (ISender b : eligibleBanks) {
                TextMessage msg = b.createMessage(req);
                msg.setJMSReplyTo(receiver.getDestination());
                msg.setIntProperty(AGGREGATION_CORRELATION, aggregateCounter);
                b.sendMessage(msg);
                System.out.println("Loanbroaker sent request to Bank");
            }
            if (bankCounter > 0) {
                BankQuoteAggregate bankQuoteAggregate = new BankQuoteAggregate(request, bankCounter, listener);
                replyAggregate.put(aggregateCounter, bankQuoteAggregate);
                aggregateCounter++;
            } else {
                BankQuoteReply bankQuoteReply = new BankQuoteReply(0, "There are no eligible banks for this loan.", 10);
                listener.onReply(request, bankQuoteReply);
            }
        } catch (Exception ex) {
            Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() throws JMSException {
        receiver.openConnection();
        sender.openConnection();
    }
}
