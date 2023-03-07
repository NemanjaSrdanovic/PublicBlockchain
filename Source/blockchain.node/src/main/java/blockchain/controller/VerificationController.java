package blockchain.controller;

import java.security.Signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.BlockHeader;
import blockchain.block.MerkleTree;
import blockchain.block.Transaction;
import blockchain.concensus.PoW;
import blockchain.concensus.SHA256Hasher;
import blockchain.database.DriverClass;
import blockchain.wallet.helper.KeyGenerator;

/**
 * This VerificationController object is used to verify that transactions and
 * blocks are not corrupted e.g. that the transactionID is correctly calculated,
 * the signature correct for the given data etc.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class VerificationController {

	private static Logger logger = LoggerFactory.getLogger(VerificationController.class);
	private NodeController nodeController;
	private PoW consensusAlgorithm;
	private KeyGenerator keyGenerator;
	private DriverClass database;

	/**
	 * Instantiates a new VerificationController object. The parameters must not be
	 * null.
	 * 
	 * @param nodeController
	 */
	public VerificationController(NodeController nodeController) {
		super();
		this.nodeController = nodeController;
		this.keyGenerator = nodeController.getWallet().getKeyGenerator();
		this.database = nodeController.getDatabase();
		this.consensusAlgorithm = nodeController.getBlockchainController().getConsensusAlgorithm();
	}

	/**
	 * Verify that the transaction sender has enough balance and that the
	 * transactionID and signature are been correctly calculated.
	 * 
	 * @param transaction
	 * @return
	 */
	public boolean verifyTransaction(Transaction transaction) {

		boolean hasEnoughBalance = transactionSenderHasEnoughBalance(transaction);
		boolean signatureAndIDCorrect = verifyTransactionSignature(transaction);

		if (!hasEnoughBalance) {

			logger.info(
					"Transacton sender for transaction " + transaction.getTransactionID() + " has not enough balance.");
		}

		if (!signatureAndIDCorrect) {

			logger.info("Signature or ID for transaction " + transaction.getTransactionID() + " corrupted");
		}

		return hasEnoughBalance && signatureAndIDCorrect;
	}

	/**
	 * Verifies the provided block by validating the block hash, previous block hash
	 * and merkle root.
	 * 
	 * @param block
	 * @return
	 */
	public boolean verifyBlock(Block block) {

		boolean isBlockHashValid = validateBlockHash(block.getBlockHeader());

		boolean isPreviousBlockHashValid = validatePreviousBlockHash(block);

		boolean isMerkleRootValid = validateMerkleRoot(block);

		if (!isBlockHashValid) {

			logger.info("Block hash for block with index " + block.getIndex() + " mined on the " + block.getTimeStamp()
					+ " corrupted");
		}

		if (!isPreviousBlockHashValid) {

			logger.info("Previous Block hash for block with index " + block.getIndex() + " mined on the "
					+ block.getTimeStamp() + "  not valid");
		}

		if (!isMerkleRootValid) {

			logger.info("Merkle root for block with index " + block.getIndex() + " mined on the " + block.getTimeStamp()
					+ " corrupted.");
		}

		return isBlockHashValid && isPreviousBlockHashValid && isMerkleRootValid;
	}

	/**
	 * Validate the block hash by hashing the block header again and comparing the
	 * hash to the hash provided in the block.
	 * 
	 * @param blockHeader
	 * @return
	 */
	private boolean validateBlockHash(BlockHeader blockHeader) {

		return consensusAlgorithm.hasHashTheCorrectDifficulty(
				SHA256Hasher.returnSHA256HashStringFromString(blockHeader.getHeaderDataForHashCalculation()));
	}

	/**
	 * Validate the previous hash by hashing the block header of the previous block
	 * in chain and comparing the hash with the hash provided in the block header.
	 * 
	 * @param blockHeader
	 * @return
	 */
	private boolean validatePreviousBlockHash(Block block) {

		String previousBlockHash = SHA256Hasher.returnSHA256HashStringFromString(database
				.getBlockWithInputIndex(block.getIndex() - 1).getBlockHeader().getHeaderDataForHashCalculation());

		return previousBlockHash.equalsIgnoreCase(block.getBlockHeader().getPreviousBlockHash());
	}

	/**
	 * Validating the merkle root in the block by calculating the merkle root for
	 * the transaction list provided in the block and comparing it with the provided
	 * merkle root in the block.
	 * 
	 * @param block
	 * @return
	 */
	private boolean validateMerkleRoot(Block block) {

		MerkleTree merkleTree = new MerkleTree(block.getTransactionList());

		return merkleTree.getMerkleRoot().equalsIgnoreCase(block.getBlockHeader().getMerkleRootHash());
	}

	/**
	 * Verify that transaction sender has enough balance to execute this
	 * transaction.
	 * 
	 * @param transaction
	 * @return
	 */
	private boolean transactionSenderHasEnoughBalance(Transaction transaction) {

		return transaction.getAmount() <= (getTransactionSenderBlockchainBalance(transaction)
				- getTransactionSenderPendingTransactionsAmount(transaction));
	}

	/**
	 * Returns amount of all transactions for the transaction sender waiting in the
	 * transaction pool to be added to a block.
	 * 
	 * @param transaction
	 * @return
	 */
	private double getTransactionSenderPendingTransactionsAmount(Transaction transaction) {

		double pendingAmount = 0;

		for (Transaction t : database.getAllTransactionsFromTransactionPool()) {

			if (t.getFromAdress().equalsIgnoreCase(transaction.getFromAdress())) {

				pendingAmount += t.getAmount();
			}
		}

		return pendingAmount;
	}

	/**
	 * Returns amount of all transactions for the transaction sender that have been
	 * added to a block/blockchain.
	 * 
	 * @param transaction
	 * @return
	 */
	private double getTransactionSenderBlockchainBalance(Transaction transaction) {

		double senderOut = 0;
		double senderIn = 0;

		for (Block block : nodeController.getBlockchainController().getBlockchain().getChain()) {

			for (Transaction t : block.getTransactionList()) {

				if (t.getToAdress().equalsIgnoreCase("balance")
						|| t.getToAdress().equalsIgnoreCase(transaction.getFromAdress())) {

					senderIn += t.getAmount();
				}

				if (t.getFromAdress().equalsIgnoreCase(transaction.getFromAdress())) {

					senderOut += t.getAmount();
				}
			}

		}

		return senderIn - senderOut;
	}

	/**
	 * Verifies the transaction signature by using the transaction sender public key
	 * from the database and the transactionID which has been signed by the senders
	 * private key.
	 * 
	 * @param transaction
	 * @return
	 */
	private synchronized boolean verifyTransactionSignature(Transaction transaction) {

		if (transaction == null)
			throw new RuntimeException("To verify a transaction, it can´t be null");

		boolean validSignature = false;

		if (verifyTransactionID(transaction)) {

			try {

				Signature signature = Signature.getInstance("SHA256withECDSA", "SunEC");

				signature.initVerify(keyGenerator.byteArrayToPublicKey(keyGenerator.hexStringToByteArray(
						database.getHexStringPublicKeyForCorrespondingWalletAddress(transaction.getFromAdress()))));

				byte[] stringToVerify = transaction.getTransactionID().getBytes("UTF-8");
				signature.update(stringToVerify);

				validSignature = signature.verify(keyGenerator.hexStringToByteArray(transaction.getSignature()));

			} catch (Exception e) {

				logger.error("Signature verification exception", e);

			}

			return validSignature;

		} else

		{

			return validSignature;

		}
	}

	/**
	 * Verify that the transactionID has been correctly calculated by hashing the
	 * transaction data to generate a transaction ID and comparing it to the
	 * transaction ID from the transaction object.
	 * 
	 * @param transaction
	 * @return
	 */
	private synchronized boolean verifyTransactionID(Transaction transaction) {

		String recalculatedTransactionID = SHA256Hasher
				.returnSHA256HashStringFromString(transaction.getTransactionIdData());

		if (recalculatedTransactionID.equals(transaction.getTransactionID())) {

			return true;

		} else {

			return false;
		}
	}

}
