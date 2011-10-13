/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import client.ClientRequest;
import creditbureau.CreditReply;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
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
     *  the collection of active clientRequests
     */
    private ArrayList<ClientRequestProcess> activeClientProcesses;

    /**
     * Intializes attributes, and registers itself (method onClinetRequest) as
     * the listener for new client requests
     * @param connectionName
     * @param clientRequestQueue
     * @param creditRequestQueue
     * @param creditReplyQueue
     * @param bankReplyQueue
     */
    public LoanBroker(String clientRequestQueue, String creditRequestQueue, String creditReplyQueue, String bankReplyQueue) throws Exception {
       super();
        frame = new LoanBrokerFrame();
        activeClientProcesses = new ArrayList<ClientRequestProcess>();
        clientGateway = new ClientGateway(clientRequestQueue) {

            @Override
            public void onClientRequest(ClientRequest request) {
                LoanBroker.this.onClientRequest(request);
                
            }
        };

        creditGateway = new CreditGateway(creditReplyQueue, creditRequestQueue);
        bankGateway = new BankGateway(bankReplyQueue);

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }

    /**
     * This method is called when a new client request arrives.
     * It generates a CreditRequest and sends it to the CreditBureau.
     * @param message the incomming message containng the ClientRequest
     */
    private void onClientRequest(ClientRequest request) { 

        try {
            System.out.println("Loanbroaker received request from Client");
            final ClientRequestProcess p = new ClientRequestProcess(request, creditGateway, clientGateway, bankGateway) {

                @Override
                void notifySentClientReply(ClientRequestProcess process) {
                    activeClientProcesses.remove(process);
                }

                @Override
                void notifyReceivedCreditReply(ClientRequest clientRequest, CreditReply reply) {
                    frame.addObject(clientRequest, reply);
                }

                @Override
                void notifyReceivedBankReply(ClientRequest clientRequest, BankQuoteReply reply) {
                    frame.addObject(clientRequest, reply);
                }
            };
            activeClientProcesses.add(p);
            frame.addObject(null, request);
        } catch (Exception ex) {
            Logger.getLogger(LoanBroker.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    /**
     * starts all gateways
     */
    public void start() throws JMSException {
        clientGateway.start();
        creditGateway.start();
        bankGateway.start();
    }
    
    public void addBank(String destination, String expression) {
        bankGateway.addBank(destination, expression);
    }
}
