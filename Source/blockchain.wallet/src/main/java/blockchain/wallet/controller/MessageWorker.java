package blockchain.wallet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import messages.Message;
import node.NodeData;

/**
 * This MessageWorker object is used to process a new network message received
 * by this node. Whenever a new message is passed by the message processor a new
 * MessageWorker is created to process that message. Depending on the endpoint
 * which the received message is having another method/process is executed.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 14 Dec 2021
 */
public class MessageWorker implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(MessageWorker.class);
	private WalletController walletController;

	/**
	 * Instantiates a new MessageWorker object. The parameters must not be null.
	 * 
	 * @param walletController
	 */
	public MessageWorker(WalletController walletController) {
		super();
		this.walletController = walletController;
	}

	/**
	 * If a received message was not processed before it´s data is been processed
	 * based on the endpoint it contains and the message than forwarded to the
	 * connected nodes.
	 */
	@Override
	public void run() {

		Message receivedMessage = walletController.getMessages().poll();

		switch (receivedMessage.getEndpoint()) {

		case DataResponse:

			Object messageData = receivedMessage.getData();

			if (messageData instanceof NodeData) {

				NodeData nodeData = (NodeData) messageData;

				walletController.insertResponseNodeData(nodeData);

			}

			break;

		case DataRequest:

			this.walletController.sendWalletData();

			break;

		default:
			break;
		}

		/**
		 * Forward the received message to all connected nodes if this message was not
		 * already forwarded by this node.
		 */
		try

		{

			walletController.getConnection().getClient().addMessage(receivedMessage);

		} catch (InterruptedException e) {
			logger.error("Forwarding message exception in message worker.", e);
		}

	}

}
