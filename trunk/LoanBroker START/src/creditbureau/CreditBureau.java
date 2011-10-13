package creditbureau;

import creditbureau.gui.CreditFrame;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;

/**
 * This class represents one Credit Agency Application.
 * It should:
 *  1. Receive CreditRequest-s for a loan from the LoanBroker Messaging-Orianted Middleware (MOM).
 *  2. Randomly create CreditReply for each request (use method "getReply").
 *  3. Send the CreditReply from the LoanBroker MOM.
 */
public class CreditBureau {

    LoanBrokerGateway gateway;
    private CreditFrame frame; // GUI
    private Random random = new Random(); // for random generation of replies

    public CreditBureau(String creditRequestQueue, String creditReplyQueue) throws Exception {
        super();
        gateway = new LoanBrokerGateway(creditRequestQueue, creditReplyQueue) {
            @Override
            public void receivedCreditRequest(CreditRequest request) {
                processRequest(request);
            }
        };

        // create GUI
        frame = new CreditFrame();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }
    
    void processRequest(CreditRequest request) {
        System.out.println("Creditbureau received request from LoanBroker");
        CreditReply reply = computeReply(request);
        frame.addRequest(request);
        processReply(request, reply);
    }

    void processReply(CreditRequest request, CreditReply reply) {
         System.out.println("Creditbureau sent reply to LoanBroker");
        gateway.sendCreditHistory(request, reply);
        frame.addReply(request, reply);
    }

    /**
     * Randomly generates a CreditReply given the request.
     * @param request is the Creditrequest for which the reply must be generated
     * @return a credit reply
     */
    public CreditReply computeReply(CreditRequest request) {
        int ssn = request.getSSN();

        int score = (int) (random.nextInt(600) + 300);
        int history = (int) (random.nextInt(19) + 1);

        return new CreditReply(ssn, score, history);
    }

    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() throws JMSException {
        gateway.start();
    }
}
