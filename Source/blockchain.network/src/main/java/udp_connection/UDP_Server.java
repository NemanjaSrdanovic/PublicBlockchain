package udp_connection;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import connection.Connection;
import messageProcessor.MessageProcessor;

/**
 * This object contains all methods and data that the ServerWorker needs to
 * receive datagram packets and extract messages. It connects to a local port
 * over which datagram packets are received. Its is also used to set the message
 * processor interface that the node which implements this component is using to
 * receive extracted messages from this object.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class UDP_Server {

	private static Logger logger = LoggerFactory.getLogger(UDP_Server.class);
	private Connection connection;
	private MessageProcessor messageProcessor;
	private ExecutorService threadPool;
	private DatagramSocket socket = null;
	private Set<String> connectedClients;

	/**
	 * Instantiates a new UDP_Server handler object. The parameters must not be
	 * null.
	 */
	public UDP_Server(Connection connection) {

		logger.info("Server connecting on port " + connection.getMyNode().getServerPortNmr());

		this.connection = connection;
		this.connectedClients = new HashSet<String>();

		try {

			this.socket = new DatagramSocket(connection.getPortHandler().releaseAndReturnReservedServerPort());

			logger.info("Server connected on port " + socket.getLocalPort());

		} catch (SocketException e) {

			logger.error("Socket error while connecting server.", e);

			throw new NullPointerException("UDP_Server object couldn´t be fully initialized due SocketException.");
		}

		this.threadPool = Executors.newFixedThreadPool(4);

		for (int i = 0; i < 1; ++i) {
			this.threadPool.execute(new ServerWorker(this));

		}

	}

	/**
	 * Returns the MessageProcessor object
	 * 
	 * @return
	 */
	public synchronized MessageProcessor getMessageProcessor() {
		return messageProcessor;
	}

	/**
	 * Sets the MessageProcessor object in the UDP_Server object.
	 * 
	 * @param messageProcessor
	 */
	public synchronized void setMessageProcessor(MessageProcessor messageProcessor) {

		if (messageProcessor == null)
			throw new NullPointerException("MessageProcessor object can´t be null");

		this.messageProcessor = messageProcessor;
	}

	/**
	 * Returns the DatagramSocket object.
	 * 
	 * @return
	 */
	public synchronized DatagramSocket getSocket() {
		return socket;
	}

	/**
	 * Sets the
	 * 
	 * @param socket DatagramSocket object.
	 */
	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	/**
	 * Returns the Connection object.
	 * 
	 * @return
	 */
	public synchronized Connection getConnection() {
		return this.connection;
	}

	/**
	 * Sets the Connection object.
	 * 
	 * @return
	 */
	public synchronized Set<String> getConnectedClients() {
		return connectedClients;
	}

}
