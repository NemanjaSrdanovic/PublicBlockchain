package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import connection.Connection;
import messageProcessor.MessageProcessor;

/**
 * This connection handler object is used to control and initialise the
 * connection and its parameters.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class ConnectionHandler implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);
	private Connection connection;

	/**
	 * Instantiates a new connection handler object.
	 */
	public ConnectionHandler() {
		super();

		try {

			this.connection = new Connection();

		} catch (IllegalArgumentException eA) {

			logger.error("Exception while trying Connection initialization.", eA);

		} catch (NullPointerException eN) {

			logger.error("Exception while trying Connection initialization.", eN);
		}
	}

	/**
	 * Returns connections object containing communication objects (client and
	 * server.
	 * 
	 * @return -> Connection object.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Assigns the object implementing the MessageProcessor interface to the
	 * UDP_Server so that he can forward received messages to that object.
	 * 
	 * @param messageProcessor-> Class implementing the MessageProcessor interface.
	 */
	public synchronized void setMessageProcessor(MessageProcessor messageProcessor) {

		try {

			getConnection().getServer().setMessageProcessor(messageProcessor);

		} catch (NullPointerException e) {

			logger.error("Exception while setting message processor.", e);
		}

	}

	/**
	 * Starts a new thread in which the ConnectionHandler/Connection (servers and
	 * clients) are running.
	 */
	@Override
	public void run() {

		Thread connectionThread = new Thread(this.connection);
		connectionThread.start();

	}

}
