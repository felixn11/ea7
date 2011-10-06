package bank;

import bank.gui.BankFrame;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;

/**
 * This class represents one Bank Application.
 * It sould eventually:
 *  1. Receive BankQuoteRequest-s for a loan from the LoanBroker Messaging-Orianted Middleware (MOM).
 *  2. Randomly create BankQuoteReply for each request (use method "computeBankReply").
 *  3. Send the BankQuoteReply from the LoanBroker MOM.
 */
public class Bank {

    private boolean debug_mode;
    LoanBrokerGateway gateway;
    private BankFrame frame; // GUI
    private static final int ERROR_CODE = 1;
    private static final int NO_ERROR_CODE = 0;
    private final double primeRate = 3.5;
    private String name;
    private double ratePremium = 0.5;
    private int maxLoanTerm = 10000;
    protected Random random = new Random();
    public int quoteCounter = 0;

    public Bank(String bankName, String bankRequestQueue, String bankReplyQueue, boolean debug_mode) throws Exception {
        super();

        gateway = new LoanBrokerGateway(bankRequestQueue, bankReplyQueue) {

            @Override
            public void receivedQuoteRequest(BankQuoteRequest request) {
                // New request received from the LoanBroker compute reply
                handleBankQuoteRequest(request);
            }
        };
        this.name = bankName;
        this.debug_mode = debug_mode;
        frame = new BankFrame(bankName, debug_mode) {

            @Override
            public boolean sendBankReply(BankQuoteRequest request, double interest, int error) {
                String quoteID = name + "-" + String.valueOf(quoteCounter);
                quoteCounter++;
                BankQuoteReply reply = new BankQuoteReply(interest, quoteID, error);
                try {
                    gateway.sendQuoteOffer(request, reply);
                    return true;
                } catch (JMSException ex) {
                    Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
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
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Processes a new request message. Only if the debug_mode is true, this method
     * randomly generates a reply and sends it back.
     * @param message
     */
    private void handleBankQuoteRequest(BankQuoteRequest request) {
        try {
            frame.addRequest(request);
            if (debug_mode) { // only in debug mode send immediately random reply
                BankQuoteReply reply = computeReply(request);
                gateway.sendQuoteOffer(request, reply);
                frame.addReply(request, reply);
            }
        } catch (Exception ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Randomly generates a reply for the given request.
     * @param request for which the reply should be generaged.
     * @return randomly generated reply
     */
    public BankQuoteReply computeReply(BankQuoteRequest request) {
        double interest = 0.0;
        int error = ERROR_CODE;
        if (request.getTime() <= maxLoanTerm) {
            Double interestTemp = new Double(primeRate + ratePremium + (double) (request.getTime() / 12) / 10 + (double) random.nextInt(10) / 10);
            DecimalFormat niceFormat = new DecimalFormat("#,#"); // round to two decimals
            interest = Double.valueOf(niceFormat.format(interestTemp));

            error = NO_ERROR_CODE;
        }
        String quoteID = name + "-" + String.valueOf(++quoteCounter);
        return new BankQuoteReply(interest, quoteID, error);
    }
}
