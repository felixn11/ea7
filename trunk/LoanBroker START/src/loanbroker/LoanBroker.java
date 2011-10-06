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
     * @param bankRequestQueue
     * @param bankReplyQueue
     */
    public LoanBroker(String clientRequestQueue, String creditRequestQueue, String creditReplyQueue, String bankRequestQueue, String bankReplyQueue) throws Exception {
       super();
        frame = new LoanBrokerFrame();
        activeClientProcesses = new ArrayList<ClientRequestProcess>();
        clientGateway = new ClientGateway(clientRequestQueue) {

            @Override
            public void receivedLoanRequest(ClientRequest request) {
                onClientRequest(request);
                
            }
        };

        creditGateway = new CreditGateway(creditRequestQueue, creditReplyQueue);
        bankGateway = new BankGateway(bankRequestQueue,bankReplyQueue);

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
}
