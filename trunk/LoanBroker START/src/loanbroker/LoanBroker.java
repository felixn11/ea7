/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import client.*;
import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import loanbroker.gui.LoanBrokerFrame;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import messaging.JMSSettings;

/**
 *
 * @author Maja Pesic
 */
public class LoanBroker {

    /**
     *  Connection to the JMS
     */
    private Connection connection; // JMS connection
    protected Session session; // JMS session
      /*
     * Connection to the TestClient
     *
    private MessageProducer clientProducer; // for sending messages to the Client
    private MessageConsumer clientConsumer; // for receiving messages from the Client
     */
    private ClientSerializer clientSerializer; // serializer ClientRequest ClientReply to/from XML:
    private ClientGateway clientGateway;
    /*
     * Connection to the CreditBuerau
     *
    private MessageProducer creditProducer; // for sending messages to the CreditBuerau
    private MessageConsumer creditConsumer; // for receiving messages from the CreditBuerau
     */
    private CreditSerializer creditSerializer; // serializer CreditRequest CreditReply to/from XML:
    private CreditGateway creditGateway;

    /*
     * Connection to the Bank
     */
    /* private MessageProducer bankProducer; // for sending messages to the Bank
    private MessageConsumer bankConsumer; // for receiving messages from the Bank
    private BankSerializer bankSerializer;*/ // serializer BankQuoteRequest BankQuoteReply to/from XML:
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
        // connecting to the JMS 
        Context jndiContext = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(JMSSettings.CONNECTION);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        /*
         * setting up the LoanTestClient connection
         *
        Destination clientSenderDestination = (Destination) jndiContext.lookup(clientReplyQueue);
        clientProducer = session.createProducer(clientSenderDestination);
        Destination clientReceiverDestination = (Destination) jndiContext.lookup(clientRequestQueue);
        clientConsumer = session.createConsumer(clientReceiverDestination);
        clientConsumer.setMessageListener(new MessageListener() {
        
        public void onMessage(Message msg) {
        onClientRequest((TextMessage) msg);
        }
        });
        clientSerializer = new ClientSerializer();
        
        /*
         * setting up the CreditBureau connection
         *
        Destination creditSenderDestination = (Destination) jndiContext.lookup(creditRequestQueue);
        creditProducer = session.createProducer(creditSenderDestination);
        Destination creditReceiverDestination = (Destination) jndiContext.lookup(creditReplyQueue);
        creditConsumer = session.createConsumer(creditReceiverDestination);
        creditConsumer.setMessageListener(new MessageListener() {
        
        public void onMessage(Message msg) {
        onCreditReply((TextMessage) msg);
        }
        });
        creditSerializer = new CreditSerializer();
        
        
        /*
         * setting up the Bank connection
         */
        /*  Destination bankSenderDestination = (Destination) jndiContext.lookup(bankRequestQueue);
        bankProducer = session.createProducer(bankSenderDestination);
        Destination bankReceiverDestination = (Destination) jndiContext.lookup(bankReplyQueue);
        bankConsumer = session.createConsumer(bankReceiverDestination);
        bankConsumer.setMessageListener(new MessageListener() {
        
        public void onMessage(Message msg) {
        onBankReply((TextMessage) msg);
        }
        });
        bankSerializer = new BankSerializer();*/
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
            void onClientReply(ClientReply reply) {
                LoanBroker.this.onClientRequest(null);
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
    private void onClientRequest(ClientReply reply) {
        try {
           frame.addObject(null, reply);
           clientGateway.offerLoan(null, reply);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    private void onClientReply(ClientReply reply) {
        frame.addObject(null, reply);
        clientGateway.onClientReply(reply);
    }

    /**
     * This method is called when a new credit reply arrives.
     * It generates a BankQuoteRequest and sends it to the Bank.
     * @param message the incomming message containng the CreditReply
     */
    private void onCreditReply(CreditReply reply) {
               try {
            frame.addObject(null, reply); // add the reply to the GUI            
            creditGateway.getCreditHistory(null, reply);
            //bankProducer.send(bankRequestMessage); // send the bank quote request message to the Bank      
            //bankProducer.send(bankRequestMessage); // send the bank quote request message to the Bank
        } catch (JMSException ex) {
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
            frame.addObject(null, reply); // add the reply to the GUI            
            bankGateway.getBankQuote(null, reply);
        } catch (JMSException ex) {
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
        try {
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
