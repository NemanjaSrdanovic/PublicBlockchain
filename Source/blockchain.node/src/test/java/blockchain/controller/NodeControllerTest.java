package blockchain.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.database.DriverClass;
import blockchain.wallet.model.Wallet;
import connection.Connection;
import controllers.ConnectionHandler;
import enumerations.EMessageEndpoint;
import messages.Message;
import udp_connection.UDP_Client;

/**
 * Testing the functionalities of the NodeController object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class NodeControllerTest {

	private static NodeController testNodeController;
	private static ConnectionHandler testConnectionHandler;
	private static MessageController testMessageController;
	private static Connection testConnection;
	private static UDP_Client testUDPClient;
	private static Wallet testWallet;
	private static Set<String> testReceivedMessages;
	private static DriverClass testDatabase;

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
		testConnectionHandler = Mockito.mock(ConnectionHandler.class);
		testMessageController = Mockito.mock(MessageController.class);
		testConnection = Mockito.mock(Connection.class);
		testUDPClient = Mockito.mock(UDP_Client.class);
		testWallet = Mockito.mock(Wallet.class);
		testReceivedMessages = new HashSet<String>();
		testDatabase = Mockito.mock(DriverClass.class);

		Mockito.when(testNodeController.getWallet()).thenReturn(testWallet);
		Mockito.when(testWallet.getWalletAddress()).thenReturn("xy");
		Mockito.when(testWallet.getHexStringPublicKey()).thenReturn("a93fke3u3au9r3adf");

		Mockito.when(testNodeController.getMessageController()).thenReturn(testMessageController);
		Mockito.when(testNodeController.getConnectionHandler()).thenReturn(testConnectionHandler);
		Mockito.when(testConnectionHandler.getConnection()).thenReturn(testConnection);
		Mockito.when(testConnection.getClient()).thenReturn(testUDPClient);

		Mockito.doNothing().when(testUDPClient).addMessage(Mockito.any(Message.class));
		Mockito.when(testMessageController.getReceivedMessages()).thenReturn(testReceivedMessages);

		Mockito.when(testNodeController.getDatabase()).thenReturn(testDatabase);
		Mockito.when(testDatabase.getAllTransactionsFromTransactionPool()).thenReturn(null);
		Mockito.when(testDatabase.getAllBlocksFromBlockchain()).thenReturn(null);
		Mockito.when(testDatabase.getAllPublicKeysFromRegister()).thenReturn(null);

		Mockito.doCallRealMethod().when(testNodeController).sendWalletData();
		Mockito.doCallRealMethod().when(testNodeController).requestNetworkData();
		Mockito.doCallRealMethod().when(testNodeController).sendCurrentNodeData(Mockito.anyString());
		Mockito.doCallRealMethod().when(testNodeController)
				.broadcastNewlyMinedBlockToTheNetwork(Mockito.any(Block.class));
		Mockito.doCallRealMethod().when(testNodeController).isSecondDateAfterFirstDate(Mockito.anyString(),
				Mockito.anyString());

	}

	/**
	 * Testing the NodeController object method sendWalletData(..) by catching the
	 * generated Message object and verifying that the object has the correct
	 * endpoint. The process should be executed without throwing a execution and the
	 * object forwarded to the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void sendWalletDataCalled_MessageContainingPublicKeySend() throws InterruptedException {

		testNodeController.sendWalletData();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testReceivedMessages.size() > 0);

		testReceivedMessages.clear();

		assertTrue(capturedMessage.getValue().getEndpoint().equals(EMessageEndpoint.PublicKey));

	}

	/**
	 * Testing the NodeController object method requestNetworkData(..) by catching
	 * the generated Message object and verifying that the object has the correct
	 * endpoint. The process should be executed without throwing a execution and the
	 * object forwarded to the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void requestNetworkDataCalled_MessageContainingDataRequestSend() throws InterruptedException {

		testNodeController.requestNetworkData();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testReceivedMessages.size() > 0);

		testReceivedMessages.clear();

		assertTrue(capturedMessage.getValue().getEndpoint().equals(EMessageEndpoint.DataRequest));
	}

	/**
	 * Testing the NodeController object method sendCurrentNodeData(..) by catching
	 * the generated Message object and verifying that the object has the correct
	 * endpoint. The process should be executed without throwing a execution and the
	 * object forwarded to the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void sendCurrentNodeDataCalled_MessageContainingDataResponseSend() throws InterruptedException {

		testNodeController.sendCurrentNodeData("xz");

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testReceivedMessages.size() > 0);

		testReceivedMessages.clear();

		assertTrue(capturedMessage.getValue().getEndpoint().equals(EMessageEndpoint.DataResponse));

	}

	/**
	 * Testing the NodeController object method
	 * broadcastNewlyMinedBlockToTheNetwork(..) by catching the generated Message
	 * object and verifying that the object has the correct endpoint. The process
	 * should be executed without throwing a execution and the object forwarded to
	 * the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void broadcastNewlyMinedBlockToTheNetworkCalled_MessageContainingBlockSend() throws InterruptedException {

		testNodeController.broadcastNewlyMinedBlockToTheNetwork(new Block(4, null, null, 2));

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testReceivedMessages.size() > 0);

		testReceivedMessages.clear();

		assertTrue(capturedMessage.getValue().getEndpoint().equals(EMessageEndpoint.Block));
	}

	/**
	 * Testing the NodeController object method isSecondDateAfterFirstDate(..) by
	 * providing arguments where false should be returned.
	 */
	@Test
	public void isSecondDateAfterFirstDateCalled_ReturnsFalse() {

		assertFalse(testNodeController.isSecondDateAfterFirstDate("2022.06.12.15:00:54", "2022.06.12.15:00:56"));
	}

	/**
	 * Testing the NodeController object method isSecondDateAfterFirstDate(..) by
	 * providing arguments where true should be returned.
	 */
	@Test
	public void isSecondDateAfterFirstDateCalled_ReturnsTrue() {

		assertTrue(testNodeController.isSecondDateAfterFirstDate("2022.06.12.15:00:54.124", "2022.06.12.15:00:56.321"));
	}
}
