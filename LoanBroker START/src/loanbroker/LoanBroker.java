/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import client.ClientReply;
import client.ClientRequest;
import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import java.util.logging.Level;
import java.util.logging.Logger;
import loanbroker.gui.LoanBrokerFrame;

/**
 *
 * @author Maja Pesic
 */
public class LoanBroker {

    private ClientGateway clientGateway;
    private CreditGateway creditGateway;
    private BankGateway bankGateway;
    private LoanBrokerFrame frame; // GUI

    /**
     * Intializes attributes, and registers itself (method onClinetRequest) as
     * the listener for new client requests
     * @param connectionName
     * @param clientRequestQueue
     * @param creditRequestQueue
     * @param creditReplyQueue
     * @param bankRequestQueue
     * @param bankReplyQueue
     */
    public LoanBroker(String clientRequestQueue, String clientReplyQueue, String creditRequestQueue, String creditReplyQueue, String bankRequestQueue, String bankReplyQueue) throws Exception {
        super();

        /*
         * Make the GUI
         */
        frame = new LoanBrokerFrame();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
        clientGateway = new ClientGateway() {

            @Override
            void onClientRequest(ClientRequest request) {
                LoanBroker.this.onClientRequest(request);
            }
        };

        creditGateway = new CreditGateway() {

            @Override
            void onCreditReply(CreditReply reply) {
                LoanBroker.this.onCreditReply(reply);
            }
        };

        bankGateway = new BankGateway() {

            @Override
            void onBankReply(BankQuoteReply reply) {
                LoanBroker.this.onBankReply(reply);
            }
        };
    }

    /**
     * This method is called when a new client request arrives.
     * It generates a CreditRequest and sends it to the CreditBureau.
     * @param message the incomming message containng the ClientRequest
     */
    private void onClientRequest(ClientRequest request) {
        try {
            CreditRequest credit = createCreditRequest(request);
            creditGateway.getCreditHistory(credit);
            frame.addObject(null, request);
        } catch (Exception ex) {
            Logger.getLogger(LoanBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called when a new credit reply arrives.
     * It generates a BankQuoteRequest and sends it to the Bank.
     * @param message the incomming message containng the CreditReply
     */
    private void onCreditReply(CreditReply reply) {
        try {
            BankQuoteRequest bank = createBankRequest(null, reply);
            bankGateway.getBankQuote(bank);
            frame.addObject(null, reply); // add the reply to the GUI 
        } catch (Exception ex) {
            Logger.getLogger(LoanBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called when a new bank quote reply arrives.
     * It generates a ClientReply and sends it to the LoanTestClient.
     * @param message the incomming message containng the BankQuoteReply
     */
    private void onBankReply(BankQuoteReply reply) {
        try {
            ClientReply client = createClientReply(reply);
            clientGateway.offerLoan(client);
            frame.addObject(null, reply); // add the reply to the GUI  
        } catch (Exception ex) {
            Logger.getLogger(LoanBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates a credit request based on the given client request.
     * @param clientRequest
     * @return
     */
    private CreditRequest createCreditRequest(ClientRequest clientRequest) {
        return new CreditRequest(clientRequest.getSSN());
    }

    /**
     * Generates a bank quote reguest based on the given client request and credit reply.
     * @param creditReply
     * @return
     */
    private BankQuoteRequest createBankRequest(ClientRequest clientRequest, CreditReply creditReply) {
        int ssn = creditReply.getSSN();
        int score = creditReply.getCreditScore();
        int history = creditReply.getHistory();
        int amount = 100;
        int time = 24;
        if (clientRequest != null) {
            amount = clientRequest.getAmount();
            time = clientRequest.getTime();
        }
        return new BankQuoteRequest(ssn, score, history, amount, time);
    }

    /**
     * Generates a client reply based on the given bank quote reply.
     * @param creditReply
     * @return
     */
    private ClientReply createClientReply(BankQuoteReply reply) {
        return new ClientReply(reply.getInterest(), reply.getQuoteId());
    }

    /**
     * Opens connestion to JMS,so that messages can be send and received.
     */
    public void start() {
        clientGateway.start();
        bankGateway.start();
        creditGateway.start();
    }
}
