package network;

import java.net.UnknownHostException;

import controllers.ConnectionHandler;

/**
 * Starts Network node and connects to other running nodes.
 *
 */
public class App {
	public static void main(String[] args) throws UnknownHostException {

		ConnectionHandler connectionHandler = new ConnectionHandler();
		connectionHandler.run();

	}
}
