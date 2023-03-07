package controllers;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constraints.Constraints;
import node.Node;

/**
 * This IpPortHandler object is used to reserve a random port on the current Ip
 * address that will be used by the UDP_Server object.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 *
 */
public class IpPortHandler {

	private static Logger logger = LoggerFactory.getLogger(IpPortHandler.class);
	private String localIPAdress;
	private DatagramSocket reservedServerPort;
	private DatagramSocket reservedClientPort;
	private Node myNode;

	/**
	 * Instantiates a new IpPortHandler object.
	 */
	public IpPortHandler(Node myNode) {
		super();

		if (myNode == null)
			throw new NullPointerException("Node object can´t be null");

		this.myNode = myNode;
		this.reservedServerPort = null;
		this.reservedClientPort = null;
		this.localIPAdress = null;

		findMyIP();
		setMyPorts();

	}

	/**
	 * Finding the local (Ethernet) host address for the machine on which the
	 * network system is running.
	 * 
	 * @return IP adress as String (173.22.112.5)
	 */
	private void findMyIP() {

		try {

			localIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {

			logger.error("Inet Adress exception.", e);

		}

		if (localIPAdress == null || localIPAdress.isEmpty())
			throw new IllegalArgumentException("Your IP is null or empty string please " + "check your connection.");

		myNode.setIpAddress(localIPAdress);

	}

	/**
	 * Finds a random port between max and min port range, set in constraints and
	 * reserving it for the connection.
	 * 
	 * @return Port number as Integer (3025)
	 */
	private void setMyPorts() {

		Random random = new Random();
		int randomServerPortInRange;
		int randomClientPortInRange;

		while (reservedServerPort == null || reservedClientPort == null) {

			randomServerPortInRange = random.nextInt(Constraints.PORTNUM_MAX - Constraints.PORTNUM_MIN + 1)
					+ Constraints.PORTNUM_MIN;

			randomClientPortInRange = randomServerPortInRange + Constraints.CLIENT_SOCKET_ADDITION;

			try {
				reservedServerPort = new DatagramSocket(randomServerPortInRange);
				reservedClientPort = new DatagramSocket(randomClientPortInRange);
			} catch (SocketException e) {

				logger.info("Connection to port...");

			}

		}

		myNode.setClientPortNmr(reservedClientPort.getLocalPort());
		myNode.setServerPortNmr(reservedServerPort.getLocalPort());

	}

	/**
	 * Releases reserved port, so that the initialised UDP_Client/Server can connect
	 * to it.
	 * 
	 */
	public int releaseAndReturnReservedServerPort() {
		reservedServerPort.close();
		return myNode.getServerPortNmr();
	}

	/**
	 * Releases reserved port, so that the initialised UDP_Client/Server can connect
	 * to it.
	 */
	public int releaseAndReturnReservedClientPort() {
		reservedClientPort.close();
		return myNode.getClientPortNmr();
	}

}
