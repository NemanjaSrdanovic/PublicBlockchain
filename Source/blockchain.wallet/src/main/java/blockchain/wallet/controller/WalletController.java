package blockchain.wallet.controller;

import java.util.HashSet;
import java.util.Map.Entry;
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
import blockchain.wallet.model.Wallet;
import connection.Connection;
import controllers.ConnectionHandler;
import enumerations.EMessageEndpoint;
import messageProcessor.MessageProcessor;
import messages.Message;
import node.NodeData;

/**
 * This WalletController object is the main instance used to instantiate all
 * controllers, connections and wallet of this node. It contains methods for
 * sending transactions, wallet data and requesting node data from other nodes
 * in the network.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 14 Dec 2021
 */
public class WalletController implements MessageProcessor {

	private static Logger logger = LoggerFactory.getLogger(WalletController.class);
	private ConnectionHandler connectionHandler;
	private ExecutorService threadPool;
	private BlockingQueue<Message> messages;
	private Set<String> receivedMessages;
	private Connection connection;
	private Wallet wallet;
	private Blockchain blockchain;

	/**
	 * Instantiates a new WalletController object that builds its own connection.
	 */
	public WalletController() {
		super();
		this.connectionHandler = new ConnectionHandler();
		this.connection = connectionHandler.getConnection();
		this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.messages = new LinkedBlockingQueue<Message>();
		this.receivedMessages = new HashSet<String>();
		this.wallet = new Wallet();
		this.blockchain = new Blockchain();

		this.requestNetworkData();
	}

	/**
	 * Instantiates a new WalletController object and hand over a existing
	 * connection.
	 * 
	 * @param connectionHandler
	 */
	public WalletController(ConnectionHandler connectionHandler) {
		super();
		this.connectionHandler = connectionHandler;
		this.connection = connectionHandler.getConnection();
		this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.messages = new LinkedBlockingQueue<Message>();
		this.receivedMessages = new HashSet<String>();
		this.wallet = new Wallet();
		this.blockchain = new Blockchain();

		this.requestNetworkData();
	}

	/**
	 * Start the connection for the WalletController object, connects the UDP_Server
	 * with the object that it can receive the messages and synchronises the network
	 * data.
	 */
	public void start() {

		this.connectionHandler.run();
		this.connectionHandler.setMessageProcessor(this);
		this.sendWalletData();

	}

	/**
	 * Iterates the blockchain and sets the wallet balance.
	 */
	private void updateWalletBalance() {

		long newBalance = 0;

		for (Block block : getBlockchain().getChain()) {

			for (Transaction transaction : block.getTransactionList()) {

				if (transaction.getFromAdress().equals(getWallet().getWalletAddress())) {

					newBalance -= transaction.getAmount();
				}

				if (transaction.getToAdress().equals(getWallet().getWalletAddress())
						|| transaction.getToAdress().equals("balance")) {

					newBalance += transaction.getAmount();
				}
			}
		}

		getWallet().setBalance(newBalance);
	}

