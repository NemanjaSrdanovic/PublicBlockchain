package blockchain.controller;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;
import blockchain.database.DriverClass;
import connection.Connection;
import controllers.ConnectionHandler;
import node.NodeData;

/**
 * Testing the functionalities of the MessageController object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class MessageControllerTest {

	private static NodeData fullNodeData;
	private static Connection testConnection;
	private static NodeController testNodeController;
	private static ConnectionHandler testConnectionHandler;
	private static DriverClass testDatabase;
	private static MessageController testMessageController;
	private static Block testBlock;
	private static Transaction testTransaction;
	private static String testWalletKey;
	private static String testWalletValue;

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
		testConnection = Mockito.mock(Connection.class);
		testConnectionHandler = Mockito.mock(ConnectionHandler.class);
		testDatabase = Mockito.mock(DriverClass.class);

		Mockito.when(testNodeController.getConnectionHandler()).thenReturn(testConnectionHandler);
		Mockito.when(testConnectionHandler.getConnection()).thenReturn(testConnection);
		Mockito.when(testNodeController.getDatabase()).thenReturn(testDatabase);

		Mockito.doNothing().when(testDatabase).insertTransactionIntoTransactionPool(Mockito.any(Transaction.class));
		Mockito.doNothing().when(testDatabase).insertWalletKeyIntoDatabase(Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(testDatabase).insertBlockIntoBlockchain(Mockito.any(Block.class));
		Mockito.doNothing().when(testDatabase).moveMinedTransactionsFromPool(Mockito.any(Block.class));
		Mockito.doNothing().when(testNodeController).setLastNodeDataSynchronisationTime(Mockito.anyString());

		testTransaction = new Transaction("xy", "xz", 1000);
		testBlock = new Block(3, null, null, 1);
		testWalletKey = "xy";
		testWalletValue = "ab472madk249586bad22hflk3h5a";

		ArrayList<Transaction> transactionPool = new ArrayList<>(Arrays.asList(testTransaction));
		HashMap<String, String> walletsData = new HashMap<>();
		walletsData.put(testWalletKey, testWalletValue);
		Blockchain blockchain = new Blockchain();
		blockchain.addBlockToBlockchain(testBlock);

		fullNodeData = new NodeData(transactionPool, walletsData, blockchain);

		testMessageController = new MessageController(testNodeController);

	}

	/**
	 * Running exactly once during the test run - at the very end of the test
	 * execution to clear all states of the test execution.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		fullNodeData = null;

	}

	/**
	 * Testing the MessageController object method insertResponseNodeData(..) by
	 * passing a mocked NodeData object to the method and verifying that all methods
	 * corresponding to that methods are called and the passed data from the
	 * NodeData object used for that methods.
	 */
	@Test
	public void mockNodeData_callInsertResponseNodeData_AllDataExtracted() {

		testMessageController.insertResponseNodeData(fullNodeData);

		ArgumentCaptor<Block> capturedBlock = ArgumentCaptor.forClass(Block.class);
		ArgumentCaptor<Transaction> capturedTransaction = ArgumentCaptor.forClass(Transaction.class);
		ArgumentCaptor<String> capturedWalletKey = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> capturedWalletValue = ArgumentCaptor.forClass(String.class);

		Mockito.verify(testDatabase).insertTransactionIntoTransactionPool(capturedTransaction.capture());
		Mockito.verify(testDatabase).insertWalletKeyIntoDatabase(capturedWalletKey.capture(),
				capturedWalletValue.capture());
		Mockito.verify(testDatabase).insertBlockIntoBlockchain(capturedBlock.capture());

		assertTrue(capturedBlock.getValue().equals(testBlock));
		assertTrue(capturedTransaction.getValue().equals(testTransaction));
		assertTrue(capturedWalletKey.getValue().equals(testWalletKey));
		assertTrue(capturedWalletValue.getValue().equals(testWalletValue));

	}

}
