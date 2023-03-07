package blockchain.chain;

import java.util.ArrayList;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.BlockController;
import blockchain.block.BlockHeader;
import blockchain.block.Transaction;
import blockchain.concensus.PoW;
import blockchain.controller.NodeController;
import blockchain.database.DriverClass;
import constraints.Constraints;

/**
 * This mining worker object is started in a new thread every xy minutes
 * (defined in the propro of of work consensus object) to mine a new block.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class MiningWorker extends TimerTask implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(MiningWorker.class);
	private BlockchainController blockchainController;
	private BlockController blockController;
	private DriverClass database;
	private NodeController nodeController;
	private PoW consensusAlgorithm;
	private ArrayList<Transaction> transactionListToBeIncludedInBlock;
	private Block previousBlock;
	private BlockHeader blockHeader;
	private Block newBlock;
	private int blockIndex;
	private final boolean blockMinedByThisNode = true;

	/**
	 * Instantiates a new MiningWorker object. The parameters must not be null
	 * 
	 * @param blockchainController
	 */
	public MiningWorker(BlockchainController blockchainController) {
		super();

		try {

			this.blockchainController = blockchainController;
			this.blockController = blockchainController.getBlockController();
			this.database = blockchainController.getDatabase();
			this.nodeController = blockchainController.getNodeController();
			this.consensusAlgorithm = blockchainController.getConsensusAlgorithm();

			this.previousBlock = database.getLastBlockFromBlockchain();
			this.blockIndex = previousBlock.getIndex() + 1;
			this.blockchainController.setCurrentlyMinedBlockIndex(blockIndex);

		} catch (Exception e) {

			logger.error("Exception while initializing MiningWorker.", e);
		}

	}

	/**
	 * Aligns which transactions will be put into a new block with the rest of the
	 * network. Puts the transactions into a new block, containing a block header
	 * and hashes the block header and the nounce until it gets a hash that meets
	 * the difficulty given by the consensus algorithm.
	 */
	@Override
	public void run() {

		if (this.nodeController.getConnectionHandler().getConnection().getClient().getConnectedIPsPort()
				.size() >= Constraints.MIN_NODES_CONNECTED_TO_SEND) {

			if (this.nodeController.synchronizeTransactionPoolBeforeMining()) {

				logger.info("Started mining a new block...");

				transactionListToBeIncludedInBlock = addCoinbaseTransactionToTransactionList(
						database.getAllTransactionsFromTransactionPoolWhereTimestampBefore(
								this.nodeController.getLastTransactionPoolSynchronisationRequestTime()));

				try {

					blockHeader = this.blockController.generateNewBlockHeader(previousBlock,
							transactionListToBeIncludedInBlock);

					calculateBlockHash(blockHeader);

					if (database.getLastBlockFromBlockchain().getIndex() < this.blockIndex) {

						newBlock = this.blockController.generateNewBlock(blockIndex, blockHeader,
								transactionListToBeIncludedInBlock);

						this.blockchainController.resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(newBlock,
								this.blockMinedByThisNode);
					}

				} catch (IllegalArgumentException e) {

					logger.error("Exception while mining new block.", e);
				}

			} else {

				logger.info("Block mining not started because the transaction pool couldn't be synchronised on time.");

			}
		}
	}

	/**
	 * Receives a list containing all transactions from the transaction pool which
	 * will be added to the next block. Then adds the coinbase transaction
	 * containing the wallet address of this node, so that if his block is accepted
	 * as next block in chain he can receive the mining block reward.
	 * 
	 * @param transactionsFromTheTransactionPool
	 * @return
	 */
	private ArrayList<Transaction> addCoinbaseTransactionToTransactionList(
			ArrayList<Transaction> transactionsFromTheTransactionPool) {

		Transaction coinbaseTransaction = new Transaction("systemWallet",
				blockchainController.getWallet().getWalletAddress(), PoW.getMiningreward());

		ArrayList<Transaction> transactionListToBeIncludedInBlock = transactionsFromTheTransactionPool;

		transactionListToBeIncludedInBlock.add(coinbaseTransaction);

		return transactionListToBeIncludedInBlock;

	}

	/**
	 * Hashes the block header constantly until a hash with the correct difficulty
	 * has been hashed. The hash is edited by incrementing the nounce in the block
	 * header object.
	 * 
	 * @param blockHeader
	 */
	private void calculateBlockHash(BlockHeader blockHeader) {

		String blockHash = this.blockController.calculateBlockHeaderHash(blockHeader);

		while (!this.consensusAlgorithm.hasHashTheCorrectDifficulty(blockHash)
				&& database.getLastBlockFromBlockchain().getIndex() < this.blockIndex) {

			blockHeader.setNounce(blockHeader.getNounce() + 1);
			blockHash = this.blockController.calculateBlockHeaderHash(blockHeader);

		}

		if (database.getLastBlockFromBlockchain().getIndex() < this.blockIndex)
			logger.info("Block hash  calculated: " + blockHash);

	}

}
