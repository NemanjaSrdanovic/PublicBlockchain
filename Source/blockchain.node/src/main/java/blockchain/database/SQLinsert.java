package blockchain.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.Transaction;
import marshaller.Marshaller;

/**
 * This SQLinsert object establishes the connection to the sqlite database and
 * inserts data into the tables created by the DriverClass object.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class SQLinsert {

	private static Logger logger = LoggerFactory.getLogger(SQLinsert.class);
	private Connection connection;
	private String database;

	/**
	 * Instantiates a new SQLinsert object. The parameters must not be null.
	 * 
	 * @param base
	 */
	public SQLinsert(String base) {
		try {

			this.database = base;
			connection = DriverManager.getConnection("jdbc:sqlite:.\\" + database);

		} catch (SQLException e) {

			logger.error("SQLinsert initialization exception.", e);
		}
	}

	/**
	 * Inserts the input transaction into the transactionPool Table that contains: |
	 * TransactionID | TimeStamp | FromAdress | ToAdress | Amount | Signature |
	 * 
	 * @param transaction
	 */
	public void insertTransactionIntoPool(Transaction transaction) {

		try {
			PreparedStatement stmt = connection
					.prepareStatement("INSERT OR IGNORE INTO transactionPool values(?,?,?,?,?,?)");
			stmt.setString(1, transaction.getTransactionID());
			stmt.setString(2, transaction.getTimeStamp());
			stmt.setString(3, transaction.getFromAdress());
			stmt.setString(4, transaction.getToAdress());
			stmt.setDouble(5, transaction.getAmount());
			stmt.setString(6, transaction.getSignature());

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Transaction insert into transaction pool exception.", e);

		}

	}

	/**
	 * Inserts the input transaction into the minedTransactions Table and connects
	 * int with the index of the block in which this transaction is contained. The
	 * table receives: | BlockIndex | TransactionID |TimeStamp |FromAdress |ToAdress
	 * | Amount |Signature|
	 * 
	 * @param blockIndex
	 * @param transaction
	 */
	public void insertTransactionIntoMinedTransactions(int blockIndex, Transaction transaction) {

		try {
			PreparedStatement stmt = connection
					.prepareStatement("INSERT OR IGNORE INTO minedTransactions values(?,?,?,?,?,?,?)");
			stmt.setInt(1, blockIndex);
			stmt.setString(2, transaction.getTransactionID());
			stmt.setString(3, transaction.getTimeStamp());
			stmt.setString(4, transaction.getFromAdress());
			stmt.setString(5, transaction.getToAdress());
			stmt.setDouble(6, transaction.getAmount());
			stmt.setString(7, transaction.getSignature());

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Transaction insert into mined transactions exception.", e);

		}

	}

	/**
	 * Inserts the wallet address and his public key into the publicKeys table that
	 * receives following data: | WalletAdress | WalletKeyHexString |
	 * 
	 * @param walletAdress
	 * @param walletKeyHexString
	 */
	public void insertWalletKeyIntoDatabase(String walletAdress, String walletKeyHexString) {

		try {

			PreparedStatement stmt = connection.prepareStatement("INSERT OR IGNORE INTO publicKeys values(?,?)");
			stmt.setString(1, walletAdress);
			stmt.setString(2, walletKeyHexString);

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Wallet data insert exception.", e);

		}

	}

	/**
	 * Insert the input block into the blockChain Table which receives following
	 * data: | BlockIndex | BlockHeader | TransactionList | TransactionCounter |
	 * 
	 * @param block
	 */
	public void insertBlockIntoBlockchain(Block block) {

		try {

			Marshaller marshaller = new Marshaller();
			byte[] transformedBlockHeader = marshaller.transformObjectToByte(block.getBlockHeader());
			byte[] transformedTransactionList = marshaller.transformObjectToByte(block.getTransactionList());

			PreparedStatement stmt = connection.prepareStatement("INSERT OR IGNORE INTO blockChain values(?,?,?,?,?)");
			stmt.setInt(1, block.getIndex());
			stmt.setString(2, block.getTimeStamp());
			stmt.setBytes(3, transformedBlockHeader);
			stmt.setBytes(4, transformedTransactionList);
			stmt.setInt(5, block.getTransactionCounter());

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Block insertion exception.", e);

		}

	}

	/**
	 * Replaces existing block from the blockchain table with the input block. Data:
	 * | BlockIndex | BlockHeader | TransactionList | TransactionCounter |
	 * 
	 * @param block
	 */
	public void replaceBlockFromBlockchain(Block block) {

		try {

			Marshaller marshaller = new Marshaller();
			byte[] transformedBlockHeader = marshaller.transformObjectToByte(block.getBlockHeader());
			byte[] transformedTransactionList = marshaller.transformObjectToByte(block.getTransactionList());

			PreparedStatement stmt = connection.prepareStatement(
					"REPLACE INTO blockChain (blockIndex, miningTimeStamp, blockHeader, transactions, transactionCounter) values(?,?,?,?,?)");

			stmt.setInt(1, block.getIndex());
			stmt.setString(2, block.getTimeStamp());
			stmt.setBytes(3, transformedBlockHeader);
			stmt.setBytes(4, transformedTransactionList);
			stmt.setInt(5, block.getTransactionCounter());

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Block replacement exception.", e);

		}

	}
}
