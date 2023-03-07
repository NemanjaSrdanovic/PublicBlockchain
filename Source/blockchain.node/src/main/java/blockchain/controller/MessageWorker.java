
package blockchain.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.Transaction;
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
 * @since 9 Dec 2021
 */
public class MessageWorker implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(MessageWorker.class);
	private MessageController messageController;
	private Transaction transaction;
	private Block block;
	private final boolean blockMinedByThisNode = false;

	/**
	 * Instantiates a new MessageWorker object. The parameters must not be null.
	 * 
	 * @param messageController
	 */
	public MessageWorker(MessageController messageController) {
		super();

		this.messageController = messageController;
	}

	/**
	 * If a received message was not processed before it´s data is been processed
	 * based on the endpoint it contains and the message than forwarded to the
	 * connected nodes.
	 */
	@Override
	public void run() {

		Message receivedMessage = messageController.getMessages().poll();

		Object messageData = receivedMessage.getData();

		switch (receivedMessage.getEndpoint()) {

		/**
		 * Send a message to all nodes in the network to send you their saved data
		 * (Blockchain, transaction pool..)
		 */
		case DataRequest:

			messageController.getNodeController().sendCurrentNodeData(receivedMessage.getSenderNode());

			break;

		/**
		 * If the message containing the node data is a response for this node
		 * (walletAdress) the node data is saved to the database.
		 */
		case DataResponse:

			if (receivedMessage.getReceiverNode()
					.equalsIgnoreCase(messageController.getNodeController().getWallet().getWalletAddress())) {

				if (messageData instanceof NodeData) {

					NodeData nodeData = (NodeData) messageData;

					logger.info("Received node data from " + receivedMessage.getSenderNode());
					messageController.insertResponseNodeData(nodeData);

				}

			}

			break;

		/**
		 * Saving the walletAdress of the sender node and the corresponding public key
		 * to the database
		 * 
		 */
		case PublicKey:

			if (messageData instanceof String) {

				messageController.getNodeController().getDatabase()
						.insertWalletKeyIntoDatabase(receivedMessage.getSenderNode(), (String) messageData);

				logger.info("Public key for wallet " + receivedMessage.getSenderNode() + " saved.");
			}

			break;

		/**
		 * Verify transaction and save it into transaction pool if all criteria are met.
		 */
		case Transaction:

			do {

				if (messageData instanceof Transaction
						&& this.messageController.getNodeController().isStartNodeSynchronised()) {

					transaction = (Transaction) messageData;

					if (messageController.getNodeController().getVerificationController()
							.verifyTransaction(transaction)) {

						messageController.getNodeController().getDatabase()
								.insertTransactionIntoTransactionPool(transaction);
						logger.info(
								"Transaction " + transaction.getTransactionID() + " inserted into transaction pool");
					} else {

						logger.error("Transaction " + transaction.getTransactionID() + " not valid");
					}

				}

			} while (!this.messageController.getNodeController().isStartNodeSynchronised());

			break;

		/**
		 * Verify new block and save it as the next index in the blockchain if all
		 * criteria are met.
		 */
		case Block:

			do {

				if (messageData instanceof Block
						&& this.messageController.getNodeController().isStartNodeSynchronised()) {

					block = (Block) messageData;

					if (messageController.getNodeController().getVerificationController().verifyBlock(block)) {

						logger.info("Received new block with index " + block.getIndex());

						messageController.getNodeController().getBlockchainController()
								.resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(block,
										this.blockMinedByThisNode);

					}

				}

			} while (!this.messageController.getNodeController().isStartNodeSynchronised());

			break;

		default:
			break;
		}

		/**
		 * Forward the received message to all connected nodes if this message was not
		 * already forwarded by this node.
		 */
		try {

			messageController.getConnection().getClient().addMessage(receivedMessage);

			logger.info("Message (" + receivedMessage.getMessageId() + ") forwarded to:"
					+ messageController.getConnection().getClient().getConnectedIPsPort() + "\n");

		} catch (InterruptedException e) {
			logger.error("Forwarding message exception in message worker.", e);
		}

	}

}
