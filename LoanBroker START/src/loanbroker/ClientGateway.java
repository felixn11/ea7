package loanbroker;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
import java.util.logging.*;
import javax.jms.JMSException;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;

/**
 *
 * @author Ronny
 */
public abstract class ClientGateway {

    private ClientSerializer serializer;
    private AsynchronousReplier<ClientRequest, ClientReply> msgGateway;

    public ClientGateway(String requestQueue) throws Exception {
        // create the serializer
        serializer = new ClientSerializer();
        msgGateway = new AsynchronousReplier<ClientRequest, ClientReply>(requestQueue, serializer);
        msgGateway.setRequestListener(new IRequestListener<ClientRequest>() {

            public void receivedRequest(ClientRequest request) {
                onClientRequest(request);
            }
        });
    }

    public void start() throws JMSException {
        msgGateway.start();
    }

    abstract void onClientRequest(ClientRequest request);

    public void offerLoan(ClientRequest request, ClientReply reply) {
        try {
            msgGateway.sendReply(request, reply);
        } catch (Exception ex) {
            Logger.getLogger(ClientGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
