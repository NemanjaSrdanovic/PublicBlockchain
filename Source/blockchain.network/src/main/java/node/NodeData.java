package node;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The NodeData object is used to send/receive network data to/from other Nodes.
 * It contains transaction pool data (list of transactions), wallet data
 * (hashmap with wallet address and public key) and blockchian (Object
 * containing all mined blocks).
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class NodeData implements Serializable {

	private static final long serialVersionUID = 1L;
	private Object transactionPool;
	private HashMap<String, String> walletsData;
	private Object blockchain;

	/**
	 * Instantiates a new NodeData object. The parameters must not be null.
	 * 
	 * @param transactionPool
	 * @param walletsData
	 * @param blockchain
	 */
	public NodeData(Object transactionPool, HashMap<String, String> walletsData, Object blockchain) {
		super();
		this.transactionPool = transactionPool;
		this.walletsData = walletsData;
		this.blockchain = blockchain;
	}

	/**
	 * Returns a list of transactions representing the transaction pool which the
	 * node has saved in his database.
	 * 
	 * @return
	 */
	public Object getTransactionPool() {
		return transactionPool;
	}

	/**
	 * Returns a hash map with a pair of wallet address and public key as hex string
	 * representing the public keys and the addresses that a node has saved in his
	 * database.
	 * 
	 * @return
	 */
	public HashMap<String, String> getWalletsData() {
		return walletsData;
	}

	/**
	 * Returns the Blockchain object containing all mined blocks that the node has
	 * saved in his database as an representation of the current blockchain.
	 * 
	 * @return
	 */
	public Object getBlockchain() {
		return blockchain;
	}

}
