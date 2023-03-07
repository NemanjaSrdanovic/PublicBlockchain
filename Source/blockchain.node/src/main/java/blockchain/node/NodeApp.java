package blockchain.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.controller.NodeController;

/**
 * Starts a new blockchian node.
 *
 */
public class NodeApp {

	private static Logger logger = LoggerFactory.getLogger(NodeApp.class);

	public static void main(String[] args) throws Exception {

		logger.info("Starting blockchain node...");

		NodeController nodeController = new NodeController();
		nodeController.start();

	}
}
