package udp_connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constraints.Constraints;
import marshaller.Marshaller;
import messages.Message;

/**
 * This ClientWorker object is constantly running and depending on the
 * connection state trying to connect the node to other nodes or fetching
 * messages from the pool and sending them to the connected nodes.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class ClientWorker implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(ClientWorker.class);
	private UDP_Client client;
	private DatagramPacket packet;
	private DatagramPacket recPacket;
	private byte[] buffer;
	private byte[] receivedData;
	private Marshaller marshaller;

	/**
	 * Instantiates a new ClientWorker object. The parameters must not be null.
	 * 
	 * @param client
	 * @throws SocketException
	 */
	public ClientWorker(UDP_Client client) throws SocketException {

		this.client = client;
		this.marshaller = new Marshaller();
	}

	/**
	 * Runs this ClientWorker object which is sending connection requests to other
	 * nodes in the network and when enough nodes are connected removes messages
	 * from the waiting queue and sends them to all connected nodes over Datagram
	 * sockets.
	 */
	@Override
	public void run() {

		while (true) {

			String ipPortOfRecipient;
			Message message = null;

			try {

				if (!client.isMaximalClientsConnected()) {

					client.getSocket().setSoTimeout(200);

					ipPortOfRecipient = client.randomIPAddressAndPortToConnect();
					sendGreeting(ipPortOfRecipient);

				}

				if (client.getConnectedIPsPort().size() >= Constraints.MIN_NODES_CONNECTED_TO_SEND) {
					message = client.getMessage();

				}

				if (message != null) {

					client.sendMessage(message, buffer, packet, marshaller, recPacket, receivedData);

				}

			} catch (IOException | SQLException e) {

				logger.error("Exception while running ClientWorker.", e);
			}
		}
	}

	/**
	 * Sends connection requests to all nodes in the network and saves their ip:port
	 * address if they respond to the connection requests.
	 * 
	 * @param ipPortOfRecipient
	 * @throws IOException
	 */
	private void sendGreeting(String ipPortOfRecipient) throws IOException {

		String message = "Hello";
		buffer = message.getBytes();

		packet = marshaller.makeDatagramPacket(message, buffer, ipPortOfRecipient);

		client.getSocket().send(packet);

		receivedData = new byte[1024];
		this.recPacket = new DatagramPacket(receivedData, receivedData.length);

		try {

			client.getSocket().receive(this.recPacket);

			client.setIPandPortOfRandomNodes(this.recPacket, null);

		} catch (InterruptedIOException e) {

			/**
			 * Mostly the servers won't respond on time and this Exception will be executed
			 * a few times. The exception will be caught in this part but not processed
			 * because the Worker will continue until a server respondes on the connection
			 * request.
			 */

		}

	}
}
