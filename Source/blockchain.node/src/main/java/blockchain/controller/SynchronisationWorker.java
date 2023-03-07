package blockchain.controller;

import constraints.Constraints;

/**
 * This SynchronisationWorker object starts whenever a node is started to
 * receive and synchronise the data from the network (Blockchain, Transaction
 * pool, Public keys .. )
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class SynchronisationWorker implements Runnable {

	private NodeController nodeController;
	private boolean dataSynchronized;

	/**
	 * Instantiates a new SynchronisationWorker object. The parameters must not be
	 * null.
	 * 
	 * @param nodeController
	 */
	public SynchronisationWorker(NodeController nodeController) {
		super();
		this.nodeController = nodeController;
		this.dataSynchronized = false;
	}

	/**
	 * If the node is connected to a minimal amount of nodes that enable him to
	 * propagate his messages two messages are send to the network. The first
	 * contains his wallet address and his public key and the second is a request
	 * for the rest of the nodes to send him the blockchain data they have.
	 */
	@Override
	public void run() {

		while (!dataSynchronized) {

			if (this.nodeController.getConnectionHandler().getConnection().getClient().getConnectedIPsPort()
					.size() >= Constraints.MIN_NODES_CONNECTED_TO_SEND) {

				this.nodeController.sendWalletData();
				this.nodeController.requestNetworkData();

				dataSynchronized = true;

			}

		}

	}

}
