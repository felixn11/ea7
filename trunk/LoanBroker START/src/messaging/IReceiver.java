/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import javax.jms.MessageListener;

/**
 *
 * @author Michael
 */
public interface IReceiver extends IChannel{
    void setMessageListener(MessageListener listener);
}
