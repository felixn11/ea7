package loanbroker;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
import javax.jms.JMSException;
import javax.naming.NamingException;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;

/**
 *
 * @author Ronny
 */
public abstract class ClientGateway {

    private AsynchronousReplier msgGateway;
    private ClientSerializer serializer;

    public ClientGateway(String requestQueue) throws NamingException, JMSException, Exception {
        serializer = new ClientSerializer();
        msgGateway = new AsynchronousReplier<ClientRequest, ClientReply>(requestQueue, serializer);  
        
        msgGateway.setRequestListener(new IRequestListener<ClientRequest>() {

            public void receivedRequest(ClientRequest request) {
                receivedLoanRequest(request);
            }
        });     
    }
    
    protected void offerLoan(ClientRequest request, ClientReply reply) throws JMSException {
        msgGateway.sendReply(request, reply);    
    }
    
    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    protected void start() throws JMSException {
       msgGateway.start();
    }
    
    public abstract void receivedLoanRequest(ClientRequest request);
}