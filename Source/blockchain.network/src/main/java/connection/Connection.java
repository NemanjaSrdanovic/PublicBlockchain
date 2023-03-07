package connection;

import controllers.IpPortHandler;
import node.Node;
import udp_connection.UDP_Client;
import udp_connection.UDP_Server;

/**
 * This connection object is used by the node to initialise and access the
 * communication classes (UDP_Server/Client).
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 *
 */
public class Connection implements Runnable {

	private Node myNode;
	private IpPortHandler portHandler;
	private UDP_Server server;
	private UDP_Client client;

	/**
	 * Instantiates a new connection object.
	 */
	public Connection() {

		this.myNode = new Node();
		this.portHandler = new IpPortHandler(myNode);
		this.server = new UDP_Server(this);
		this.client = new UDP_Client(this);

	}

	/**
	 * 
	 * Returns the server object which receives messages and forwards them to the
	 * message processor.
	 *
	 * @return UDP_Server object initialised by Connection.
	 */
	public synchronized UDP_Server getServer() {

		if (this.server == null)
			throw new NullPointerException("Called UDP_Server object is null.");

		return server;
	}

	/**
	 * Returns the client object which sends messages to the connected server nodes.
	 * 
	 * @return UDP_Client object initialised by Connection.
	 */
	public synchronized UDP_Client getClient() {
		return client;
	}

	/**
	 * Used by the ConnectionHandler object to start new thread.
	 */
	@Override
	public void run() {

		// Running while connectionHandler not cancelled.
	}

	/**
	 * Returns node object which contains informations about the socket port and Ip
	 * address of the running connection.
	 * 
	 * @return
	 */
	public Node getMyNode() {
		return myNode;
	}

	/**
	 * Returns IpPortHandler object which manipulates the Ip address and port of the
	 * connection.
	 * 
	 * @return
	 */
	public IpPortHandler getPortHandler() {
		return portHandler;
	}

}
