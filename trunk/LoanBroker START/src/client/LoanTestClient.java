package client;

import client.gui.ClientFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;

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
    ClientFrame frame; // GUI

    public LoanTestClient(String name, String requestQueue, String replyQueue) throws Exception {
        super();

        gateway = new LoanBrokerGateway(requestQueue, replyQueue) {

            @Override
            public void loanOfferArrived(ClientRequest request, ClientReply reply) {
                processLoanOffer(request, reply);
            }
        };

        // create the GUI
        frame = new ClientFrame(name) {

            @Override
            public void send(ClientRequest request) {
                try {
                    sendRequest(request);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanTestClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                frame.setVisible(true);
            }
        });
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

    /**
     * This message is called whenever a new client reply message arrives.
     * The message is de-serialized into a ClientReply, and the reply is shown in the GUI.
     * @param message
     */
    private void processLoanOffer(ClientRequest request, ClientReply reply) {
        frame.addReply(request, reply);
    }

    /**
     * Sends new loan request to the LoanBroker.
     * @param request
     */
    public void sendRequest(ClientRequest request) throws JMSException {
        gateway.applyForLoan(request);
        frame.addRequest(request);
    }
}