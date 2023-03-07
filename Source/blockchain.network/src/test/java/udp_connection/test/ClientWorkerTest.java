package udp_connection.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import enumerations.EMessageEndpoint;
import marshaller.Marshaller;
import messages.Message;
import udp_connection.ClientWorker;
import udp_connection.UDP_Client;

/**
 * Testing the functionalities of the ClientWorker object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class ClientWorkerTest {

	private static UDP_Client testClient;
	private static DatagramSocket testSocket;
	private static String localIp;
	private static Set<String> testConnectedIpPorts;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testClient = Mockito.mock(UDP_Client.class);
		testSocket = Mockito.mock(DatagramSocket.class);
		localIp = InetAddress.getLocalHost().getHostAddress().toString();
		testConnectedIpPorts = Mockito.mock(Set.class);

		Mockito.when(testClient.randomIPAddressAndPortToConnect()).thenReturn(localIp + ":" + 3033);
		Mockito.when(testClient.getSocket()).thenReturn(testSocket);
		Mockito.when(testClient.getConnectedIPsPort()).thenReturn(testConnectedIpPorts);
	}

	/**
	 * Checking the functionality of the ClientWorker object by mocking all
	 * dependencies needed for the run method to work properly. The method will
	 * check if enough nodes are connected and send connection requests to all nodes
	 * in the network. After receiving a connection confirmation the method will
	 * call the method setIPandPortOfRandomNodesCalled() to save the connection.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void isMaximalClientsConnectedFalse_sendGreeting_setIPandPortOfRandomNodesCalled()
			throws IOException, InterruptedException {

		Mockito.doNothing().when(testSocket).receive(Mockito.any(DatagramPacket.class));

		Mockito.doNothing().when(testSocket).send(Mockito.any(DatagramPacket.class));

		Mockito.doNothing().when(testSocket).setSoTimeout(Mockito.anyInt());

		Mockito.when(testConnectedIpPorts.size()).thenReturn(1);

		Mockito.when(testClient.isMaximalClientsConnected()).thenReturn(false);

		Thread thread = new Thread(new ClientWorker(testClient));

		thread.start();

		Thread.sleep(100);

		if (thread.isAlive())
			thread.stop();

		ArgumentCaptor<DatagramPacket> capturedRecPacket = ArgumentCaptor.forClass(DatagramPacket.class);
		ArgumentCaptor<DatagramPacket> capturedReqPacket = ArgumentCaptor.forClass(DatagramPacket.class);

		Mockito.verify(testClient, atLeast(1)).setIPandPortOfRandomNodes(capturedRecPacket.capture(),
				capturedReqPacket.capture());

	}

	/**
	 * Checking the functionality of the ClientWorker object by mocking all
	 * dependencies needed for the run method to work properly. The method will
	 * check if enough nodes are connected and call the sendMessage(..) method to
	 * send a mocked message object to the connected nodes. This method call will be
	 * verified by the test and the object which the ClientWorker is trying to send
	 * compared with the mocked object. Both object should be the same.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void isMaximalClientsConnectedTrueMessageNotNull_sendMessage_MessageInSendMessageEquals()
			throws InterruptedException, IOException, SQLException {

		Mockito.when(testClient.isMaximalClientsConnected()).thenReturn(true);

		Mockito.when(testConnectedIpPorts.size()).thenReturn(2);

		Message testMessage = new Message(localIp + ":" + "3000", localIp + ":" + "3050", EMessageEndpoint.Block, null);

		Mockito.when(testClient.getMessage()).thenReturn(testMessage);

		Thread thread = new Thread(new ClientWorker(testClient));

		thread.start();

		Thread.sleep(100);

		if (thread.isAlive())
			thread.stop();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		ArgumentCaptor<byte[]> capturedBuffer = ArgumentCaptor.forClass(byte[].class);
		ArgumentCaptor<DatagramPacket> capturedPacket = ArgumentCaptor.forClass(DatagramPacket.class);
		ArgumentCaptor<Marshaller> capturedMarshaller = ArgumentCaptor.forClass(Marshaller.class);
		ArgumentCaptor<byte[]> capturedRecPacket = ArgumentCaptor.forClass(byte[].class);
		ArgumentCaptor<DatagramPacket> capturedReceivedData = ArgumentCaptor.forClass(DatagramPacket.class);

		Mockito.verify(testClient, atLeast(1)).sendMessage(capturedMessage.capture(), capturedBuffer.capture(),
				capturedPacket.capture(), capturedMarshaller.capture(), capturedReceivedData.capture(),
				capturedRecPacket.capture());

		assertTrue(testMessage.getMessageId().equals(capturedMessage.getValue().getMessageId()));
	}

}