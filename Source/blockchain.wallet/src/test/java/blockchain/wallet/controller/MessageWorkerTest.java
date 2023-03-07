package blockchain.wallet.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.chain.Blockchain;
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

	private static WalletController testWalletController;
	private static Connection testConnection;
	private static UDP_Client testUDPClient;
	private static MessageWorker testMessageWorker;
	private static Message testDataResponseMessage;
	private static Message testDataRequestMessage;
	private static NodeData testNodeData;
	private static BlockingQueue<Message> testMessages;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testWalletController = Mockito.mock(WalletController.class);
		testConnection = Mockito.mock(Connection.class);
		testUDPClient = Mockito.mock(UDP_Client.class);

		testNodeData = new NodeData(new ArrayList<>(), new HashMap<String, String>(), new Blockchain());
		testDataResponseMessage = new Message("xy", "xz", EMessageEndpoint.DataResponse, testNodeData);
		testDataRequestMessage = new Message("xz", "xy", EMessageEndpoint.DataRequest, null);
		testMessages = new LinkedBlockingQueue<Message>();

		Mockito.when(testWalletController.getMessages()).thenReturn(testMessages);
		Mockito.doNothing().when(testWalletController).insertResponseNodeData(Mockito.any(NodeData.class));
		Mockito.when(testWalletController.getConnection()).thenReturn(testConnection);
		Mockito.when(testConnection.getClient()).thenReturn(testUDPClient);
		Mockito.doNothing().when(testUDPClient).addMessage(Mockito.any(Message.class));

		testMessageWorker = new MessageWorker(testWalletController);
	}

	/**
	 * Testing the MessageWorker object insertResponseNodeData(..) method by mocking
	 * a Message object containing NodeData and capturing the data from
	 * corresponding methods called by the insert.. method. The captured data should
	 * match with the mocked data and the process should not throw any exception.
	 * 
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void runMessageWorker_mockMessageWithDataResponse_insertedResponseNodeData() throws InterruptedException {

		testMessages.add(testDataResponseMessage);

		testMessageWorker.run();

		ArgumentCaptor<NodeData> capturedNodeData = ArgumentCaptor.forClass(NodeData.class);
		Mockito.verify(testWalletController, atLeast(1)).insertResponseNodeData(capturedNodeData.capture());

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testMessages.isEmpty());
		assertTrue(capturedNodeData.getValue().equals(testNodeData));
		assertTrue(capturedMessage.getValue().equals(testDataResponseMessage));
	}

	/**
	 * Testing the MessageWorker object sendWalletData(..) method and run method
	 * switch by mocking a Message object containing the DataRequest endpoint.
	 */
	@Test
	public void runMessageWorker_mockMessageWithDataRequest_sendWalletDataCalled() {

		testMessages.add(testDataRequestMessage);

		testMessageWorker.run();

		Mockito.verify(testWalletController, atLeast(1)).sendWalletData();

	}
}
