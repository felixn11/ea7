package messaging;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author Ronny
 */
public class MessagingGateway {

    public MessagingGateway(String senderName, String receiverName) {
    }

    public MessagingGateway(String receiverName) {
    }

    public boolean sendMessage(Message msg) {
        return true;
    }

    public boolean sendMessage(Destination destination, Message msg) {
        return true;
    }

    public TextMessage createMessage(String body) {
        return null;
    }

    public void setReceivedMessageListener(MessageListener listener) {
    }

    public void openConnection() {
    }
}