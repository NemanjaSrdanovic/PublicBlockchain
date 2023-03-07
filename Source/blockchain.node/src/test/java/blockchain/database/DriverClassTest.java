package blockchain.database;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import blockchain.block.Block;
import blockchain.block.BlockHeader;
import blockchain.block.MerkleTree;
import blockchain.block.Transaction;
import blockchain.concensus.SHA256Hasher;
import blockchain.wallet.model.Wallet;

/**
 * Testing the functionalities of the DriverClass object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class DriverClassTest {

	private static DriverClass testDatabase;
	private static BlockHeader testBlockHeader;
	private static BlockHeader secoundTestBlockHeader;
	private static BlockHeader thirdTestBlockHeader;
	private static Block testBlock;
	private static Block secondTestBlock;
	private static Block thirdTestBlock;
	private static Transaction testTransaction;
	private static Transaction secoundTestTransaction;
	private static ArrayList<Transaction> testTransactionList;
	private static ArrayList<Transaction> secoundTestTransactionList;
	private static Wallet testWallet;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testTransaction = new Transaction("xy", "xz", 1000);
		testTransaction.setTimeStamp("2022.01.11.11:00:00");
		testTransactionList = new ArrayList<>(Arrays.asList(testTransaction));
		testBlockHeader = new BlockHeader("0", new MerkleTree(testTransactionList).getMerkleRoot(), 4);
		testBlock = new Block(0, testBlockHeader, testTransactionList, testTransactionList.size());

		secoundTestTransaction = new Transaction("xz", "xy", 800);
		secoundTestTransaction.setTimeStamp("2022.01.11.11:00:01");
		secoundTestTransactionList = new ArrayList<>(Arrays.asList(secoundTestTransaction));
		secoundTestBlockHeader = new BlockHeader(
				SHA256Hasher.returnSHA256HashStringFromString(testBlockHeader.getHeaderDataForHashCalculation()),
				new MerkleTree(secoundTestTransactionList).getMerkleRoot(), 4);
		secondTestBlock = new Block(1, secoundTestBlockHeader, secoundTestTransactionList,
				secoundTestTransactionList.size());

		thirdTestBlockHeader = new BlockHeader(
				SHA256Hasher.returnSHA256HashStringFromString(testBlockHeader.getHeaderDataForHashCalculation()),
				new MerkleTree(secoundTestTransactionList).getMerkleRoot(), 4);
		thirdTestBlock = new Block(1, thirdTestBlockHeader, secoundTestTransactionList,
				secoundTestTransactionList.size());

		testWallet = new Wallet();

	}

	/**
	 * Executed before each tests in this class to prepare dependencies
	 */
	@Before
	public void setUp() {

		testDatabase = new DriverClass(3070);

	}

	/**
	 * Testing the DriverClass object insert methods by mocking objects that should
	 * be inserted and calling methods that insert those data into the DB. When
	 * fetching the data from those tables the result can't be null. The methods
	 * should process without any exceptions.
	 */
	@Test
	public void insertDataIntoExistingTables_RecreateDriverClassAndSelectAllTables_EmptyObjectsReturned() {

		testDatabase.insertBlockIntoBlockchain(testBlock);
		testDatabase.insertTransactionIntoTransactionPool(testTransaction);
		testDatabase.insertWalletKeyIntoDatabase(testWallet.getWalletAddress(), testWallet.getHexStringPublicKey());

		testDatabase = new DriverClass(3070);

		assertTrue(testDatabase.getLastBlockFromBlockchain() == null);
		assertTrue(testDatabase.getAllTransactionsFromTransactionPool().size() == 0);
		assertTrue(testDatabase.getAllPublicKeysFromRegister().size() == 0);
	}

	/**
	 * Testing the DriverClass object method
	 * getAllTransactionsFromTransactionPoolWhereTimestampBefore() by inserting two
	 * transactions into the DB with different time stamps and calling the method
	 * with a argument that should only return one of those transactions. The
	 * returned transaction data should match with the mocked transaction data and
	 * the process should return without any executions.
	 */
	@Test
	public void insertTransactionIntoExistingTable_SelectAllTransactionsFromTableAndAllBeforeTime_SameTransactionID() {

		testDatabase.insertTransactionIntoTransactionPool(testTransaction);
		testDatabase.insertTransactionIntoTransactionPool(secoundTestTransaction);

		assertTrue(testDatabase
				.getAllTransactionsFromTransactionPoolWhereTimestampBefore(secoundTestTransaction.getTimeStamp()).get(0)
				.getTransactionID().equals(testTransaction.getTransactionID()));

		Transaction selectedTransaction = testDatabase.getAllTransactionsFromTransactionPool().get(0);

		assertTrue(selectedTransaction.getTransactionID().equals(testTransaction.getTransactionID())
				&& selectedTransaction.getTimeStamp().equals(testTransaction.getTimeStamp())
				&& selectedTransaction.getFromAdress().equals(testTransaction.getFromAdress())
				&& selectedTransaction.getAmount() == (testTransaction.getAmount())
				&& selectedTransaction.getSignature().equals(testTransaction.getSignature())
				&& selectedTransaction.getToAdress().equals(testTransaction.getToAdress()));
	}

	/**
	 * Testing the DriverClass object get.. methods by inserting two Block objects
	 * into the DB and calling the methods with arguments that should only return
	 * one of those block or in specific order. The returned block data should match
	 * with the mocked block data and the process should return without any
	 * executions.
	 */
	@Test
	public void insertMultipleBlocksIntoExistingTable_SelectLastBlockFromTableAndAllBlocks_SameBlockIndexAsLastInsertedBlock() {

		testDatabase.insertBlockIntoBlockchain(testBlock);
		testDatabase.insertBlockIntoBlockchain(secondTestBlock);

		assertTrue(testDatabase.getAllBlocksFromBlockchain().getChain().get(0).getTimeStamp()
				.equals(testBlock.getTimeStamp()));

		assertTrue(testDatabase.getLastBlockFromBlockchain().getTimeStamp().equals(secondTestBlock.getTimeStamp()));

		Block selectedBlock = testDatabase.getBlockWithInputIndex(0);

		assertTrue(selectedBlock.getTimeStamp().equals(testBlock.getTimeStamp())
				&& selectedBlock.getIndex() == testBlock.getIndex()
				&& selectedBlock.getTransactionCounter() == testBlock.getTransactionCounter()
				&& selectedBlock.getTransactionList().get(0).getTransactionID()
						.equals(testBlock.getTransactionList().get(0).getTransactionID())
				&& selectedBlock.getBlockHeader().getTimeStamp().equals(testBlock.getBlockHeader().getTimeStamp()));
	}

	/**
	 * Testing DriverClass object insertWalletKeyIntoDatabase(..) method by mocking
	 * the insert arguments. When the data from the existing is fetched it should
	 * return data which matches with those mocked.
	 */
	@Test
	public void insertWalletKeyIntoExistingTable_SelectKeyForWalletAddress_SameKeyAsWalletKey() {

		testDatabase.insertWalletKeyIntoDatabase(testWallet.getWalletAddress(), testWallet.getHexStringPublicKey());

		assertTrue(testDatabase.getHexStringPublicKeyForCorrespondingWalletAddress(testWallet.getWalletAddress())
				.equals(testWallet.getHexStringPublicKey()));

		assertTrue(testDatabase.getAllPublicKeysFromRegister().get(testWallet.getWalletAddress())
				.equals(testWallet.getHexStringPublicKey()));
	}

	/**
	 * Testing DriverClass object replaceBlockFromBlockchain(..) method by inserting
	 * mocked data into all corresponding tables and calling the method with a new
	 * Block object that should change the data in all the corresponding tables.
	 */
	@Test
	public void replaceBlockFromBlockchain_MoveMinedTransactionsAndOldBlock_BlockInChainUpdated() {

		testDatabase.insertTransactionIntoTransactionPool(testTransaction);
		testDatabase.insertBlockIntoBlockchain(testBlock);
		testDatabase.moveMinedTransactionsFromPool(testBlock);

		testDatabase.insertTransactionIntoTransactionPool(secoundTestTransaction);
		testDatabase.insertBlockIntoBlockchain(secondTestBlock);
		testDatabase.moveMinedTransactionsFromPool(secondTestBlock);

		testDatabase.replaceBlockFromBlockchain(thirdTestBlock);

		assertTrue(testDatabase.getBlockWithInputIndex(secondTestBlock.getIndex()).getTimeStamp()
				.equals(thirdTestBlock.getTimeStamp()));

	}
}
