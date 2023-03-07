package node;

/**
 * This node object is used by the network system to save data regarding the Ip
 * adress and port of the connection.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class Node {

	private String ipAddress;
	private int serverPortNmr;
	private int clientPortNmr;

	/**
	 * Instantiates a new node object. The parameters must not be null.
	 * 
	 * @param ipAddress
	 * @param portNmr
	 */
	public Node() {

	}

	/**
	 * Returns the Ip address of the current connection.
	 * 
	 * @return-> String in format 173.192.121.1
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the Ip address of the current connection.
	 * 
	 * @param ipAddress-> String in format 173.192.121.1
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Returns port to which the UDP_Server of the current instance is connected.
	 * 
	 * @return -> int in format 3025
	 */
	public int getServerPortNmr() {
		return serverPortNmr;
	}

	/**
	 * Returns port to which the UDP_Client of the current instance is connected.
	 * 
	 * @return -> int in format 3055
	 */
	public int getClientPortNmr() {
		return clientPortNmr;
	}

	/**
	 * Sets port to which the UDP_Server of the current instance is connected.
	 * 
	 * @param serverPortNmr -> int in format 3025
	 */
	public void setServerPortNmr(int serverPortNmr) {
		this.serverPortNmr = serverPortNmr;
	}

	/**
	 * Sets port to which the UDP_Client of the current instance is connected.
	 * 
	 * @param clientPortNmr -> int in format 3055
	 */
	public void setClientPortNmr(int clientPortNmr) {
		this.clientPortNmr = clientPortNmr;
	}

	/**
	 * Implements a working toString method for this object.
	 */
	@Override
	public String toString() {
		return "Node [ipAddress=" + ipAddress + ", serverPortNmr=" + serverPortNmr + ", clientPortNmr=" + clientPortNmr
				+ "]";
	}

}
