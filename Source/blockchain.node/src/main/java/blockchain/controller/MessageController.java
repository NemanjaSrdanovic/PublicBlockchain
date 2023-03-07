package blockchain.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;
import blockchain.database.DriverClass;
import connection.Connection;
import messageProcessor.MessageProcessor;
import messages.Message;
import node.NodeData;

/**
 * This MessageController object is used to receive messages from the
 * blockchain.network and instantiate new MessageWorker objects. It contains
 * methods for processing message data.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class MessageController implements MessageProcessor {

	private static Logger logger = LoggerFactory.getLogger(MessageController.class);
	private Connection connection;
	private BlockingQueue<Message> messages;
	private ExecutorService threadPool;
	private Set<String> receivedMessages;
	private NodeController nodeController;
	private DriverClass database;

	/**
	 * Instantiates a new MessageController object. The parameters must not be null.
	 * 
	 * @param nodeController
	 */
	public MessageController(NodeController nodeController) {
		super();

		this.nodeController = nodeController;
		this.connection = nodeController.getConnectionHandler().getConnection();
		this.messages = new LinkedBlockingQueue<Message>();
		this.database = nodeController.getDatabase();
		this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.receivedMessages = new HashSet<String>();

	}

	/**
	 * Extract data from other nodes about the blockchain, transaction pool etc.
	 * from the NodeData object and inserts them into the database. Because the
	 * primary keys are the unique id´s of the data, no data is save double.
	 * 
	 * @param nodeData
	 */
	public void insertResponseNodeData(NodeData nodeData) {

		ArrayList<Transaction> transactions = (ArrayList<Transaction>) nodeData.getTransactionPool() == null
				? new ArrayList<Transaction>()
				: (ArrayList<Transaction>) nodeData.getTransactionPool();

		Blockchain blockchain = (Blockchain) nodeData.getBlockchain() == null ? new Blockchain()
				: (Blockchain) nodeData.getBlockchain();

		HashMap<String, String> wallets = nodeData.getWalletsData() == null ? new HashMap<String, String>()
				: nodeData.getWalletsData();

		if (transactions.size() > 0) {

			for (Transaction t : transactions) {

				database.insertTransactionIntoTransactionPool(t);
			}
		}

		if (wallets.size() > 0) {

			for (Map.Entry<String, String> entry : nodeData.getWalletsData().entrySet()) {

				database.insertWalletKeyIntoDatabase(entry.getKey(), entry.getValue());

			}

			if (blockchain.getChain().size() > 0) {

				for (Block block : blockchain.getChain()) {

					database.insertBlockIntoBlockchain(block);
					database.moveMinedTransactionsFromPool(block);

					// TODO BlockchainController Blockchain object synchronize

				}

			}

		}

		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss.SSS");
		nodeController.setLastNodeDataSynchronisationTime(date.format(new Date()));

	}

	/**
	 * Receives messages from the network component and executes a new MessageWorker
	 * object witch process that message.
	 */
	@Override
	public void onMessage(Message received) {

		if (received != null) {

			if (!this.receivedMessages.contains(received.getMessageId())) {

				try {
					messages.put(received);
					this.receivedMessages.add(received.getMessageId());

					threadPool.execute(new MessageWorker(this));

				} catch (InterruptedException e) {

					logger.error("Exception while puting message to the message pool and starting new Message Worker: ",
							e);
				}

			}

		}
	}

	/**
	 * Returns the connection object from the network component over which data can
	 * be send to other nodes by using the UDP_Client
	 * 
	 * @return
	 */
	public synchronized Connection getConnection() {
		return connection;
	}

	/**
	 * Returns the queue containing the messages received from the network
	 * component.
	 * 
	 * @return
	 */
	public synchronized BlockingQueue<Message> getMessages() {
		return messages;
	}

	/**
	 * Receives a set containing all messageId´s which were already processed.
	 * 
	 * @return
	 */
	public synchronized Set<String> getReceivedMessages() {
		return receivedMessages;
	}

	/**
	 * Returns the NodeController object.
	 * 
	 * @return
	 */
	public synchronized NodeController getNodeController() {
		return nodeController;
	}

}
