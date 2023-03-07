package controller.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import connection.Connection;
import controllers.ConnectionHandler;
import messageProcessor.MessageProcessor;
import udp_connection.UDP_Server;

/**
 * Testing the functionalities of the ConnectionHandler object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 25 Jan 2022
 */
public class ConnectionHandlerTest {

	private static ConnectionHandler connectionHandler;
	private static UDP_Server udpTestServer;
	private static Connection testConnection;
	private static MessageProcessor testMessageProcessor;

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
		connectionHandler = Mockito.mock(ConnectionHandler.class);
		testConnection = Mockito.mock(Connection.class);

		Mockito.when(connectionHandler.getConnection()).thenReturn(testConnection);
		Mockito.when(testConnection.getServer()).thenReturn(udpTestServer);
		Mockito.doCallRealMethod().when(connectionHandler).setMessageProcessor(testMessageProcessor);
		Mockito.doCallRealMethod().when(udpTestServer).setMessageProcessor(testMessageProcessor);
		Mockito.when(udpTestServer.getMessageProcessor()).thenCallRealMethod();
	}

	/**
	 * Running exactly once during the test run - at the very end of the test
	 * execution to clear all states of the test execution.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		udpTestServer = null;
		testMessageProcessor = null;
		connectionHandler = null;
		testConnection = null;
	}

	/**
	 * Testing the set up of the messageProcessor interface by assigning the mocked
	 * object and checking if the returned object is equals to the set up object.
	 */
	@Test
	public void callConnectionHandlerToSetMessageProcessor_IsUDPServerMessageProcessorSameObjAsSet() {

		connectionHandler.setMessageProcessor(testMessageProcessor);

		assertTrue(connectionHandler.getConnection().getServer().getMessageProcessor().equals(testMessageProcessor));

	}

}
