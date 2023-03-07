package blockchain.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;

/**
 * This DriverClass object establishes the connection to the sqlite database,
 * creates the tables for saving node and network related data and
 * inserts/retrieves/deletes entyties from the database. Prevents using multiple
 * objects (insert/select/delete) to edit data in the database, by encapsulating
 * the methods which those objects provide.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class DriverClass {

	private static Logger logger = LoggerFactory.getLogger(DriverClass.class);
	private Connection connection;
	private Statement statement;
	private String database;
	private SQLinsert insert;
	private SQLselect select;
	private SQLdelete delete;

	/**
	 * Instantiates a new DriverClass object, sets connection to the database and
	 * calls a function to create the tables in the database. The parameters must
	 * not be null.
	 * 
	 * @param port
	 */
	public DriverClass(int port) {
		try {

			String portDB = Integer.toString(port);
			this.database = "Database" + portDB + ".db";
			this.connection = DriverManager.getConnection("jdbc:sqlite:.\\" + database);
			this.connection.setAutoCommit(false);

			this.createNodeTables();
			this.clearTables();

			insert = new SQLinsert(database);
			select = new SQLselect(database);
			delete = new SQLdelete(database);

		} catch (SQLException e) {

			logger.error("Driver class initialization exception.", e);
		}
	}

	/**
	 * Creates tables which store the node/network related data as transaction pool,
	 * blockchain/blocks, public keys and wallet addresses of other nodes.
	 */
	private void createNodeTables() {

		try {
			connection = DriverManager.getConnection("jdbc:sqlite:.\\" + database);

			connection.setAutoCommit(false);
			statement = connection.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS transactionPool (" + "transactionId varchar(100) primary key,"
					+ "timeStamp varchar(100)," + "fromAdress varchar(100)," + "toAdress varchar(100),"
					+ "amount DOUBLE PRECISION," + "signature varchar(100))");
			statement.execute("CREATE TABLE IF NOT EXISTS publicKeys (" + "walletAdress varchar(100) primary key,"
					+ "publicKey varchar(100))");
			statement.execute("CREATE TABLE IF NOT EXISTS blockChain (" + "blockIndex INTEGER primary key,"
					+ "miningTimeStamp varchar(100)," + "blockHeader VARBINARY," + "transactions VARBINARY,"
					+ "transactionCounter INTEGER)");
			statement.execute("CREATE TABLE IF NOT EXISTS minedTransactions (" + "includedInBlockWithIndex INTEGER,"
					+ "transactionId varchar(100) primary key," + "timeStamp varchar(100)," + "fromAdress varchar(100),"
					+ "toAdress varchar(100)," + "amount INTEGER," + "signature varchar(100))");

			statement.close();
			connection.commit();

		} catch (SQLException e) {

			logger.error("Table creation exception.", e);
		}

	}

	/**
	 * Delets all entries from all tables in the database.
	 */
	private void clearTables() {

		String sqlClearTransactionPool = "DELETE FROM transactionPool";
		String sqlClearPublicKeys = "DELETE FROM publicKeys";
		String blockChain = "DELETE FROM blockChain";
		String minedTransactions = "DELETE FROM minedTransactions";

		try {
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			statement.execute(sqlClearTransactionPool);
			statement.execute(sqlClearPublicKeys);
			statement.execute(blockChain);
			statement.execute(minedTransactions);
			statement.close();
			connection.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Provides possibility to access the functions of the SQLinsert object without
	 * directly exposing the object to all classes. Calling a function which inserts
	 * transactions into the transaction pool from which the objects will be taken
	 * to be inserted into a block.
	 * 
	 * @param transaction
	 */
	public synchronized void insertTransactionIntoTransactionPool(Transaction transaction) {

		this.insert.insertTransactionIntoPool(transaction);

	}

	/**
	 * Provides possibility to access the functions of the SQLinsert object without
	 * directly exposing the object to all classes. Calling a function which inserts
	 * the wallet address into the database and links it´s public key to it.
	 * 
	 * @param walletAdress
	 * @param walletKeyHexString
	 */
	public synchronized void insertWalletKeyIntoDatabase(String walletAdress, String walletKeyHexString) {

		this.insert.insertWalletKeyIntoDatabase(walletAdress, walletKeyHexString);

	}

	/**
	 * Provides possibility to access the functions of the SQLinsert object without
	 * directly exposing the object to all classes. Calling a function which inserts
	 * the block into a table which represents the blockchain where every block has
	 * it´s unique index.
	 * 
	 * @param block
	 */
	public synchronized void insertBlockIntoBlockchain(Block block) {

		this.insert.insertBlockIntoBlockchain(block);

	}

	/**
	 * Provides possibility to access the functions of the SQLinsert object without
	 * directly exposing the object to all classes. Calling a function which
	 * replaces a existing block from the blockchain table with the block input.
	 * 
	 * @param block
	 */
	public synchronized void replaceBlockFromBlockchain(Block block) {

		try {

			this.delete.deleteAllTransactionsFromMinedTransactionsWhereBlockIndex(block.getIndex());
			this.insert.replaceBlockFromBlockchain(block);
			this.moveMinedTransactionsFromPool(block);

		} catch (Exception e) {

			logger.error("Exception while replacing block in blockchain.", e);
		}
	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * all transaction from the transaction pool where the creation/sending of that
	 * transaction was before a specific date/time.
	 * 
	 * @return
	 */
	public synchronized ArrayList<Transaction> getAllTransactionsFromTransactionPoolWhereTimestampBefore(
			String inputTime) {

		return this.select.getAllTransactionsFromTransactionPoolWhereTimestampBefore(inputTime);
	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * all transaction from the transaction pool and returning them in a list.
	 * 
	 * @return
	 */
	public synchronized ArrayList<Transaction> getAllTransactionsFromTransactionPool() {

		return this.select.getAllTransactionsFromTransactionPool();
	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * all wallet addresses and their corresponding public keys and returning them
	 * as a hash map where the wallet address is the key and the public key the
	 * value.
	 * 
	 * @return
	 */
	public synchronized HashMap<String, String> getAllPublicKeysFromRegister() {

		return this.select.getAllPublicKeysFromRegister();
	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * all saved blocks in the blockchain table and returning them as a Blockchain
	 * object.
	 * 
	 * @return
	 */
	public synchronized Blockchain getAllBlocksFromBlockchain() {

		return this.select.getAllBlocksFromBlockchain();
	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * the block from the blockchain table that has the biggest index (is the last
	 * block in the chain).
	 * 
	 * @return
	 */
	public synchronized Block getLastBlockFromBlockchain() {

		return this.select.getLastBlockFromBlockchain();
	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * the block from the blockchain table that has the give input block index.
	 * 
	 * @return
	 */
	public synchronized Block getBlockWithInputIndex(int blockIndexInput) {

		return this.select.getBlockWithInputIndex(blockIndexInput);

	}

	/**
	 * Provides possibility to access the functions of the SQLselect object without
	 * directly exposing the object to all classes. Calling a function which selects
	 * the public key for a corresponding wallet address and returns it in a hex
	 * string form.
	 * 
	 * @param walletAdress
	 * @return
	 */
	public synchronized String getHexStringPublicKeyForCorrespondingWalletAddress(String walletAdress) {

		return this.select.getHexStringPublicKeyForCorrespondingWalletAddress(walletAdress);
	}

	/**
	 * Method that receives a newly mined block and removes the transaction that
	 * this block contains from the transaction pool and inserts them into the table
	 * which contains transactions that were already mined (included in a
	 * block/chain).
	 * 
	 * @param block
	 */
	public synchronized void moveMinedTransactionsFromPool(Block block) {

		if (block == null)
			throw new IllegalArgumentException("Block can´t be null.");

		for (Transaction transaction : block.getTransactionList()) {

			this.delete.deleteTransactionFromTransactionPool(transaction);
			this.insert.insertTransactionIntoMinedTransactions(block.getIndex(), transaction);
		}

	}

	/**
	 * Closes the connection to the database.
	 */
	public void closeDbConnection() {
		try {
			statement.close();
			connection.close();
		} catch (SQLException e) {

			logger.error("Database closing exception.", e);
		}
	}

}
