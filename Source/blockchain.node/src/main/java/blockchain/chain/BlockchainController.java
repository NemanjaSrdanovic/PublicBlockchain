package blockchain.chain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.BlockController;
import blockchain.block.BlockHeader;
import blockchain.block.MerkleTree;
import blockchain.block.Transaction;
import blockchain.concensus.PoW;
import blockchain.controller.NodeController;
import blockchain.database.DriverClass;
import blockchain.wallet.model.Wallet;

/**
 * This blockchain controller object is used to control and initialise a new
 * blockchain objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class BlockchainController {

	private static Logger logger = LoggerFactory.getLogger(BlockchainController.class);
	private NodeController nodeController;
	private DriverClass database;
	private Wallet wallet;
	private PoW consensusAlgorithm;
	private Blockchain blockchain;
	private BlockController blockController;
	private ScheduledExecutorService miningExecutor;
	private Future<?> minerFuture;
	private int currentlyMinedBlockIndex;
	private final BlockchainController blockchainController;

	/**
	 * Instantiates a new blockchain controller object. The parameters must not be
	 * null.
	 * 
	 * @param nodeController
	 */
	public BlockchainController(NodeController nodeController) {

		this.nodeController = nodeController;
		this.database = nodeController.getDatabase();
		this.wallet = nodeController.getWallet();
		this.blockchain = new Blockchain();
		this.consensusAlgorithm = new PoW();
		this.blockController = new BlockController();
		this.addGenesisBlockToChain();
		this.currentlyMinedBlockIndex = database.getLastBlockFromBlockchain().getIndex() + 1;
		this.blockchainController = this;
		this.miningExecutor = Executors.newSingleThreadScheduledExecutor();

	}

	/**
	 * Starts the MiningWorker object at a full second minute (if node is started at
	 * 13:43:35 it starts the MiningWorker at 13:45:00) and then restarts new
	 * objects at a fixed rate defined by the consensus alg.
	 * 
	 * @param waitingTimeUntilNextMiningSeconds
	 */
	public void startMiningExecutor(int waitingTimeUntilNextMiningSeconds) {

		LocalDateTime lt = LocalDateTime.now();

		this.minerFuture = this.miningExecutor.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				MiningWorker worker = new MiningWorker(blockchainController);
				worker.run();
			}
		}, (waitingTimeUntilNextMiningSeconds - lt.getSecond()) * 1000, PoW.getMiningRate() * 1000,
				TimeUnit.MILLISECONDS);
	}

	/**
	 * Resolves conflict in which two blocks with the same index arrive at the Node
	 * by comparing the timestamp, which is set when the block was mined and sets
	 * the block which was earlier mined as the correct block for the blockchain. If
	 * there is not any conflict inserts the block in the blockchain.
	 * 
	 * @param newBlock
	 */
	public synchronized void resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(Block newBlock,
			boolean blockMinedByThisNode) {

		if (newBlock == null)
			throw new IllegalArgumentException("Block for broadcasting and inputing can´t be null");

		Block blockInBlockchain = database.getBlockWithInputIndex(newBlock.getIndex());
		boolean isNewBlockBeforeExistingBlock = false;

		if (blockInBlockchain != null) {

			SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss.SSS");

			try {
				isNewBlockBeforeExistingBlock = date.parse(newBlock.getTimeStamp())
						.before(date.parse(blockInBlockchain.getTimeStamp()));

			} catch (ParseException e) {

				logger.error("Exception while comparing block time stamps.", e);
			}

			if (isNewBlockBeforeExistingBlock) {

				database.replaceBlockFromBlockchain(newBlock);

				logger.info("Block consensus conflict: Existing block with index " + blockInBlockchain.getIndex()
						+ " in blockchain replaced because new block was mined before.");

			}

		} else {

			database.insertBlockIntoBlockchain(newBlock);

			this.blockchain.addBlockToBlockchain(newBlock);

			database.moveMinedTransactionsFromPool(newBlock);

		}

		if (blockMinedByThisNode && (isNewBlockBeforeExistingBlock || blockInBlockchain == null

		)) {

			nodeController.broadcastNewlyMinedBlockToTheNetwork(newBlock);

			logger.info("New block with index " + newBlock.getIndex() + " mined.");
		}

	}

	/**
	 * Insert a hard coded genesis block to the blockchain which includes the static
	 * balance for every node in the network.
	 */
	public void addGenesisBlockToChain() {

		Transaction genesisTransaction = new Transaction(
				"95f43a460e0135a5f8a26031d663c956f02cf01429b1812bd33769862af7611b", "balance", 1000);
		genesisTransaction.setTransactionID("c9f02ccdf2a04668aa872658d25da28b264140df7a234b9cbdb53380deeaa69b");
		genesisTransaction.setTimeStamp("2009.01.03.19:15:00");
		ArrayList<Transaction> transactions = new ArrayList<>();
		transactions.add(genesisTransaction);

		MerkleTree merkleTree = new MerkleTree(transactions);

		BlockHeader blockHeader = new BlockHeader("0", merkleTree.getMerkleRoot(), 0);
		blockHeader.setTimeStamp("2009.01.03.19:15:00");

		Block genesisBlock = new Block(0, blockHeader, transactions, transactions.size());
		genesisBlock.setTimeStamp("2009.01.03.19:15:00");

		blockchain.addBlockToBlockchain(genesisBlock);

		database.insertBlockIntoBlockchain(genesisBlock);

		logger.info("Genesis block with index " + genesisBlock.getIndex() + " added to blockchain");

	}

	/**
	 * Stops the mining executor from starting new MiningWorker objects every fixed
	 * time rate.
	 */
	public void stopMiningExecutor() {

		miningExecutor.shutdown();
		try {
			if (!miningExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				miningExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			miningExecutor.shutdownNow();
		}

	}

	/**
	 * Stops the currently running mining worker if for example a block with the
	 * same index as the block which should be mined arrives to the node.
	 */
	public void stopCurrentMiner() {

		this.minerFuture.cancel(true);

		logger.info("Miner for block index " + this.currentlyMinedBlockIndex + " stoped.");

		startMiningExecutor(PoW.getMiningRate());
	}

	/**
	 * Returns the proof of work consensus algorithm.
	 * 
	 * @return
	 */
	public PoW getConsensusAlgorithm() {
		return consensusAlgorithm;
	}

	/**
	 * Returns the blockchain object.
	 * 
	 * @return
	 */
	public Blockchain getBlockchain() {
		return blockchain;
	}

	/**
	 * Returns the database object.
	 * 
	 * @return
	 */
	public DriverClass getDatabase() {
		return database;
	}

	/**
	 * Returns the NodeController object.
	 * 
	 * @return
	 */
	public NodeController getNodeController() {
		return nodeController;
	}

	/**
	 * Returns the BlockController object.
	 * 
	 * @return
	 */
	public BlockController getBlockController() {
		return blockController;
	}

	/**
	 * Returns the wallet object for this node.
	 * 
	 * @return
	 */
	public Wallet getWallet() {
		return wallet;
	}

	/**
	 * Returns index of the block which is been currently mined by the MiningWorker.
	 * object
	 * 
	 * @return
	 */
	public int getCurrentlyMinedBlockIndex() {
		return currentlyMinedBlockIndex;
	}

	/**
	 * Sets index of the block which is been currently mined by the MiningWorker.
	 * 
	 * @param currentlyMinedBlockIndex
	 */
	public void setCurrentlyMinedBlockIndex(int currentlyMinedBlockIndex) {
		this.currentlyMinedBlockIndex = currentlyMinedBlockIndex;

	}

	/**
	 * Returns thread pool containing the MiningWorker object.
	 * 
	 * @return
	 */
	public ScheduledExecutorService getMiningPool() {
		return miningExecutor;
	}

}
