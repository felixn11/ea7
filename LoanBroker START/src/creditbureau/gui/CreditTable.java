/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package creditbureau.gui;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import gui.CreditReplyCellRenderer;
import gui.CreditRequestCellRenderer;
import gui.SendReceiveTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author Maja Pesic
 */
public class CreditTable extends SendReceiveTable {


    private  CreditRequestCellRenderer requestRenderer = new  CreditRequestCellRenderer();
    private  CreditReplyCellRenderer replyRenderer = new  CreditReplyCellRenderer();

    public CreditTable(TableModel model) {
        super(model);
//                Vector<String> header = new Vector<String>();
//        header.add("ssn");
//        header.add("score");
//        header.add("history");
//        setModel( new CreditTableDataModel(header));
        setReceiveColumns(new int[]{0});
        setSendColumns(new int[]{1,2});
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0) {
            return requestRenderer;
        } else {
            return replyRenderer;
        }
    }

//    private class CreditTableDataModel extends RequestReplyTableModel<CreditRequest, CreditReply> {
//
//        public CreditTableDataModel(Vector<String> column) {
//            super(column);
//        }
//
//        @Override
//        protected RequestReplyRow<CreditRequest, CreditReply> createRow(CreditRequest request) {
//            return new Row(request);
//        }
//    }
//
//    private class Row extends RequestReplyRow<CreditRequest, CreditReply> {
//
//        public Row(CreditRequest request) {
//            super(request, getRowCount());
//        }
//
//        @Override
//        protected void fillRequestCells(CreditRequest request) {
//            add(request.getSSN());
//        }
//
//        @Override
//        protected void fillReplyCells(CreditReply reply) {
//            setValueAt(reply.getCreditScore(), getIndex(), 1);
//            setValueAt(reply.getHistory(), getIndex(), 2);
//        }
//    }
}
