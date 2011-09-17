
import messaging.JMSSettings;
import bank.Bank;
import client.ClientRequest;
import loanbroker.LoanBroker;
import client.LoanTestClient;
import creditbureau.CreditBureau;

/**
 * This application tests the LoanBroker system.
 * 
 */
public class RunMessaging {

    private static final boolean DEBUG_MODE = true;

    public static void main(String[] args) {
        try {
            // create a LoanBroker middleware
            LoanBroker broker = new LoanBroker(JMSSettings.LOAN_REQUEST, JMSSettings.LOAN_REPLY, JMSSettings.CREDIT_REQUEST, JMSSettings.CREDIT_REPLY, JMSSettings.BANK_1, JMSSettings.BANK_REPLY);

            // create a Client Application
            LoanTestClient hypotheeker = new LoanTestClient("The Hypotheker", JMSSettings.LOAN_REQUEST, JMSSettings.LOAN_REPLY);
             // create the CreditBuerau Application
            CreditBureau creditBuerau = new CreditBureau(JMSSettings.CREDIT_REQUEST, JMSSettings.CREDIT_REPLY);

             // create one Bank application
            Bank raboBank = new Bank("Rabo Bank", JMSSettings.BANK_1, JMSSettings.BANK_REPLY, DEBUG_MODE);

            // open all connections in the broker, client and credit applications
            broker.start();
            creditBuerau.start();
            raboBank.start();
            hypotheeker.start();

            // send three requests
            hypotheeker.sendRequest(1, 100000, 24);
            hypotheeker.sendRequest(2, 88888, 5);
            hypotheeker.sendRequest(3, 100, 5);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
