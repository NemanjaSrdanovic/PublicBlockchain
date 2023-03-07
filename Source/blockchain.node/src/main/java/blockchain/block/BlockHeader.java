package blockchain.block;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This block header object is part of the block object and used to store
 * block/chain specific data.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
@SuppressWarnings("serial")
public class BlockHeader implements Serializable {

	private String previousBlockHash;
	private String merkleRootHash;
	private String timeStamp;
	private int difficulty;
	private int nounce;

	/**
	 * Instantiates a new block header object. The parameters must not be null.
	 * 
	 * @param previousBlockHash
	 * @param merkleRootHash
	 * @param difficulty
	 */
	public BlockHeader(String previousBlockHash, String merkleRootHash, int difficulty) {
		super();
		this.previousBlockHash = previousBlockHash;
		this.merkleRootHash = merkleRootHash;

		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
		this.timeStamp = date.format(new Date());
		this.nounce = 0;
		this.difficulty = difficulty;

	}

	/**
	 * Returns the hash of the prevoius block in chain.
	 * 
	 * @return
	 */
	public String getPreviousBlockHash() {
		return previousBlockHash;
	}

	/**
	 * Returns the merkle root calculated for the transactions in this block.
	 * 
	 * @return
	 */
	public String getMerkleRootHash() {
		return merkleRootHash;
	}

	/**
	 * Returns a string containing the header data which is used to calculate the
	 * block hash.
	 * 
	 * @return
	 */
	public String getHeaderDataForHashCalculation() {

		return this.previousBlockHash + this.merkleRootHash + this.timeStamp + this.difficulty + this.nounce;
	}

	/**
	 * Returns the time and date when this block header object was created.
	 * 
	 * @return
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets time Stamp for this BlockHeader object
	 * 
	 * @param timeStamp
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Returns the difficulty used to mine the block which contains this block
	 * header.
	 * 
	 * @return
	 */
	public int getDifficulty() {
		return difficulty;
	}

	/**
	 * Returns the nounce which in combination with the block header gave the
	 * correct hash difficulty for the block containing this block header.
	 * 
	 * @return
	 */
	public int getNounce() {
		return nounce;
	}

	/**
	 * Sets the nounce for this block header.
	 * 
	 * @param nounce
	 */
	public void setNounce(int nounce) {
		this.nounce = nounce;
	}

	/**
	 * Implements a working toString method for this object.
	 */
	@Override
	public String toString() {
		return "previousBlockHash:" + previousBlockHash + "\n" + "merkleRootHash:" + merkleRootHash + "\n"
				+ "timeStamp:" + timeStamp + "\n" + "difficulty:" + difficulty + "\n" + "nounce:" + nounce;
	}

}
