
package udp_connection.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import connection.Connection;
import enumerations.EMessageEndpoint;
import marshaller.Marshaller;
import messageProcessor.MessageProcessor;
import messages.Message;
import udp_connection.ServerWorker;
import udp_connection.UDP_Client;
import udp_connection.UDP_Server;

/**
 * Testing the functionalities of the ServerWorker object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class ServerWorkerTest {

	private static UDP_Server udpTestServer;
	private static MessageProcessor testMessageProcessor;
	private static Marshaller testMarshaller;
	private static DatagramSocket clientSocket;
	private static DatagramSocket serverSocket;
	private static Set<String> connectedClientsMock;
	private static String localIp;
	private static UDP_Client testClient;
	private static Connection testConnection;
	private static DatagramPacket stringPacket;
	private static DatagramPacket messagePacket;
	private static byte[] buffer;
	private static Message testMessage;
	private static String message;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		udpTestServer = Mockito.mock(UDP_Server.class);
		testMessageProcessor = Mockito.mock(MessageProcessor.class);
		testMarshaller = new Marshaller();
		connectedClientsMock = Mockito.mock(Set.class);
		testClient = Mockito.mock(UDP_Client.class);
		testConnection = Mockito.mock(Connection.class);

		Mockito.when(udpTestServer.getMessageProcessor()).thenReturn(testMessageProcessor);

		Mockito.when(udpTestServer.getConnectedClients()).thenReturn(connectedClientsMock);

		Mockito.when(connectedClientsMock.size()).thenReturn(1);

		Mockito.doReturn(testConnection).when(udpTestServer).getConnection();

		Mockito.doReturn(testClient).when(testConnection).getClient();

		message = "Hello";
		buffer = new byte[1024];

		localIp = InetAddress.getLocalHost().getHostAddress().toString();

	}

	/**
	 * Running exactly once during the test run - at the very end of the test
	 * execution to clear all states of the test execution.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		clientSocket.close();
		serverSocket.close();

	}

	/**
	 * Executed after each tests in this class to prepare dependencies
	 */
	@AfterEach
	public void tearDown() {

		clientSocket.close();
		serverSocket.close();
	}

	/**
	 * Checking the functionality of the ServerWorker object by mocking all
	 * dependencies needed for the run method to work properly. The test will
	 * connect to two local sockets that represent the client and server socket. A
	 * datagram packet is send from the client socket to the server socket, which is
	 * picked up by the serverWorker, de-marshalled and depending on the message
	 * instance processed. The mocked message is of instance string which represents
	 * a connection request that the serverWorker will process. In that process the
	 * client object setIPandPortOfRandomNodes() method is called which is verified
	 * by the test.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void createRequestPackateContainingString_HandOverToServerWorker_ClientAddedToConnectedClients()
			throws IOException, InterruptedException {

		clientSocket = new DatagramSocket(9000);
		serverSocket = new DatagramSocket(9001);

		stringPacket = testMarshaller.makeDatagramPacket(message, buffer, localIp + ":" + serverSocket.getLocalPort());

		Mockito.doReturn(serverSocket).when(udpTestServer).getSocket();

		Thread thread = new Thread(new ServerWorker(udpTestServer));

		thread.start();

		clientSocket.send(stringPacket);

		Thread.sleep(500);

		if (thread.isAlive())
			thread.stop();

		Mockito.verify(testConnection, atLeast(1)).getClient();

	}

	/**
	 * Checking the functionality of the ServerWorker object by mocking all
	 * dependencies needed for the run method to work properly. The test will
	 * connect to two local sockets that represent the client and server socket. A
	 * datagram packet is send from the client socket to the server socket, which is
	 * picked up by the serverWorker, de-marshalled and depending on the message
	 * instance processed. The mocked message is of instance message so that the
	 * serverWorker is using the messageProcessor to forward this message to the
	 * component that uses this network component. In that process the onMessage()
	 * method is called and the test verifies if the message forwarded by the
	 * serverWorker is the same as the mocked one.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void createRequestPackateContainingMessage_HandOverToServerWorker_ServerWorkerOnMessageContainsMessage()
			throws IOException, InterruptedException {

		clientSocket = new DatagramSocket(9002);
		serverSocket = new DatagramSocket(9003);

		testMessage = new Message(localIp + ":" + serverSocket.getLocalPort(),
				localIp + ":" + clientSocket.getLocalPort(), EMessageEndpoint.Block, null);

		messagePacket = testMarshaller.makeDatagramPacket(testMessage, buffer,
				localIp + ":" + serverSocket.getLocalPort());

		Mockito.doReturn(serverSocket).when(udpTestServer).getSocket();

		udpTestServer.setMessageProcessor(testMessageProcessor);

		Mockito.when(connectedClientsMock.contains(Mockito.anyString())).thenReturn(true);

		Thread thread = new Thread(new ServerWorker(udpTestServer));

		thread.start();

		clientSocket.send(messagePacket);

		Thread.sleep(500);

		if (thread.isAlive())
			thread.stop();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testMessageProcessor).onMessage(capturedMessage.capture());

		assertTrue(testMessage.getMessageId().equals(capturedMessage.getValue().getMessageId()));

	}

}
