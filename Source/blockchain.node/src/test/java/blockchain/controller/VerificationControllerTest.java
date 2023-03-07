package blockchain.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.block.BlockHeader;
import blockchain.block.MerkleTree;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;
import blockchain.chain.BlockchainController;
import blockchain.concensus.PoW;
import blockchain.concensus.SHA256Hasher;
import blockchain.database.DriverClass;
import blockchain.wallet.model.Wallet;

/**
 * Testing the functionalities of the VerificationController object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class VerificationControllerTest {

	private static NodeController testNodeController;
	private static Wallet testWallet;
	private static DriverClass testDatabase;
	private static BlockchainController testBlockchainController;
	private static PoW testConcensusAlg;
	private static VerificationController testVerificationController;
	private static LinkedList<Block> testChain;
	private static Block testGenesisBlock;
	private static Transaction genesisTransaction;
	private static Blockchain testBlockchain;
	private static BlockHeader testHeader;
	private static ArrayList<Transaction> blockTransactions;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testNodeController = Mockito.mock(NodeController.class);
		testWallet = new Wallet();
		testDatabase = Mockito.mock(DriverClass.class);
		testBlockchainController = Mockito.mock(BlockchainController.class);
		testConcensusAlg = Mockito.mock(PoW.class);

		Mockito.when(testNodeController.getWallet()).thenReturn(testWallet);

		genesisTransaction = new Transaction("systemWallet", "balance", 1000);
		genesisTransaction.setTimeStamp("2022.01.09.12:00:00");
		genesisTransaction.setTransactionID(genesisTransaction.calculateTransactionID());
		ArrayList<Transaction> transactionList = new ArrayList<>(Arrays.asList(genesisTransaction));
		BlockHeader testGenesisHeader = new BlockHeader("0", "0", 4);
		testGenesisHeader.setTimeStamp("2022.01.09.12:00:00");
		testGenesisBlock = new Block(0, testGenesisHeader, transactionList, 1);
		testGenesisBlock.setTimeStamp("2022.01.09.12:00:00");

		Transaction t2 = new Transaction("xy", "xz", 200);
		t2.setTimeStamp("2022.01.09.12:00:00");
		t2.setTransactionID(t2.calculateTransactionID());
		blockTransactions = new ArrayList<>(Arrays.asList(t2));
		MerkleTree testTree = new MerkleTree(blockTransactions);
		testHeader = new BlockHeader(
				SHA256Hasher.returnSHA256HashStringFromString(
						testGenesisBlock.getBlockHeader().getHeaderDataForHashCalculation()),
				testTree.getMerkleRoot(), 4);
		testHeader.setTimeStamp("2022.01.09.12:00:00");

		testChain = new LinkedList<Block>();
		testChain.add(testGenesisBlock);
		testBlockchain = new Blockchain();
		testBlockchain.addBlockToBlockchain(testGenesisBlock);

		Mockito.when(testNodeController.getDatabase()).thenReturn(testDatabase);
		Mockito.when(testNodeController.getBlockchainController()).thenReturn(testBlockchainController);
		Mockito.when(testBlockchainController.getConsensusAlgorithm()).thenReturn(testConcensusAlg);
		Mockito.when(testConcensusAlg.hasHashTheCorrectDifficulty(Mockito.anyString())).thenCallRealMethod();

		Mockito.when(testBlockchainController.getBlockchain()).thenReturn(testBlockchain);

		Mockito.when(testDatabase.getAllTransactionsFromTransactionPool()).thenReturn(new ArrayList<Transaction>());
		Mockito.when(testDatabase.getHexStringPublicKeyForCorrespondingWalletAddress(Mockito.anyString()))
				.thenReturn(testWallet.getHexStringPublicKey());

		Mockito.when(testDatabase.getBlockWithInputIndex(Mockito.anyInt())).thenReturn(testGenesisBlock);

		testVerificationController = new VerificationController(testNodeController);

	}

	/**
	 * Testing the VerificationController method verifyTransaction() by providing a
	 * mocked Transaction object which has all correct data and sufficient balance
	 * for the sending wallet. The verification should be executed without any
	 * exception and return true.
	 */
	@Test
	public void callVerifyTransaction_InsertCorrectTransaction_ReturnedTrue() {

		Transaction t1 = new Transaction("xy", "xz", 100);
		t1.setSignature(testWallet.signTransaction(t1.getTransactionID()));

		assertTrue(testVerificationController.verifyTransaction(t1));
	}

	/**
	 * Testing the VerificationController method verifyTransaction() by providing a
	 * mocked Transaction object which has all correct data but insufficient balance
	 * for the sending wallet. The verification should be executed without any
	 * exception and return false.
	 */
	@Test
	public void callVerifyTransaction_AmountHigherThanBalance_ReturnedFalse() {

		Transaction t1 = new Transaction("xy", "xz", 1001);
		t1.setSignature(testWallet.signTransaction(t1.getTransactionID()));

		assertFalse(testVerificationController.verifyTransaction(t1));
	}

	/**
	 * Testing the VerificationController method verifyTransaction() by providing a
	 * mocked Transaction object which has a corrupted signature.The verification
	 * should be executed without any exception and return false.
	 */
	@Test
	public void callVerifyTransaction_SignatureNotCorrect_ReturnedFalse() {

		Transaction t1 = new Transaction("xy", "xz", 1001);
		t1.setSignature(testWallet.signTransaction("wrongSignature"));

		assertFalse(testVerificationController.verifyTransaction(t1));
	}

	/**
	 * Testing the VerificationController method verifyBlock() by providing a mocked
	 * Block object which has all correct data. The verification should be executed
	 * without any exception and return true.
	 */
	@Test
	public void callVerifyBlock_InsertCorrectBlock_ReturnedTrue() {

		// 9058 long

		testHeader.setNounce(86636);

		Block validBlock = new Block(1, testHeader, blockTransactions, 1);

		assertTrue(testVerificationController.verifyBlock(validBlock));
	}

	/**
	 * Testing the VerificationController method verifyBlock() by providing a mocked
	 * Block object which has nounce that when hashed with the BlockHeader returns a
	 * hash the has not a correct difficulty. The verification should be executed
	 * without any exception and return false.
	 */
	@Test
	public void callVerifyBlock_isBlockHashValidFalse_ReturnedFalse() {

		testHeader.setNounce(86635);

		Block validBlock = new Block(1, testHeader, blockTransactions, 1);

		assertFalse(testVerificationController.verifyBlock(validBlock));
	}

	/**
	 * Testing the VerificationController method verifyBlock() by providing a mocked
	 * Block object which has saved a previousBlockHash that does't match with the
	 * correct hash from the previous block in blockchain. The verification should
	 * be executed without any exception and return false.
	 */
	@Test
	public void callVerifyBlock_isPreviousBlockHashValidFalse_ReturnedFalse() {

		testHeader.setNounce(86636);

		BlockHeader wrongHeader = new BlockHeader("wrongPreviousHahs",
				new MerkleTree(blockTransactions).getMerkleRoot(), 4);

		Block validBlock = new Block(1, wrongHeader, blockTransactions, 1);

		assertFalse(testVerificationController.verifyBlock(validBlock));
	}

	/**
	 * Testing the VerificationController method verifyBlock() by providing a mocked
	 * Block object which has saved a BlockHeader that contains a merkleRoot that
	 * doesn't match with the merkleRoot calculated from the transaction list
	 * contained in the block. The verification should be executed without any
	 * exception and return false.
	 */
	@Test
	public void callVerifyBlock_isMerkleRootValidFalse_ReturnedFalse() {

		testHeader.setNounce(86636);

		BlockHeader wrongHeader = new BlockHeader(SHA256Hasher.returnSHA256HashStringFromString(
				testGenesisBlock.getBlockHeader().getHeaderDataForHashCalculation()), "wrongRoot", 4);

		Block validBlock = new Block(1, wrongHeader, blockTransactions, 1);

		assertFalse(testVerificationController.verifyBlock(validBlock));
	}
}
