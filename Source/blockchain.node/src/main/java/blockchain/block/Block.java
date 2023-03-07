package blockchain.block;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 
 * This Block object is used to store transactions and other specific blockchain
 * data, which are set during the mining process of the object.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
@SuppressWarnings("serial")
public class Block implements Serializable {

	private int index;
	private BlockHeader blockHeader;
	private ArrayList<Transaction> transactionList;
	private int transactionCounter;
	private String timeStamp;

	/**
	 * Instantiates a new block object. The parameters must not be null.
	 * 
	 * @param index
	 * @param blockHeader
	 * @param transactionList
	 * @param transactionCounter
	 */
	public Block(int index, BlockHeader blockHeader, ArrayList<Transaction> transactionList, int transactionCounter) {
		super();
		this.index = index;
		this.blockHeader = blockHeader;
		this.transactionList = transactionList;
		this.transactionCounter = transactionCounter;
		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss.SSS");
		this.timeStamp = date.format(new Date());
	}

	/**
	 * Returns the index of this block in the blockchain.
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the blockheader object which contains specific blockchain related
	 * data.
	 * 
	 * @return
	 */
	public BlockHeader getBlockHeader() {
		return blockHeader;
	}

	/**
	 * Return a list of transactions that are added to this object during the mining
	 * process.
	 * 
	 * @return
	 */
	public ArrayList<Transaction> getTransactionList() {
		return transactionList;
	}

	/**
	 * Returns the amount of transactions that are added to this block during the
	 * mining process.
	 * 
	 * @return
	 */
	public int getTransactionCounter() {
		return transactionCounter;
	}

	/**
	 * Returns date and time when this Block object was generated.
	 * 
	 * @return
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets time stamp when this block was created.
	 * 
	 * @param timeStamp
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Implements a working toString method for this object.
	 */
	@Override
	public String toString() {
		return "\nBlock { \n" + "index:" + index + ";\n" + blockHeader + ";\n" + "transactionCounter:"
				+ transactionCounter + ";\n" + "timeStamp: " + timeStamp + ";\n" + "Transactions:\n" + transactionList
				+ ";\n" + "\n}";
	}
}
