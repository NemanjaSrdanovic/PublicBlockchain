package udp_connection.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import connection.Connection;
import constraints.Constraints;
import controllers.IpPortHandler;
import enumerations.EMessageEndpoint;
import marshaller.Marshaller;
import messageProcessor.MessageProcessor;
import messages.Message;
import node.Node;
import udp_connection.ServerWorker;
import udp_connection.UDP_Client;
import udp_connection.UDP_Server;

/**
 * Testing the functionalities of the UDP_Client object by mocking corresponding
 * objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class UDP_ClientTest {

	private static Connection testConnection;
	private static IpPortHandler testIpPortHandler;
	private static Node testNode;
	private static UDP_Client client;
	private static DatagramSocket connectionAttempt;
	private static UDP_Client testClient;
	private static String localIp;
	private static Marshaller testMarshaller;
	private static UDP_Server udpTestServer;
	private static MessageProcessor testMessageProcessor;
	private static Set<String> connectedClientsMock;
	private static byte[] buffer;
	private static byte[] receivedData;
	private static HashMap<String, Boolean> testSendMessagesStatus;
	private static DatagramPacket recPacket;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testConnection = Mockito.mock(Connection.class);
		testIpPortHandler = Mockito.mock(IpPortHandler.class);
		testNode = Mockito.mock(Node.class);
		testClient = Mockito.mock(UDP_Client.class);
		udpTestServer = Mockito.mock(UDP_Server.class);
		testMessageProcessor = Mockito.mock(MessageProcessor.class);
		connectedClientsMock = Mockito.mock(Set.class);

		buffer = new byte[1024];

		receivedData = new byte[1024];

		testSendMessagesStatus = new HashMap<String, Boolean>();

		recPacket = new DatagramPacket(receivedData, receivedData.length);

		localIp = InetAddress.getLocalHost().getHostAddress().toString();

		testMarshaller = new Marshaller();

		Mockito.when(testClient.getConnection()).thenReturn(testConnection);

		Mockito.when(testConnection.getPortHandler()).thenReturn(testIpPortHandler);

		Mockito.when(testConnection.getServer()).thenReturn(udpTestServer);

		Mockito.when(testConnection.getMyNode()).thenReturn(testNode);

		Mockito.when(testNode.getClientPortNmr()).thenReturn(4000);

		Mockito.when(testNode.getServerPortNmr()).thenReturn(4040);

		Mockito.when(testNode.getIpAddress()).thenReturn("192.0.1.2");

		Mockito.when(udpTestServer.getMessageProcessor()).thenReturn(testMessageProcessor);

		Mockito.when(testIpPortHandler.releaseAndReturnReservedClientPort()).thenReturn(4000);

		Mockito.when(testClient.getSendMessagesStatus()).thenReturn(testSendMessagesStatus);

		udpTestServer.setMessageProcessor(testMessageProcessor);

	}

	/**
	 * Running exactly once during the test run - at the very end of the test
	 * execution to clear all states of the test execution.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		testConnection = null;
		testIpPortHandler = null;
		testNode = null;
	}

	/**
	 * Testing the UDP_Client object initialisation. After the object has been
	 * initialised the socket will be reserved so that the attempt to connect to
	 * this socket will throw a socket exception.
	 * 
	 * @throws SocketException
	 */
	@Test(expected = SocketException.class)
	public void startClientConstructor_CheckIfConnectedOnSocket_ClientConnectedReconnectingThrowsSocketException()
			throws SocketException {

		client = new UDP_Client(testConnection);

		connectionAttempt = new DatagramSocket(4000);

	}

	/**
	 * Testing the exception handling for the UDP_Client initialisation. The socket
	 * on which the client will try to connect is already taken so that the
	 * initialisation will throw a binding exception.
	 * 
	 * @throws SocketException
	 */
	@Test(expected = BindException.class)
	public void startClientConstructor_ReserveTheSocketBeforeClient_ClientNotConnectedThrowsBindException()
			throws SocketException {

		connectionAttempt = new DatagramSocket(4000);

		try {
			client = new UDP_Client(testConnection);

		} catch (NullPointerException e) {

		}
	}

	/**
	 * Checking if the UDP_Client object method randomIPAddressAndPortToConnect() is
	 * returning a ip:port address that is in the range defined in the project
	 * constraints.
	 */
	@Test
	public void callRandomIPAddressAndPortToConnect_ReceiveRandomValue_ValueInRangeOfConstraints() {

		Mockito.when(testClient.randomIPAddressAndPortToConnect()).thenCallRealMethod();

		int returnValue = Integer.valueOf(testClient.randomIPAddressAndPortToConnect().split(":")[1]);

		assertTrue(returnValue <= Constraints.PORTNUM_MAX && returnValue >= Constraints.PORTNUM_MIN);
	}

	/**
	 * Checking the functionality of the UDP_Client object method sendMessage(..) by
	 * mocking all dependencies needed for the method to work properly. The test
	 * will connect to two local sockets that represent the client and server
	 * socket. A datagram packet is send from the client socket to the server
	 * socket, which is picked up by the serverWorker, de-marshalled and depending
	 * on the message instance processed. The send() method will receive a message
	 * object that will have to be marshalled and send as a datagram packet to the
	 * connected server socket. The send() method should properly process the
	 * message use the client socket to send the message and receive a response that
	 * the message has been received without throwing any exception. The received
	 * message will be analysed and should contain the same data as the mocked
	 * message object.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void callSendMessage_MessageSendOverSocket_MessageReceivedAndMessageStatusTrue()
			throws IOException, SQLException, InterruptedException {

		DatagramSocket clientTestSocket = new DatagramSocket(9005);
		DatagramSocket serverWorkerSocket = new DatagramSocket(9006);

		String connectedTestServer = localIp + ":" + serverWorkerSocket.getLocalPort();
		Set<String> connectedTestPorts = new HashSet<String>();
		connectedTestPorts.add(connectedTestServer);

		Message testMessage = new Message(localIp + ":" + serverWorkerSocket.getLocalPort(),
				localIp + ":" + clientTestSocket.getLocalPort(), EMessageEndpoint.Block, null);

		DatagramPacket messagePacket = testMarshaller.makeDatagramPacket(testMessage, buffer,
				localIp + ":" + serverWorkerSocket.getLocalPort());

		Mockito.doCallRealMethod().when(testClient).sendMessage(testMessage, buffer, messagePacket, testMarshaller,
				recPacket, receivedData);

		Mockito.when(testClient.getConnectedIPsPort()).thenReturn(connectedTestPorts);

		Mockito.when(testClient.getSocket()).thenReturn(clientTestSocket);

		Mockito.doReturn(serverWorkerSocket).when(udpTestServer).getSocket();

		Mockito.when(udpTestServer.getConnectedClients()).thenReturn(connectedClientsMock);

		Mockito.when(connectedClientsMock.contains(Mockito.anyString())).thenReturn(true);

		Thread thread = new Thread(new ServerWorker(udpTestServer));

		thread.start();

		testClient.sendMessage(testMessage, buffer, messagePacket, testMarshaller, recPacket, receivedData);

		Thread.sleep(500);

		if (thread.isAlive())
			thread.stop();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testMessageProcessor).onMessage(capturedMessage.capture());

		assertTrue(testMessage.getMessageId().equals(capturedMessage.getValue().getMessageId()));
		assertTrue(testSendMessagesStatus.get(testMessage.getMessageId()));

		clientTestSocket.close();
		serverWorkerSocket.close();
	}

	/**
	 * Checking the functionality of the UDP_Client object method sendMessage(..) by
	 * mocking all dependencies needed for the method to work properly. The test
	 * will connect to a local sockets that represent the client socket. A datagram
	 * packet is send from the client socket to the server socket which will be
	 * closed (not instantiated). After a certain amount of time the socket timeout
	 * will be called and the "connected" server socket removed from the connected
	 * list to make space for other active nodes to connect. After that the
	 * connected list which contained only the closed server node will be empty.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void callSendMessage_MessageSendToClosedSocket_InterruptedIOExceptionThrownAndConnectedNodeIpPortRemoved()
			throws IOException, SQLException {

		DatagramSocket clientTestSocket = new DatagramSocket(9007);

		String connectedTestServer = localIp + ":" + "9000";
		Set<String> connectedTestServerPorts = new HashSet<String>();
		connectedTestServerPorts.add(connectedTestServer);

		String connectedTestClient = localIp + ":" + "9030";
		Set<String> connectedTestClientPorts = new HashSet<String>();
		connectedTestClientPorts.add(connectedTestClient);

		Message testMessage = new Message(localIp + ":" + "9000", localIp + ":" + clientTestSocket.getLocalPort(),
				EMessageEndpoint.Block, null);

		DatagramPacket messagePacket = testMarshaller.makeDatagramPacket(testMessage, buffer, localIp + ":" + "9000");

		Mockito.doCallRealMethod().when(testClient).sendMessage(testMessage, buffer, messagePacket, testMarshaller,
				recPacket, receivedData);

		Mockito.when(testClient.getConnectedIPsPort()).thenReturn(connectedTestServerPorts);

		Mockito.when(testClient.getSocket()).thenReturn(clientTestSocket);

		Mockito.when(udpTestServer.getConnectedClients()).thenReturn(connectedTestClientPorts);

		testClient.sendMessage(testMessage, buffer, messagePacket, testMarshaller, recPacket, receivedData);

		clientTestSocket.close();

		assertTrue(connectedTestServerPorts.size() == 0);
		assertTrue(connectedTestClientPorts.size() == 0);

	}

	/**
	 * Checking that the method which saves the connected client is correctly
	 * extracting the ip and port address from the received datagram packet and
	 * saving the address as a connected client and server in the list.
	 * 
	 * @throws IOException
	 */
	@Test
	public void setIPandPortOfRandomNodes_HandOverRecPacket_ConnectedClientIpPortAndConnectedServerIpPortContainingAddress()
			throws IOException {

		Set<String> connectedTestServerPorts = new HashSet<String>();
		Set<String> connectedTestClientPorts = new HashSet<String>();

		String serverAddress = localIp + ":" + "9000";

		String clientAddress = localIp + ":" + "9030";

		Mockito.when(testClient.getConnectedIPsPort()).thenReturn(connectedTestServerPorts);

		Mockito.when(udpTestServer.getConnectedClients()).thenReturn(connectedTestClientPorts);

		DatagramPacket messagePacket = testMarshaller.makeDatagramPacket(null, buffer, serverAddress);

		Mockito.doCallRealMethod().when(testClient).setIPandPortOfRandomNodes(messagePacket, null);

		testClient.setIPandPortOfRandomNodes(messagePacket, null);

		assertTrue(connectedTestClientPorts.contains(clientAddress));
		assertTrue(connectedTestServerPorts.contains(serverAddress));

	}

	/**
	 * Checking that the method which saves the connected client is correctly
	 * extracting the ip and port address from the request datagram packet to which
	 * a positive connection response has been received and saving the address as a
	 * connected client and server in the list. The list should contain the max
	 * amout of connected nodes after which the method isMaximalClientsConnected()
	 * should return true.
	 * 
	 * @throws IOException
	 */
	@Test
	public void setIPandPortOfRandomNodes_HandOverRequestPacket_ConnectedClientIpPortAndConnectedServerIpPortContainingAddress()
			throws IOException {

		Set<String> connectedTestServerPorts = new HashSet<String>();
		connectedTestServerPorts.add("172.19.160.1:9001");
		connectedTestServerPorts.add("172.19.160.1:9002");

		Set<String> connectedTestClientPorts = new HashSet<String>();

		String serverAddress = localIp + ":" + "9003";

		String clientAddress = localIp + ":" + "9033";

		Mockito.when(testClient.getConnectedIPsPort()).thenReturn(connectedTestServerPorts);

		Mockito.when(udpTestServer.getConnectedClients()).thenReturn(connectedTestClientPorts);

		DatagramPacket messagePacket = testMarshaller.makeDatagramPacket(null, buffer, clientAddress);

		Mockito.doCallRealMethod().when(testClient).setIPandPortOfRandomNodes(null, messagePacket);

		Mockito.doCallRealMethod().when(testClient).isMaximalClientsConnected();

		testClient.setIPandPortOfRandomNodes(null, messagePacket);

		assertTrue(connectedTestClientPorts.contains(clientAddress));
		assertTrue(connectedTestServerPorts.contains(serverAddress));
		assertTrue(testClient.isMaximalClientsConnected());

	}

}
