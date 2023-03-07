package udp_connection.test;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import connection.Connection;
import controllers.IpPortHandler;
import node.Node;
import udp_connection.UDP_Server;

/**
 * Testing the functionalities of the UDP_Server object by mocking corresponding
 * objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class UDP_ServerTest {

	private static Connection testConnection;
	private static IpPortHandler testIpPortHandler;
	private static Node testNode;
	private static UDP_Server server;
	private static DatagramSocket connectionAttempt;

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

		Mockito.when(testConnection.getPortHandler()).thenReturn(testIpPortHandler);

		Mockito.when(testConnection.getMyNode()).thenReturn(testNode);

		Mockito.when(testNode.getServerPortNmr()).thenReturn(3000);

		Mockito.when(testIpPortHandler.releaseAndReturnReservedServerPort()).thenReturn(3000);

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
	 * Testing the UDP_Server object initialisation. After the object has been
	 * initialised the socket will be reserved so that the attempt to connect to
	 * this socket will throw a socket exception.
	 * 
	 * @throws SocketException
	 */
	@Test(expected = SocketException.class)
	public void startServerConstructor_CheckIfConnectedOnSocket_ServerConnectedReconnectingThrowsSocketException()
			throws SocketException {

		server = new UDP_Server(testConnection);

		connectionAttempt = new DatagramSocket(3000);

	}

	/**
	 * Testing the exception handling for the UDP_Server initialisation. The socket
	 * on which the client will try to connect is already taken so that the
	 * initialisation will throw a binding exception.
	 * 
	 * @throws SocketException
	 */
	@Test(expected = BindException.class)
	public void startServerConstructor_ReserveTheSocketBeforeServer_ServerNotConnectedThrowsBindException()
			throws SocketException {

		connectionAttempt = new DatagramSocket(3000);

		server = new UDP_Server(testConnection);

	}

}
