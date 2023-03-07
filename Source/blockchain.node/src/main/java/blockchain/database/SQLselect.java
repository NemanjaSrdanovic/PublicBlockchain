package blockchain.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blockchain.block.Block;
import blockchain.block.BlockHeader;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;
import marshaller.Marshaller;

/**
 * This SQLselect object establishes the connection to the sqlite database and
 * fetches and returns data from the tables created by the DriverClass object
 * and filled by the SQLinsert object.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 10 Dec 2021
 */
public class SQLselect {

	private static Logger logger = LoggerFactory.getLogger(SQLselect.class);
	private Connection connection;
	private String database;

	/**
	 * Instantiates a new SQLselect object. The parameters must not be null.
	 * 
	 * @param base
	 */
	public SQLselect(String base) {
		try {

			this.database = base;
			connection = DriverManager.getConnection("jdbc:sqlite:.\\" + database);
		} catch (SQLException e) {

			logger.error("SQLselect initialization exception.", e);
		}
	}

	/**
	 * Fetching all transaction entries from database table transactionPool where
	 * time stamp before inputTime and returning all fetched transactions in a list.
	 * 
	 * @return
	 */
	public ArrayList<Transaction> getAllTransactionsFromTransactionPoolWhereTimestampBefore(String inputTime) {

		ArrayList<Transaction> transactionsFromTransactionPool = new ArrayList<Transaction>();

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement
					.executeQuery("SELECT * FROM transactionPool WHERE timeStamp <" + "'" + inputTime + "'");

			while (rs.next()) {

				String transactionId = rs.getString("transactionId");
				String timeStamp = rs.getString("timeStamp");
				String fromAdress = rs.getString("fromAdress");
				String toAdress = rs.getString("toAdress");
				double amount = rs.getDouble("amount");
				String signature = rs.getString("signature");

				Transaction transaction = new Transaction(fromAdress, toAdress, amount);

				transaction.setTransactionID(transactionId);
				transaction.setTimeStamp(timeStamp);
				transaction.setSignature(signature);

				transactionsFromTransactionPool.add(transaction);

			}

			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching transaction pool data in sql select.", e);
		}

