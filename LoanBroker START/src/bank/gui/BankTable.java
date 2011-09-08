/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.gui;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import gui.BankReplyCellRenderer;
import gui.BankRequestCellRenderer;
import gui.SendReceiveTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author Maja Pesic
 */
public class BankTable extends SendReceiveTable {

    private BankReplyCellRenderer replyRenderer = new BankReplyCellRenderer();
    private BankRequestCellRenderer requestRenderer = new BankRequestCellRenderer();

    public BankTable(TableModel model) {
        super(model);
//        Vector<String> header = new Vector<String>();
//        header.add("ssn");
//        header.add("credit");
//        header.add("history");
//        header.add("amount");
//        header.add("time");
//        header.add("interest");
//        header.add("id");
//        header.add("error");
        //setModel(new BankTableDataModel(header));
        
        setReceiveColumns(new int[]{0,1,2,3,4});
        setSendColumns(new int[]{5,6,7});
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column < 5) {
            return requestRenderer;
        } else {
            return replyRenderer;
        }
    }

    /*private class BankTableDataModel extends RequestReplyTableModel<BankQuoteRequest, BankQuoteReply> {

        public BankTableDataModel(Vector<String> column) {
            super(column);
        }

        @Override
        protected RequestReplyRow<BankQuoteRequest, BankQuoteReply> createRow(BankQuoteRequest request) {
            return new Row(request);
        }
    }

    private class Row extends RequestReplyRow<BankQuoteRequest, BankQuoteReply> {

        public Row(BankQuoteRequest request) {
            super(request, getRowCount());
        }

        @Override
        protected void fillRequestCells(BankQuoteRequest request) {
            add(request.getSSN());
            add(request.getCredit());
            add(request.getHistory());
            add(request.getAmount());
            add(request.getTime());
        }

        @Override
        protected void fillReplyCells(BankQuoteReply reply) {
            setValueAt(reply.getInterest(), getIndex(), 5);
            setValueAt(reply.getQuoteId(), getIndex(), 6);
            setValueAt(reply.getError(), getIndex(), 7);
        }
    }*/
}
