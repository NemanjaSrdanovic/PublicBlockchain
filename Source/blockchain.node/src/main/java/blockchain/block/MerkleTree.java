package blockchain.block;

import java.util.ArrayList;

import blockchain.concensus.SHA256Hasher;

/**
 * This merkle tree object is used to calculate the merkle root for a given
 * transaction list with which the correctness of this transaction list can be
 * proved.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class MerkleTree {

	private ArrayList<Transaction> transactions;
	private String merkleRoot;

	/**
	 * Instantiates a new merkle tree object. The parameters must not be null.
	 * 
	 * @param transactions
	 */
	public MerkleTree(ArrayList<Transaction> transactions) {

		if (transactions == null)
			throw new IllegalArgumentException("Transaction list for root calculation can´t be null");

		this.transactions = transactions;
		this.merkleRoot = calculateMerkleRoot(transactions);
	}

	/**
	 * Calculates the merkle root for the given transaction list by recursively
	 * hashing two neighbour transactions thll only one output is left. If the
	 * transaction list contains a uneven number of transactions the last
	 * transaction is copied and put at the end of the list, so that every element
	 * in the list has a neighbour. If the transaction list is empty, a empty string
	 * is returned as the merkle root for that list. When the transaction list
	 * contains only one transaction, the hash of that transaction is returned as
	 * the merkle root.
	 * 
	 * @param transactions
	 * @return
	 */
	private String calculateMerkleRoot(ArrayList<Transaction> transactions) {

		ArrayList<String> merkleRootList = createMerkleLeafNodes(transactions);

		if (merkleRootList.isEmpty()) {

			return " ";
		}

		if (merkleRootList.size() == 1) {

			return merkleRootList.get(0);
		}

		while (merkleRootList.size() > 1) {

			ArrayList<String> merkleNodeList = new ArrayList<String>();

			if (merkleRootList.size() % 2 != 0) {

				merkleRootList.add(merkleRootList.get(merkleRootList.size() - 1));

			}

			for (int i = 0; i < merkleRootList.size() - 1; i += 2) {

				String leftLeaf = merkleRootList.get(i);
				String rightLeaf = merkleRootList.get(i + 1);

				String node = SHA256Hasher.returnSHA256HashStringFromString(leftLeaf + rightLeaf);

				merkleNodeList.add(node);
			}

			merkleRootList = merkleNodeList;

		}

		return merkleRootList.get(0);
	}

	/**
	 * Helper method for the calculation of the merkle root that returns a list of
	 * hashes for a hand over transaction list. That list is than recursively hashed
	 * till the merkle root has been found.
	 * 
	 * @param transactions
	 * @return
	 */
	private ArrayList<String> createMerkleLeafNodes(ArrayList<Transaction> transactions) {

		ArrayList<String> merkleLeafList = new ArrayList<String>();

		for (Transaction t : transactions) {

			merkleLeafList.add(SHA256Hasher.returnSHA256HashStringFromString(t.getTransactionData()));
		}

		return merkleLeafList;
	}

	/**
	 * Return the calculated merkle root for a transaction list which was hand over
	 * to the merkle tree object.
	 * 
	 * @return
	 */
	public String getMerkleRoot() {
		return merkleRoot;
	}

	/**
	 * Returns the transaction list which was hand over to the merkle tree object to
	 * calculate the merkle root.
	 * 
	 * @return
	 */
	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

}
