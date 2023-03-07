package blockchain.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.block.Transaction;
import blockchain.chain.BlockchainController;
import blockchain.database.DriverClass;
import blockchain.wallet.model.Wallet;
import connection.Connection;
import enumerations.EMessageEndpoint;
import messages.Message;
import node.NodeData;
import udp_connection.UDP_Client;

/**
 * Testing the functionalities of the MessageWorker object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class MessageWorkerTest {

	private static MessageController testMessageController;
	private static NodeController testNodeController;
	private static Connection testConnection;
	private static UDP_Client testUDPClient;
	private static Wallet testWallet;
	private static BlockingQueue<Message> testMessages;
	private static DriverClass testDatabase;
	private static VerificationController testVerificationController;
	private static BlockchainController testBlockchainController;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testMessages = new LinkedBlockingQueue<Message>();
		testMessageController = Mockito.mock(MessageController.class);
		testNodeController = Mockito.mock(NodeController.class);
		testConnection = Mockito.mock(Connection.class);
		testUDPClient = Mockito.mock(UDP_Client.class);
		testWallet = Mockito.mock(Wallet.class);
		testDatabase = Mockito.mock(DriverClass.class);
		testVerificationController = Mockito.mock(VerificationController.class);
		testBlockchainController = Mockito.mock(BlockchainController.class);

		Mockito.when(testMessageController.getMessages()).thenReturn(testMessages);

		Mockito.when(testMessageController.getNodeController()).thenReturn(testNodeController);

		Mockito.doNothing().when(testNodeController).sendCurrentNodeData(Mockito.anyString());
		Mockito.when(testMessageController.getConnection()).thenReturn(testConnection);
		Mockito.when(testConnection.getClient()).thenReturn(testUDPClient);

		Mockito.doNothing().when(testUDPClient).addMessage(Mockito.any(Message.class));

		Mockito.when(testNodeController.getWallet()).thenReturn(testWallet);

		Mockito.when(testWallet.getWalletAddress()).thenReturn("xy");

		Mockito.doNothing().when(testMessageController).insertResponseNodeData(Mockito.any(NodeData.class));

		Mockito.when(testNodeController.getDatabase()).thenReturn(testDatabase);

		Mockito.doNothing().when(testDatabase).insertWalletKeyIntoDatabase(Mockito.anyString(), Mockito.anyString());

		Mockito.when(testNodeController.isStartNodeSynchronised()).thenReturn(true);

		Mockito.when(testNodeController.getVerificationController()).thenReturn(testVerificationController);

		Mockito.when(testVerificationController.verifyTransaction(Mockito.any(Transaction.class))).thenReturn(true);

		Mockito.when(testVerificationController.verifyBlock(Mockito.any(Block.class))).thenReturn(true);

		Mockito.when(testNodeController.getBlockchainController()).thenReturn(testBlockchainController);

		Mockito.doNothing().when(testBlockchainController).resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(
				Mockito.any(Block.class), Mockito.anyBoolean());
	}

	/**
	 * Testing the MessageWorker object method run() and corresponding switch method
	 * by passing a mocked Message object which contains the Endpoint:DataRequest
	 * that triggers the sendCurrentNodeData() method. The message hand over to the
	 * method is captured and verified that it is the same as the mocked object.
	 */
	@Test
	public void mockDataRequestMessage_runMessageWorker_sendCurrentNodeDataCalled() {

		Message dataRequestMessage = new Message("xy", "xz", EMessageEndpoint.DataRequest, null);

		testMessages.add(dataRequestMessage);

		MessageWorker testWorker = new MessageWorker(testMessageController);

		testWorker.run();

		ArgumentCaptor<String> capturedSenderNode = ArgumentCaptor.forClass(String.class);

		Mockito.verify(testNodeController).sendCurrentNodeData(capturedSenderNode.capture());

		assertTrue(capturedSenderNode.getValue().equals(dataRequestMessage.getSenderNode()));

	}

	/**
	 * Testing the MessageWorker object method run() and corresponding switch method
	 * by passing a mocked Message object which contains the Endpoint:DataResponse
	 * that triggers the insertResponseNodeData() method. The message hand over to
	 * the method is captured and verified that it is the same as the mocked object.
	 */
	@Test
	public void mockDataResponseMessage_runMessageWorker_insertResponseNodeDataCalled() {

		NodeData testNode = new NodeData(null, null, null);

		Message dataResponseMessage = new Message("xy", "xz", EMessageEndpoint.DataResponse, testNode);

		testMessages.add(dataResponseMessage);

		MessageWorker testWorker = new MessageWorker(testMessageController);

		testWorker.run();

		ArgumentCaptor<NodeData> capturedNodeData = ArgumentCaptor.forClass(NodeData.class);

		Mockito.verify(testMessageController, atLeast(1)).insertResponseNodeData(capturedNodeData.capture());

		assertTrue(capturedNodeData.getValue().equals(testNode));
	}

	/**
	 * Testing the MessageWorker object method run() and corresponding switch method
	 * by passing a mocked Message object which contains the Endpoint:PublicKey that
	 * triggers the insertWalletKeyIntoDatabase() method. The message hand over to
	 * the method is captured and verified that it is the same as the mocked object.
	 */
	@Test
	public void mockPublicKeyMessage_runMessageWorker_insertWalletKeyIntoDatabaseCalled() {

		String walletPublicKey = "4kjg43k32ho2d4fahfo44kfjkajdf4bcjfe56344";
		Message publicKeyMessage = new Message("xy", "xz", EMessageEndpoint.PublicKey, walletPublicKey);

		testMessages.add(publicKeyMessage);

		MessageWorker testWorker = new MessageWorker(testMessageController);

		testWorker.run();

		ArgumentCaptor<String> capturedWalletKey = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> capturedWalletAddress = ArgumentCaptor.forClass(String.class);

		Mockito.verify(testDatabase, atLeast(1)).insertWalletKeyIntoDatabase(capturedWalletAddress.capture(),
				capturedWalletKey.capture());

		assertTrue(capturedWalletAddress.getValue().equals(publicKeyMessage.getSenderNode()));
		assertTrue(capturedWalletKey.getValue().equals(walletPublicKey));
	}

	/**
	 * Testing the MessageWorker object method run() and corresponding switch method
	 * by passing a mocked Message object which contains the Endpoint:Transaction
	 * that triggers the insertTransactionIntoTransactionPool() method. The message
	 * hand over to the method is captured and verified that it is the same as the
	 * mocked object.
	 */
	@Test
	public void mockTransactionMessage_runMessageWorker_insertTransactionIntoTransactionPoolCalled() {

		Transaction t = new Transaction("xy", "xz", 1000);

		Message transactionMessage = new Message("xy", "xz", EMessageEndpoint.Transaction, t);

		testMessages.add(transactionMessage);

		MessageWorker testWorker = new MessageWorker(testMessageController);

		testWorker.run();

		ArgumentCaptor<Transaction> capturedTransaction = ArgumentCaptor.forClass(Transaction.class);

		Mockito.verify(testDatabase, atLeast(1)).insertTransactionIntoTransactionPool(capturedTransaction.capture());

		assertTrue(capturedTransaction.getValue().equals(t));
	}

	/**
	 * Testing the MessageWorker object method run() and corresponding switch method
	 * by passing a mocked Message object which contains the Endpoint:Block that
	 * triggers the resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain()
	 * method. The message hand over to the method is captured and verified that it
	 * is the same as the mocked object.
	 */
	@Test
	public void mockBlockMessage_runMessageWorker_resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchainCalled() {

		Block b = new Block(3, null, null, 3);

		Message blockMessage = new Message("xy", "xz", EMessageEndpoint.Block, b);

		testMessages.add(blockMessage);

		MessageWorker testWorker = new MessageWorker(testMessageController);

		testWorker.run();

		ArgumentCaptor<Block> capturedBlock = ArgumentCaptor.forClass(Block.class);
		ArgumentCaptor<Boolean> capturedBool = ArgumentCaptor.forClass(Boolean.class);

		Mockito.verify(testBlockchainController, atLeast(1))
				.resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(capturedBlock.capture(),
						capturedBool.capture());

		assertTrue(capturedBlock.getValue().equals(b));

	}
}
