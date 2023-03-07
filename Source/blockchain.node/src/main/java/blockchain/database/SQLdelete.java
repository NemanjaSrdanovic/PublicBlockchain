
package blockchain.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Transaction;

/**
 * This SQLdelete object establishes the connection to the sqlite database and
 * deletes data from tables in the database.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class SQLdelete {

	private static Logger logger = LoggerFactory.getLogger(SQLselect.class);
	private Connection connection;
	private String database;

	/**
	 * Instantiates a new SQLdelete object. The parameters must not be null.
	 * 
	 * @param base
	 */
	public SQLdelete(String base) {
		try {

			this.database = base;
			connection = DriverManager.getConnection("jdbc:sqlite:.\\" + database);

		} catch (SQLException e) {

			logger.error("SQLdelete initialization exception.", e);
		}
	}

	/**
	 * Removes the input transaction from the table transactionPool by comparing the
	 * transactionId´s of the input transaction and input transaction.
	 * 
	 * @param transaction
	 */
	public void deleteTransactionFromTransactionPool(Transaction transaction) {

		try {

			PreparedStatement stmt = connection.prepareStatement(
					"DELETE FROM transactionPool WHERE transactionId=" + "'" + transaction.getTransactionID() + "'");

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Exception while deleting transaction from transaction pool sql delete.", e);
		}
	}

	/**
	 * Removes all transactions from the table minedTransactions where the
	 * includedInBlockWithIndex matches the inputBlockIndex.
	 * 
	 * @param inputBlockIndex
	 */
	public void deleteAllTransactionsFromMinedTransactionsWhereBlockIndex(int inputBlockIndex) {

		try {

			PreparedStatement stmt = connection
					.prepareStatement("DELETE FROM minedTransactions WHERE includedInBlockWithIndex=" + "'"
							+ String.valueOf(inputBlockIndex) + "'");

			stmt.executeUpdate();

		} catch (Exception e) {

			logger.error("Exception while deleting transaction from minedTransactions pool sql delete.", e);
		}

	}

}