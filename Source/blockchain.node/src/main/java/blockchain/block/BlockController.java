package blockchain.block;

import java.util.ArrayList;

import blockchain.concensus.PoW;
import blockchain.concensus.SHA256Hasher;

/**
 * This block controller object is used to control and initialise the new block
 * objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class BlockController {

	/**
	 * Instantiates a new block controller object. The parameters must not be null.
	 * 
	 * @param concensusAlg
	 */
	public BlockController() {
		super();

	}

	/**
	 * Generates a new block object that can be added to the blockchain.
	 * 
	 * @param previousBlockHash --> hash of the previous block in the blockchain.
	 * @param transactions      --> list of transactions that this block object will
	 *                          contain.
	 * @param blockIndex        --> the index in the blockchain that this block
	 *                          object will have.
	 * @return
	 */
	public Block generateNewBlock(int blockIndex, BlockHeader blockHeader,
			ArrayList<Transaction> transactionListToBeIncludedInBlock) {

		if (blockHeader == null || transactionListToBeIncludedInBlock == null)
			throw new IllegalArgumentException("Block can´t be initialized with null values");

		Block block = new Block(blockIndex, blockHeader, transactionListToBeIncludedInBlock,
				transactionListToBeIncludedInBlock.size());

		return block;
	}

	/**
	 * Generates a new BlockHeader object to be included in the block object.
	 * 
	 * @param previousBlock
	 * @param transactionListToBeIncludedInBlock
	 * @return
	 */
	public BlockHeader generateNewBlockHeader(Block previousBlock,
			ArrayList<Transaction> transactionListToBeIncludedInBlock) {

		if (previousBlock == null)
			throw new IllegalArgumentException("Previous block can´t be null");

		MerkleTree merkleTree = new MerkleTree(transactionListToBeIncludedInBlock);

		return new BlockHeader(calculateBlockHeaderHash(previousBlock.getBlockHeader()), merkleTree.getMerkleRoot(),
				PoW.getDifficulty());

	}

	/**
	 * Calculates the hash of the block header provided by using the sha256 eclipse
	 * function.
	 * 
	 * @param blockHeader
	 * @return
	 */
	public String calculateBlockHeaderHash(BlockHeader blockHeader) {

		return SHA256Hasher.returnSHA256HashStringFromString(blockHeader.getHeaderDataForHashCalculation());
	}

}
