package controller.test;

import static org.junit.Assert.assertTrue;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import controllers.IpPortHandler;
import node.Node;

/**
 * Testing the functionalities of the IpPortHandler object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class IpPortHandlerTest {

	private String localIpAddressFoundInThisTest;
	private static IpPortHandler ipPortHandler;
	private static Node testNode;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testNode = new Node();
		ipPortHandler = new IpPortHandler(testNode);

	}

	/**
	 * Running exactly once during the test run - at the very end of the test
	 * execution to clear all states of the test execution.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		testNode = null;
		ipPortHandler = null;
	}

	/**
	 * Executed before each tests in this class to prepare dependencies.
	 */
	@BeforeEach
	public void setUp() {

		testNode = new Node();
		ipPortHandler = new IpPortHandler(testNode);
		this.localIpAddressFoundInThisTest = null;

	}

	/**
	 * Executed after each tests in this class to prepare dependencies.
	 */
	@AfterEach
	public void tearDown() {

		testNode = null;
		ipPortHandler = null;
		this.localIpAddressFoundInThisTest = null;

	}

	/**
	 * Running a instance of the IpPortHandler object and executing its
	 * getIpAddress() method to check of the returned local Ip Address is matching
	 * with the one returned by that method.
	 */
	@Test
	public void findLocalIpAddress_SetIpAddressInNode_IsVariableIpAddressSameAsLocalIpAddress() {

		try {
			this.localIpAddressFoundInThisTest = InetAddress.getLocalHost().getHostAddress().toString();

		} catch (UnknownHostException e) {

			System.err.println("Exception while fetching local ip address in test.");
		}

		assertTrue(this.localIpAddressFoundInThisTest.equals(testNode.getIpAddress()));
	}

	/**
	 * Testing if a connection with the right client object has been established by
	 * intentionally provoking a exception and checking if the correct exception has
	 * been thrown.
	 * 
	 * @throws SocketException
	 */
	@Test(expected = SocketException.class)
	public void fetchClientPortReservedByIpPortHandler_TryToConnectToReservedPort_IsSocketExceptionThrown()
			throws SocketException {

		DatagramSocket reservePort = new DatagramSocket(testNode.getClientPortNmr());

	}

	/**
	 * Testing if a connection with the right server object has been established by
	 * intentionally provoking a exception and checking if the correct exception has
	 * been thrown.
	 * 
	 * @throws SocketException
	 */
	@Test(expected = SocketException.class)
	public void fetchServerPortReservedByIpPortHandler_TryToConnectToReservedPort_IsSocketExceptionThrown()
			throws SocketException {

		DatagramSocket reservePort = new DatagramSocket(testNode.getServerPortNmr());

	}

	/**
	 * Testing the functionality of the releaseAndReturnReservedClientPort() method
	 * on the client object by checking if the correct port has been released and if
	 * reconnection to that port is possible again.
	 * 
	 * @throws SocketException
	 */
	@Test
	public void releaseClientPortReservedByIpPortHandler_TryToConnectToReservedPort_IsTheSamePortReservedAgain()
			throws SocketException {

		int clientPortReservedByIpPortHandler = ipPortHandler.releaseAndReturnReservedClientPort();

		DatagramSocket reservePort = new DatagramSocket(clientPortReservedByIpPortHandler);

		Assert.assertTrue(clientPortReservedByIpPortHandler == reservePort.getLocalPort());

	}

	/**
	 * Testing the functionality of the releaseAndReturnReservedClientPort() method
	 * on the server object by checking if the correct port has been released and if
	 * reconnection to that port is possible again.
	 * 
	 * @throws SocketException
	 */
	@Test
	public void releaseServerPortReservedByIpPortHandler_TryToConnectToReservedPort_IsTheSamePortReservedAgain()
			throws SocketException {

		int serverPortReservedByIpPortHandler = ipPortHandler.releaseAndReturnReservedServerPort();

		DatagramSocket reservePort = new DatagramSocket(serverPortReservedByIpPortHandler);

		Assert.assertTrue(serverPortReservedByIpPortHandler == reservePort.getLocalPort());

	}

}
