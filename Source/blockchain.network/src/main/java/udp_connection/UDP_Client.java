package udp_connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import connection.Connection;
import constraints.Constraints;
import marshaller.Marshaller;
import messages.Message;

/**
 * This UDP_Client object contains all methods and data that the ClientWorker
 * and other Objects can use to send messages to connected nodes, or check which
 * nodes are connected. It reserves a local socket over which it sends messages
 * to other connected server sockets.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class UDP_Client implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(UDP_Client.class);
	private BlockingQueue<Message> messages;
	private ExecutorService threadPool;
	private boolean maximalClientsConnected;
	private Set<String> connectedIPsPort;
	private DatagramSocket socket;
	private Connection connection;
	private HashMap<String, Boolean> sendMessagesStatus;

	/**
	 * Instantiates a new UDP_Server handler object. The parameters must not be
	 * null.
	 * 
	 * @param connection
	 */
	public UDP_Client(Connection connection) {

		logger.info("Client connecting on port " + connection.getMyNode().getClientPortNmr());

		this.connection = connection;

		try {
			this.socket = new DatagramSocket(connection.getPortHandler().releaseAndReturnReservedClientPort());

			logger.info("Client connected on port " + socket.getLocalPort());

		} catch (SocketException e) {

			logger.error("Socket error while connecting client.", e);

			throw new NullPointerException("UDP_Client object couldn´t be fully initialized due SocketException.");
		}

		this.threadPool = Executors.newFixedThreadPool(2);
		this.connectedIPsPort = new HashSet<String>();
		this.messages = new LinkedBlockingQueue<Message>();
		this.maximalClientsConnected = false;
		this.sendMessagesStatus = new HashMap<String, Boolean>();

		try {

			for (int i = 0; i < 2; ++i)

				this.threadPool.execute(new ClientWorker(this));
		} catch (SocketException e) {

			logger.error("Client worker initialization exception", e);

		}

	}

	/**
	 * Returns a random Port and IP address which is not the nodes address or a
	 * address which is already connected.
	 * 
	 * @return
	 */
	public synchronized String randomIPAddressAndPortToConnect() {

		String[] ipPort;
		String random_ip_port_comb;

		do {

			random_ip_port_comb = iterateThroughIPsandPorts();

			ipPort = random_ip_port_comb.split(":");

		} while (ipPort[1].equals(Integer.toString(getConnection().getMyNode().getServerPortNmr()))
				|| getConnectedIPsPort().contains(random_ip_port_comb));

		return random_ip_port_comb;

	}

	/**
	 * Iterates randomly between the max and min allowed port numbers and return it
	 * in a string format.
	 * 
	 * @return
	 */
	private synchronized String iterateThroughIPsandPorts() {

		int port;

		Random random = new Random();

		port = random.nextInt(Constraints.PORTNUM_MAX - Constraints.PORTNUM_MIN + 1) + Constraints.PORTNUM_MIN;

		return getConnection().getMyNode().getIpAddress() + ":" + port;

	}

	/**
	 * Iterates through all connected ip:port addresses and sends them over the
	 * socket the marshaled message in a datagrampacket format.
	 * 
	 * @param message
	 * @param buffer
	 * @param packet
	 * @param marshaller
	 * @param recPacket
	 * @param receivedData
	 * @throws IOException
	 * @throws SQLException
	 */
	public synchronized void sendMessage(Message message, byte[] buffer, DatagramPacket packet, Marshaller marshaller,
			DatagramPacket recPacket, byte[] receivedData) throws IOException, SQLException {

		getSendMessagesStatus().put(message.getMessageId(), false);

		Set<String> tempConnectedServers = new HashSet<String>(getConnectedIPsPort());

		Iterator<String> connectedNodes = tempConnectedServers.iterator();

		while (connectedNodes.hasNext()) {

			String connectedNodeIpPort = connectedNodes.next();
			boolean messageReceived = false;

			packet = marshaller.makeDatagramPacket(message, buffer, connectedNodeIpPort);

			while (!messageReceived) {

				try {

					getSocket().setSoTimeout(10000);

					getSocket().send(packet);

					receivedData = new byte[1024];

					recPacket = new DatagramPacket(receivedData, receivedData.length);

					try {
						getSocket().receive(recPacket);

						messageReceived = true;

						getSendMessagesStatus().replace(message.getMessageId(), true);

					} catch (InterruptedIOException e) {

						logger.error("Socket " + connectedNodeIpPort + " is closed");

						messageReceived = true;

						getConnectedIPsPort().remove(connectedNodeIpPort);

						getConnection().getServer().getConnectedClients()
								.remove(getCorrespondingClientAddress(connectedNodeIpPort));

						connectedNodes.remove();

						if (getConnectedIPsPort().size() < Constraints.MAX_CONNECTED_SERVER_NODES) {

							this.maximalClientsConnected = false;
						}

						logger.info("Client connected to : [" + connectedIPsPort + "]");

					}
				} catch (SocketTimeoutException e) {

					logger.error("Message sending: Response timeout", e);

				} catch (IOException e) {

					logger.error("Unknown source exception", e);
				}

			}

		}

	}

	/**
	 * Receives the ip:port of a node which answered the connection request and
	 * inserts this address into the connected list if maximal amount of connected
	 * server nodes is not already reached.
	 * 
	 * @param ipPortOfRecipient
	 * @param recPacket
	 * @throws IOException
	 */
	public synchronized void setIPandPortOfRandomNodes(DatagramPacket recPacket, DatagramPacket requestPacket)
			throws IOException {

		String connectedServerIpPort = null;
		String connectedClientIpPort = null;

		if (recPacket != null) {

			connectedServerIpPort = String.valueOf(recPacket.getAddress().getHostAddress() + ":" + recPacket.getPort());

			connectedClientIpPort = String.valueOf(recPacket.getAddress().getHostAddress() + ":"
					+ (recPacket.getPort() + Constraints.CLIENT_SOCKET_ADDITION));
		}

		if (requestPacket != null) {

			connectedServerIpPort = String.valueOf(requestPacket.getAddress().getHostAddress() + ":"
					+ (requestPacket.getPort() - Constraints.CLIENT_SOCKET_ADDITION));

			connectedClientIpPort = requestPacket.getAddress().getHostAddress() + ":" + requestPacket.getPort();

		}

		if (getConnection().getServer().getConnectedClients().size() < Constraints.MAX_CONNECTED_CLIENT_NODES) {

			getConnectedIPsPort().add(connectedServerIpPort);
			getConnection().getServer().getConnectedClients().add(connectedClientIpPort);

			logger.info("Client connected to : [" + connectedIPsPort + "]");
			logger.info("Server connected to: [" + getConnection().getServer().getConnectedClients() + "]");
		}

		if (getConnectedIPsPort().size() >= Constraints.MAX_CONNECTED_SERVER_NODES) {

			maximalClientsConnected = true;

		}

	}

	/**
	 * Adds a message object into the list of messages which are waiting to be send
	 * by the ClientWorker object.
	 * 
	 * @param message
	 * @throws InterruptedException
	 */
	public synchronized void addMessage(Message message) throws InterruptedException {
		messages.put(message);
	}

	/**
	 * Removes message from the list of messages which are waiting to be send and
	 * returns it to the requesting object.
	 * 
	 * @return
	 */
	public synchronized Message getMessage() {
		return messages.poll();
	}

	/**
	 * Returns if the maximal amount of connected server nodes is connected.
	 * 
	 * @return
	 */
	public synchronized boolean isMaximalClientsConnected() {
		return maximalClientsConnected;
	}

	/**
	 * Returns the list of ip:port addresses which are connected to this node.
	 * 
	 * @return
	 */
	public synchronized Set<String> getConnectedIPsPort() {
		return connectedIPsPort;
	}

	/**
	 * Returns the DatagramSocket on which this UDP_Client is connected.
	 * 
	 * @return
	 */
	public synchronized DatagramSocket getSocket() {
		return socket;
	}

	/**
	 * Returns connection object hand over to this UDP_Client object.
	 * 
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Returns map containing all send messages and their sending status (true if
	 * received and false if not).
	 * 
	 * @return
	 */
	public HashMap<String, Boolean> getSendMessagesStatus() {
		return sendMessagesStatus;
	}

	/**
	 * Returns a boolean value which signals if a message was received by the
	 * connected node or not;
	 * 
	 * @param message
	 * @return
	 */
	public boolean isMessageReceived(Message message) {

		if (this.sendMessagesStatus.get(message.getMessageId()) == null) {

			return false;
		} else {

			return this.sendMessagesStatus.get(message.getMessageId());
		}
	}

	/**
	 * Returns the client address for the input server address.
	 * 
	 * @param serverIpPortAddress
	 * @return
	 */
	private synchronized String getCorrespondingClientAddress(String serverIpPortAddress) {

		String[] serverIpPortAddressSeparated = serverIpPortAddress.split(":");

		String clientIpAddress = serverIpPortAddressSeparated[0];

		int serverIpAddress = Integer.valueOf(serverIpPortAddressSeparated[1]);

		int clientPortAddress = serverIpAddress + Constraints.CLIENT_SOCKET_ADDITION;

		return clientIpAddress + ":" + String.valueOf(clientPortAddress);
	}

	/**
	 * Starts this UDP_Client object and runs it till it is manually terminated.
	 */
	@Override
	public void run() {

		while (true) {

		}
	}

}
