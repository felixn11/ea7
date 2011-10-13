/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.bank;

import bank.BankQuoteRequest;
import java.util.*;
import javax.jms.JMSException;
import messaging.ISender;
import messaging.MessagingFactory;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

/**
 * This class contains a collection of SenderGateways for the banks.
 * Given a secific request, it determmines to which banks the request has to be sent.
 * @author Maja Pesic
 */
public class BanksSenderRouter {

    /**
     * The list of senders of all participating banks.
     */
    private ArrayList<BankSender> banks;
   
    /**
     * The only constructor.
     * @param connectionName for JMS
     * @throws NamingException
     * @throws JMSException
     */
    public BanksSenderRouter() throws Exception {
        super();
        banks = new ArrayList<BankSender>();
    }

    public void addBank(String destination, String expression) {
        try {
            banks.add(new BankSender(destination, expression));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    Iterable<ISender> getEligibleBanks(BankQuoteRequest request) {
        ArrayList<ISender> e = new ArrayList<ISender>();
        for (BankSender bank : banks) {
            if (bank.canHandleRequest(request)) {
                e.add(bank.getSender());
            }
        }
        return e;
    }

    public void openConnection() throws JMSException {
        for (BankSender bank : banks) {
            bank.getSender().openConnection();
        }
    }

    /**
     * This class represents the sender to ONE bank.
     */
    private class BankSender {

        private ISender sender;

        private final String expression;
        private final Evaluator evaluator;

        BankSender(String destinationName, String expression) throws Exception {
            super();
            sender = MessagingFactory.createSender(destinationName);
            this.expression = expression;
            this.evaluator = new Evaluator();
        }

        /**
         * returns whether this bank is willing to handle this loan request.
         * @param request
         * @return
         */
        boolean canHandleRequest(BankQuoteRequest request) {
            if (expression != null) {
                try {
                    evaluator.putVariable("amount", Integer.toString(request.getAmount()));
                    evaluator.putVariable("credit", Integer.toString(request.getCredit()));
                    evaluator.putVariable("history", Integer.toString(request.getHistory()));

                    String result = evaluator.evaluate(expression);
                    return result.equals("1.0");
                } catch (EvaluationException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {
                return true;
            }
        }

        ISender getSender(){
            return sender;
        }
    }
}
