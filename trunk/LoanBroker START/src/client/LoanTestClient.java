package client;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents one Clinet Application.
 * It:
 *  1. Creates a ClientRequest for a loan.
 *  2. Sends it to the LoanBroker Messaging-Orianted Middleware (MOM).
 *  3. Receives the reply from the LoanBroker MOM.
 * 
 */
public class LoanTestClient {

    LoanBrokerGateway gateway;

    public LoanTestClient(String name, String requestQueue, String replyQueue) throws Exception {
        super();

        gateway = new LoanBrokerGateway(name, requestQueue, replyQueue);
    }

    /**
     * Sends new loan request to the LoanBroker.
     * @param request
     */
    public void sendRequest(int SSN, int amount, int time) {
        gateway.applyForLoan(new ClientRequest(SSN, amount, time));
    }

    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() {
        try {
            gateway.start();
        } catch (Exception ex) {
            Logger.getLogger(LoanTestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}