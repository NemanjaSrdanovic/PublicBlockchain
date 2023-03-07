package blockchain.chain;

import java.io.Serializable;
import java.util.LinkedList;

import blockchain.block.Block;

/**
 * This Blockchain object is used to store block containing transactions.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class Blockchain implements Serializable {

	private static final long serialVersionUID = 1L;
	private LinkedList<Block> blockChain;

	/**
	 * Instantiates a new blockchain object.
	 */
	public Blockchain() {
		super();
		this.blockChain = new LinkedList<>();
	}

	/**
	 * Adds new block to blockchain object.
	 * 
	 * @param block
	 */
	public void addBlockToBlockchain(Block block) {

		blockChain.add(block);

	}

	/**
	 * Returnes linked list containing all mined blocks.
	 * 
	 * @return
	 */
	public LinkedList<Block> getChain() {
		return blockChain;
	}

	/**
	 * Implements a working toString method for this object.
	 */
	@Override
	public String toString() {
		return "Blockchain [\n" + blockChain + "\n]";
	}

}