	/**
	 * Iterates through the transaction list contained in the last block and updates
	 * the pending transactions if the list contains a transaction that is pending
	 * for this wallet.
	 */
	private void updatePendingTransactions() {

		Block lastBlock = getBlockchain().getChain().get(getBlockchain().getChain().size() - 1);

		for (Transaction transaction : lastBlock.getTransactionList()) {

			if (getWallet().getWalletPendingTransaction().containsKey(transaction.getTransactionID())) {

				getWallet().getWalletPendingTransaction().remove(transaction.getTransactionID());

				getWallet().setPendingTransactionsAmount(
						getWallet().getPendingTransactionsAmount() - transaction.getAmount());
			}

		}

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
			getConnection().getClient().addMessage(walletMessage);

			getReceivedMessages().add(walletMessage.getMessageId());

		} catch (InterruptedException e) {

			logger.error("Wallet data forwarding exception in node controller.", e);

		}
	}

	/**
	 * Checks if the wallet amount is sufficient to do this transaction. Uses the
	 * wallet to sign this transaction with the private key generated in the node
	 * wallet. Sends the transaction to all other nodes in the network by using the
	 * network component and its features.
	 * 
	 * @param receiver
	 * @param amount
	 */
	public void sendTransaction(String receiver, double amount) {

		this.setCurrentWalletBalance();

		if (getWallet().getBalance() >= amount) {

			Transaction transaction = new Transaction(getWallet().getWalletAddress(), receiver, amount);

			transaction.setSignature(getWallet().signTransaction(transaction.getTransactionID()));
			Message transactionMessage = new Message("all Nodes", getWallet().getWalletAddress(),
					EMessageEndpoint.Transaction, transaction);

			try {
				getConnection().getClient().addMessage(transactionMessage);

				Thread.sleep(3000);

				if (getConnection().getClient().isMessageReceived(transactionMessage)) {

					getWallet().setPendingTransactionsAmount(
							getWallet().getPendingTransactionsAmount() + transaction.getAmount());

					getWallet().getWalletPendingTransaction().put(transaction.getTransactionID(), transaction);

					logger.info("Message " + transactionMessage.getMessageId() + " containing transaction send.");

				} else {

					logger.info(
							"Message not send due network problems. Please try again after the automatic client reconnection.. ");
				}

			} catch (InterruptedException e) {

				logger.error("Transaction creation exception in node controller.", e);

			}

		} else

		{

			logger.error("Transaction creation not possible due verification problem : Not enough balance.");
		}

	}

	/**
	 * Requests the blockchain from all nodes in the network to update it and to be
	 * able to display it.
	 */
	public synchronized void requestNetworkData() {

		Message dataMessage = new Message("broadcast", getWallet().getWalletAddress(), EMessageEndpoint.DataRequest,
				null);

		try {
			getConnection().getClient().addMessage(dataMessage);

			getReceivedMessages().add(dataMessage.getMessageId());

		} catch (InterruptedException e) {

			logger.error("Network data requesting exception in node controller.", e);

		}

	}

	/**
	 * Updates the Blockchain object with the network data received.
	 * 
	 * @param nodeData
	 */
	public synchronized void insertResponseNodeData(NodeData nodeData) {

		this.blockchain = (Blockchain) nodeData.getBlockchain();

		if (blockchain != null)
			this.setCurrentWalletBalance();

	}

	/**
	 * Receives messages from the network component and executes a new MessageWorker
	 * object witch process that message.
	 */
	@Override
	public void onMessage(Message received) {

		if (received != null) {

			if (!getReceivedMessages().contains(received.getMessageId())) {

				try {
					getMessages().put(received);

					getReceivedMessages().add(received.getMessageId());

					threadPool.execute(new MessageWorker(this));

				} catch (InterruptedException e) {

					logger.error("Exception while puting message to the message pool and starting new Message Worker: ",
							e);
				}

			}
		}
	}

	/**
	 * Updates values from the wallet object that are used to determine the wallet
	 * balance.
	 */
	public void setCurrentWalletBalance() {

		updateWalletBalance();

		updatePendingTransactions();

	}

	/**
	 * Returns the ConnectionHandler object for this controller.
	 * 
	 * @return
	 */
	public synchronized ConnectionHandler getConnectionHandler() {
		return connectionHandler;
	}

	/**
	 * Returns the list containing the id of received messages for this controller.
	 * 
	 * @return
	 */
	public synchronized Set<String> getReceivedMessages() {
		return receivedMessages;
	}

	/**
	 * Returns the queue containing the messages which have to be processed by this
	 * controller.
	 * 
	 * @return
	 */
	public synchronized BlockingQueue<Message> getMessages() {
		return messages;
	}

	/**
	 * Returns the Connection object for this controller.
	 * 
	 * @return
	 */
	public synchronized Connection getConnection() {
		return connection;
	}

	/**
	 * Returns the Wallet object for this controller.
	 * 
	 * @return
	 */
	public Wallet getWallet() {
		return wallet;
	}

	/**
	 * Returns the Blockchain object for this controller.
	 * 
	 * @return
	 */
	public Blockchain getBlockchain() {
		return blockchain;
	}

	/**
	 * Displays the pending transactions list in a user frendly format.
	 * 
	 * @return
	 */
	public String pendingTransactionsToString() {

		String pendingTransactions = "     Time          " + " | "
				+ "                                ID                              " + " | " + "Amount" + "\n";

		for (Entry<String, Transaction> entry : getWallet().getWalletPendingTransaction().entrySet()) {

			pendingTransactions += entry.getValue().getTimeStamp() + " | " + entry.getKey() + " | "
					+ entry.getValue().getAmount() + "\n";

		}

		return pendingTransactions;
	}

}
