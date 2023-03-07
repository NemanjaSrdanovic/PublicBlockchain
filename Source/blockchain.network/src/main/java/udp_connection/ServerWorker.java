package udp_connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constraints.Constraints;
import marshaller.Marshaller;
import messages.Message;

/**
 * This ServerWorker object is constantly running with the purpose to get
 * datagram packets from the connected socket and extract the messages from
 * them. The extracted message is then forwarded to the node running this
 * network component which decides how that message will be handled.
 * 
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class ServerWorker implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(ServerWorker.class);
	private Message message;
	private UDP_Server udpServer;
	private Marshaller marshaller;
	private DatagramPacket requestPacket;
	private DatagramPacket packetToSend;
	private byte[] buffer;
	private int bufferSize;
	private final int bufferSizeIncrementValue;

	/**
	 * Instantiates a new ServerWorker object. The parameters must not be null.
	 * 
	 * @param udpServer
	 */
	public ServerWorker(UDP_Server udpServer) {

		this.udpServer = udpServer;
		this.marshaller = new Marshaller();
		this.bufferSize = 200000;
		this.bufferSizeIncrementValue = 3;

	}

	/**
	 * Runs this ServerWorker object which is constantly running and receiving
	 * messages send to the socket on which the UDP_Server object is connected.Upon
	 * receiving the message the ServerWorker is responding to them or forwarding
	 * them to the Node which uses this Network project.
	 */
	@Override
	public void run() {

		while (true) {

			if (udpServer.getMessageProcessor() != null) {

				try {

					String toResponse = "200 OK";

					buffer = new byte[getBufferSize()];

					this.requestPacket = new DatagramPacket(buffer, buffer.length);

					Arrays.fill(buffer, (byte) 0);

					buffer = toResponse.getBytes();

					udpServer.getSocket().receive(this.requestPacket);

					if (this.requestPacket.getLength() >= (getBufferSize() / this.bufferSizeIncrementValue)) {

						setBufferSize(getBufferSize() * this.bufferSizeIncrementValue);
					}

					Object recObject = marshaller.makeObjectFrom(requestPacket);

					packetToSend = marshaller.makeDatagramPacket(toResponse, buffer,
							requestPacket.getAddress().getHostAddress() + ":"
									+ Integer.toString(requestPacket.getPort()));

					if (recObject instanceof String
							&& !udpServer.getConnectedClients().contains(
									requestPacket.getAddress().getHostAddress() + ":" + requestPacket.getPort())
							&& udpServer.getConnectedClients().size() < Constraints.MAX_CONNECTED_CLIENT_NODES) {

						udpServer.getConnection().getClient().setIPandPortOfRandomNodes(null, requestPacket);

						udpServer.getSocket().send(packetToSend);

					}

					if (recObject instanceof Message && udpServer.getConnectedClients()
							.contains(requestPacket.getAddress().getHostAddress() + ":" + requestPacket.getPort())) {

						message = (Message) recObject;

						udpServer.getMessageProcessor().onMessage(message);

						udpServer.getSocket().send(packetToSend);
					}

				} catch (IOException e) {

					logger.error("Server worker exception", e);

				}

			}

		}
	}

	private int getBufferSize() {
		return bufferSize;
	}

	private void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

}