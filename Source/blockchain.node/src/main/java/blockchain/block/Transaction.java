package blockchain.block;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import blockchain.concensus.SHA256Hasher;

/**
 * This transaction object contains all data concerning asset transferring
 * between two entities and is saved in blocks of the blockchain where they are
 * visible to all.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
@SuppressWarnings("serial")
public class Transaction implements Serializable {

	private String transactionID;
	private String fromAdress;
	private String toAdress;
	private double amount;
	private String timeStamp;
	private String signature;

	/**
	 * Instantiates a new transaction object. The parameters must not be null.
	 * 
	 * @param fromAdress
	 * @param toAdress
	 * @param amount
	 */
	public Transaction(String fromAdress, String toAdress, double amount) {
		super();
		this.fromAdress = fromAdress;
		this.toAdress = toAdress;
		this.amount = amount;

		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
		this.timeStamp = date.format(new Date());
		this.transactionID = this.calculateTransactionID();
		this.signature = "sign";

	}

	/**
	 * Calculates the transaction ID by hashing the transaction data using the
	 * SHA256 hahs function.
	 * 
	 * @return
	 */
	public String calculateTransactionID() {

		return SHA256Hasher.returnSHA256HashStringFromString(this.getTransactionIdData());
	}

	/**
	 * Helper method that returns transaction data that are hashed to get the
	 * transaction id.
	 * 
	 * @return -> fromAdress, toAdress, amount, timeStamp
	 */
	public String getTransactionIdData() {

		return this.fromAdress + this.toAdress + this.amount + this.timeStamp;

	}

	/**
	 * Helper method that returns all transaction data to be recursively hashed to
	 * get the merkle root.
	 * 
	 * @return
	 */
	public String getTransactionData() {

		return this.fromAdress + this.toAdress + this.amount + this.timeStamp + this.transactionID + this.signature;
	}

	/**
	 * Returns unique Id for this transaction.
	 * 
	 * @return
	 */
	public String getTransactionID() {
		return transactionID;
	}

	/**
	 * Sets transaction id for this transaction object.
	 * 
	 * @param transactionID
	 */
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * Returns wallet adress of the wallet that send the founds/transaction.
	 * 
	 * @return
	 */
	public String getFromAdress() {
		return fromAdress;
	}

	/**
	 * Returns wallet adress of the wallet that receives the founds/transaction.
	 * 
	 * @return
	 */
	public String getToAdress() {
		return toAdress;
	}

	/**
	 * Return the amount of found send.
	 * 
	 * @return
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * Return the time and date when this transaction object was created.
	 * 
	 * @return
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the time and date when this transaction object was created.
	 * 
	 * @param timeStamp
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Return the signature with which this transaction object was signed.
	 * 
	 * @return
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets signature with which this transaction object was signed.
	 * 
	 * @param signature
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Implements a working toString method for this object.
	 */
	@Override
	public String toString() {
		return "\n\nTransaction [ \n" + "transactionID:" + transactionID + "\n" + "fromAdress:" + fromAdress + "\n"
				+ "toAdress:" + toAdress + "\n" + "amount:" + amount + "\n" + "timeStamp:" + timeStamp + "\n"
				+ "signature:" + signature + "]";
	}

}