		return transactionsFromTransactionPool;

	}

	/**
	 * Fetching all transaction entries from database table transactionPool and
	 * returning all fetched transactions in a list.
	 * 
	 * @return
	 */
	public ArrayList<Transaction> getAllTransactionsFromTransactionPool() {

		ArrayList<Transaction> transactionsFromTransactionPool = new ArrayList<Transaction>();

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("SELECT * FROM transactionPool");

			while (rs.next()) {

				String transactionId = rs.getString("transactionId");
				String timeStamp = rs.getString("timeStamp");
				String fromAdress = rs.getString("fromAdress");
				String toAdress = rs.getString("toAdress");
				double amount = rs.getDouble("amount");
				String signature = rs.getString("signature");

				Transaction transaction = new Transaction(fromAdress, toAdress, amount);

				transaction.setTransactionID(transactionId);
				transaction.setTimeStamp(timeStamp);
				transaction.setSignature(signature);

				transactionsFromTransactionPool.add(transaction);

			}

			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching transaction pool data in sql select.", e);
		}

		return transactionsFromTransactionPool;

	}

	/**
	 * Fetching all public key and wallet address entries from database table
	 * publicKeys and returning them as a hashmap where the wallet address is the
	 * key and the public key the value.
	 * 
	 * @return
	 */
	public HashMap<String, String> getAllPublicKeysFromRegister() {

		HashMap<String, String> allPublicKeysFromRegister = new HashMap<String, String>();

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("SELECT * FROM publicKeys");

			while (rs.next()) {

				String walletAdress = rs.getString("walletAdress");
				String publicKey = rs.getString("publicKey");

				allPublicKeysFromRegister.put(walletAdress, publicKey);

			}

			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching public keys in sql select.", e);
		}

		return allPublicKeysFromRegister;

	}

	/**
	 * Fetching the public key entry from database table publicKeys which primary
	 * key matches the input wallet address hand over to the function. The public
	 * key is returned as a string in a hex format.
	 * 
	 * @param walletAdress
	 * @return
	 */
	public String getHexStringPublicKeyForCorrespondingWalletAddress(String walletAdress) {

		String publicKeyForWalletAdress = null;

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement
					.executeQuery("SELECT publicKey FROM publicKeys WHERE walletAdress=" + "'" + walletAdress + "'");

			publicKeyForWalletAdress = rs.getString("publicKey");

			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching coresponding public key", e);
		}

		// TODO can be null
		return publicKeyForWalletAdress;

	}

	/**
	 * Fetching all block entries from database table blockChain and putting them in
	 * a Blockchain object which is returned.
	 * 
	 * @return
	 */
	public Blockchain getAllBlocksFromBlockchain() {

		Blockchain blockchain = new Blockchain();
		Marshaller marshaller = new Marshaller();

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("SELECT * FROM blockChain");

			while (rs.next()) {

				int blockIndex = rs.getInt("blockIndex");
				String blockMiningTime = rs.getString("miningTimeStamp");
				byte[] byteBlockHeader = (byte[]) rs.getBytes("blockHeader");
				byte[] byteTransactionsList = (byte[]) rs.getBytes("transactions");
				int transactionCounter = rs.getInt("transactionCounter");

				Object transformedBlockHeader = marshaller.transformByteToObject(byteBlockHeader);
				Object transformedTransactionsList = marshaller.transformByteToObject(byteTransactionsList);

				@SuppressWarnings("unchecked")
				Block block = new Block(blockIndex, (BlockHeader) transformedBlockHeader,
						(ArrayList<Transaction>) transformedTransactionsList, transactionCounter);
				block.setTimeStamp(blockMiningTime);

				blockchain.addBlockToBlockchain(block);
			}

			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching blockchain in sql select.", e);
		}

		return blockchain;

	}

	/**
	 * Fetching the block entry from database table blockChain which blocIndex has
	 * the maximal value.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Block getLastBlockFromBlockchain() {

		Marshaller marshaller = new Marshaller();
		Block block = null;

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(
					"SELECT * FROM blockChain WHERE blockIndex = (SELECT MAX(blockIndex) FROM blockChain)");

			if (rs.next()) {

				int blockIndex = rs.getInt("blockIndex");
				String blockMiningTime = rs.getString("miningTimeStamp");
				byte[] byteBlockHeader = (byte[]) rs.getBytes("blockHeader");
				byte[] byteTransactionsList = (byte[]) rs.getBytes("transactions");
				int transactionCounter = rs.getInt("transactionCounter");

				Object transformedBlockHeader = marshaller.transformByteToObject(byteBlockHeader);
				Object transformedTransactionsList = marshaller.transformByteToObject(byteTransactionsList);

				block = new Block(blockIndex, (BlockHeader) transformedBlockHeader,
						(ArrayList<Transaction>) transformedTransactionsList, transactionCounter);
				block.setTimeStamp(blockMiningTime);

			}
			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching blockchain in sql select.", e);
		}

		// CAN BE NULL CATCH
		return block;

	}

	/**
	 * Fetching the block entry from database table blockChain which blocIndex has
	 * the input value.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Block getBlockWithInputIndex(int blockIndexInput) {

		Marshaller marshaller = new Marshaller();
		Block block = null;

		try {

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(
					"SELECT * FROM blockChain WHERE blockIndex=" + "'" + String.valueOf(blockIndexInput) + "'");

			if (rs.next()) {

				int blockIndex = rs.getInt("blockIndex");
				String blockMiningTime = rs.getString("miningTimeStamp");
				byte[] byteBlockHeader = (byte[]) rs.getBytes("blockHeader");
				byte[] byteTransactionsList = (byte[]) rs.getBytes("transactions");
				int transactionCounter = rs.getInt("transactionCounter");

				Object transformedBlockHeader = marshaller.transformByteToObject(byteBlockHeader);
				Object transformedTransactionsList = marshaller.transformByteToObject(byteTransactionsList);

				block = new Block(blockIndex, (BlockHeader) transformedBlockHeader,
						(ArrayList<Transaction>) transformedTransactionsList, transactionCounter);
				block.setTimeStamp(blockMiningTime);
			}
			rs.close();
			statement.close();

		} catch (Exception e) {

			logger.error("Exception while fetching blockchain in sql select.", e);
		}

		// CAN BE NULL CATCH
		return block;

	}

}
