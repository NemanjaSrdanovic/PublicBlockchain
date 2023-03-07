package blockchain.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;
import blockchain.chain.BlockchainController;
import blockchain.concensus.PoW;
import blockchain.database.DriverClass;
import blockchain.wallet.model.Wallet;
import controllers.ConnectionHandler;
import enumerations.EMessageEndpoint;
import messages.Message;
import node.NodeData;

/**
 * This NodeController object is the main instance used to instantiate all
 * controllers, connections, database and wallet of this node. It contains
 * methods for sending wallet data and requesting node data from other nodes in
 * the network.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class NodeController {

	private static Logger logger = LoggerFactory.getLogger(NodeController.class);
	private Wallet wallet;
	private ConnectionHandler connectionHandler;
	private MessageController messageController;
	private BlockchainController blockchainController;
	private DriverClass database;
	private VerificationController verificationController;
	private ExecutorService threadPool;
	private final int waitingTimeForTransPoolSynchronisationSeconds = 15;
	private String lastNodeDataSynchronisationTime;
	private String lastTransactionPoolSynchronisationRequestTime;
	private final String nodeStartTime;

	/**
	 * Instantiates a new NodeController object.
	 */
	public NodeController() {
		super();

		this.connectionHandler = new ConnectionHandler();
		this.database = new DriverClass(this.connectionHandler.getConnection().getMyNode().getServerPortNmr());
		this.wallet = new Wallet();

		this.blockchainController = new BlockchainController(this);
		this.blockchainController.startMiningExecutor(PoW.getMinerStartupTime());
		this.verificationController = new VerificationController(this);
		this.messageController = new MessageController(this);
		this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		this.lastNodeDataSynchronisationTime = null;
		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss.SSS");
		this.nodeStartTime = date.format(new Date());

	}

	/**
	 * Starts the network component of this node by initialising the connection
	 * objects.
	 */
	public void start() {

		this.connectionHandler.run();
		this.connectionHandler.setMessageProcessor(messageController);
		this.threadPool.execute(new SynchronisationWorker(this));

	}

	/**
	 * Retrieves the wallet address and its public key from the wallet object and
	 * broadcasts them to all nodes in the network, to be used when verifying the
	 * transaction signature.
	 */
	public void sendWalletData() {

		Message walletMessage = new Message("broadcast", getWallet().getWalletAddress(), EMessageEndpoint.PublicKey,
				getWallet().getHexStringPublicKey());

		try {
			getConnectionHandler().getConnection().getClient().addMessage(walletMessage);

			logger.info("Message " + walletMessage.getMessageId() + " containing wallet data send.");

			getMessageController().getReceivedMessages().add(walletMessage.getMessageId());

		} catch (InterruptedException e) {

			logger.error("Wallet data forwarding exception in node controller.", e);

		}
	}

	/**
	 * Broadcasts a message to the network requesting the data (blockchain,
	 * transaction pool etc) from all nodes in the network.
	 */
	public void requestNetworkData() {

		Message dataMessage = new Message("broadcast", getWallet().getWalletAddress(), EMessageEndpoint.DataRequest,
				null);

		try {
			getConnectionHandler().getConnection().getClient().addMessage(dataMessage);

			logger.info("Message " + dataMessage.getMessageId() + " requesting network data send.");

			getMessageController().getReceivedMessages().add(dataMessage.getMessageId());

		} catch (InterruptedException e) {

			logger.error("Network data requesting exception in node controller.", e);

		}

	}

	/**
	 * Fetching current data about the blockchain, transaction pool etc. from the
	 * database and sending them as a NodeData object to the node which requested
	 * this data over the DataRequest endpoint.
	 * 
	 * @param receiverNode
	 */
	public void sendCurrentNodeData(String receiverNode) {

		ArrayList<Transaction> transactionPool = getDatabase().getAllTransactionsFromTransactionPool();
		HashMap<String, String> walletsData = getDatabase().getAllPublicKeysFromRegister();
		Blockchain blockchain = getDatabase().getAllBlocksFromBlockchain();

		NodeData nodeData = new NodeData(transactionPool, walletsData, blockchain);

		Message dataMessage = new Message(receiverNode, getWallet().getWalletAddress(), EMessageEndpoint.DataResponse,
				nodeData);

		try {
			getConnectionHandler().getConnection().getClient().addMessage(dataMessage);

			logger.info("Message " + dataMessage.getMessageId() + " containing node data send.");

			getMessageController().getReceivedMessages().add(dataMessage.getMessageId());

		} catch (InterruptedException e) {

			logger.error("Node data sending exception in message controller.", e);

		}

	}

	/**
	 * Broadcasts newly mined block to all nodes in the network.
	 * 
	 * @param block
	 * @return
	 */
	public void broadcastNewlyMinedBlockToTheNetwork(Block block) {

		Message blockMessage = new Message("allNodes", getWallet().getWalletAddress(), EMessageEndpoint.Block, block);

		try {
			getConnectionHandler().getConnection().getClient().addMessage(blockMessage);

			logger.info("Message " + blockMessage.getMessageId() + " containing new block send.");

			getMessageController().getReceivedMessages().add(blockMessage.getMessageId());

		} catch (InterruptedException e) {

			logger.error("Exception sending new block. Sending stopped because block was already received.");

		}

	}

	/**
	 * Request network data, so that the transaction pool is synchronised before the
	 * mining starts. After sending the request a waiting time is started in which
	 * the pool should be synchronised. If the pool is not synchronised in that time
	 * or a exception occurs while doing this method false is returned.
	 */
	public boolean synchronizeTransactionPoolBeforeMining() {

		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss.SSS");

		try {

			logger.info("Transaction pool synchronisation started...");

			requestNetworkData();

			this.lastTransactionPoolSynchronisationRequestTime = date.format(new Date());

			Thread.sleep(this.waitingTimeForTransPoolSynchronisationSeconds * 1000);

			return isSecondDateAfterFirstDate(this.lastTransactionPoolSynchronisationRequestTime,
					this.getLastNodeDataSynchronisationTime());

		} catch (InterruptedException e) {

			logger.error(
					"Exception while transaction pool synchronisation. Synchronisation stopped because block was already received. Node was too slow due congestion.");

		}

		return false;
	}

	/**
	 * Returns true if the node has received at least one data synchronisation after
	 * its start time.
	 * 
	 * @return
	 */
	public boolean isStartNodeSynchronised() {

		if (this.lastNodeDataSynchronisationTime == null) {
			return false;

		} else {

			return isSecondDateAfterFirstDate(this.nodeStartTime, this.lastNodeDataSynchronisationTime);
		}
	}

	/**
	 * Compares the input dates and return true if the second date was after the
	 * first input date.
	 * 
	 * @param firstDate
	 * @param secondDate
	 * @return
	 */
	public synchronized boolean isSecondDateAfterFirstDate(String firstDate, String secondDate) {

		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss.SSS");

		boolean isFirstBeforeSecond = false;

		try {

			isFirstBeforeSecond = date.parse(firstDate).before(date.parse(secondDate));

		} catch (ParseException e) {

			logger.error("Exception while parsing the synchronisation times.");
		}

		return isFirstBeforeSecond;
	}

	/**
	 * Returns the wallet of this node.
	 * 
	 * @return
	 */
	public Wallet getWallet() {
		return wallet;
	}

	/**
	 * Returns the VerificationController object instantiated in this node
	 * controller.
	 * 
	 * @return
	 */
	public synchronized VerificationController getVerificationController() {
		return verificationController;
	}

	/**
	 * Returns the network component ConnectionHandler object.
	 * 
	 * @return
	 */
	public synchronized ConnectionHandler getConnectionHandler() {
		return connectionHandler;
	}

	/**
	 * Returns the MessageController instantiated in this node controller.
	 * 
	 * @return
	 */
	public synchronized MessageController getMessageController() {
		return messageController;
	}

	/**
	 * Returns the DriverClass/database instantiated in this node controller.
	 * 
	 * @return
	 */
	public synchronized DriverClass getDatabase() {
		return database;
	}

	/**
	 * Returns the BlockchainController object instantiated in this node controller.
	 * 
	 * @return
	 */
	public synchronized BlockchainController getBlockchainController() {
		return blockchainController;
	}

	/**
	 * Returns the time and date on which the node data was last synchronised with
	 * the rest of the network.
	 * 
	 * @return -> "yyyy.MM.dd.HH:mm:ss"
	 */
	public String getLastNodeDataSynchronisationTime() {
		return lastNodeDataSynchronisationTime;
	}

	/**
	 * Sets the time and date on which the node data was last synchronised with the
	 * rest of the network.
	 * 
	 * @param string --> "yyyy.MM.dd.HH:mm:ss"
	 */
	public void setLastNodeDataSynchronisationTime(String dateString) {
		this.lastNodeDataSynchronisationTime = dateString;
	}

	/**
	 * Returns the last time and date on which the node data synchronisation request
	 * was send.
	 * 
	 * @return -> "yyyy.MM.dd.HH:mm:ss"
	 */
	public String getLastTransactionPoolSynchronisationRequestTime() {
		return lastTransactionPoolSynchronisationRequestTime;
	}

}
