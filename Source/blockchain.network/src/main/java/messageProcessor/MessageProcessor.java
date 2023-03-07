package messageProcessor;

import messages.Message;

/**
 * Interface that is implemented by the message worker, which uses this network
 * component and forwards the messages received by the UDP_Server object to that
 * message worker.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
@FunctionalInterface
public interface MessageProcessor {

	public void onMessage(Message message);
}
