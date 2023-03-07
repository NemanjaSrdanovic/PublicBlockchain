package blockchain.block;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import blockchain.concensus.SHA256Hasher;

/**
 * Testing the functionalities of the MerkleTree object by mocking corresponding
 * objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class MerkleTreeTest {

	private static String testRoot;
	private static ArrayList<Transaction> testTransactions;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Transaction t1 = new Transaction("xy", "xz", 1000);
		Transaction t2 = new Transaction("xv", "xg", 1200);
		Transaction t3 = new Transaction("xb", "xd", 1050);

		testTransactions = new ArrayList<>(Arrays.asList(t1, t2, t3));

		String leftLeaf_t_1_2 = SHA256Hasher
				.returnSHA256HashStringFromString(SHA256Hasher.returnSHA256HashStringFromString(t1.getTransactionData())
						+ SHA256Hasher.returnSHA256HashStringFromString(t2.getTransactionData()));
		String rightLeaf_t_3_3 = SHA256Hasher
				.returnSHA256HashStringFromString(SHA256Hasher.returnSHA256HashStringFromString(t3.getTransactionData())
						+ SHA256Hasher.returnSHA256HashStringFromString(t3.getTransactionData()));

		testRoot = SHA256Hasher.returnSHA256HashStringFromString(leftLeaf_t_1_2 + rightLeaf_t_3_3);

	}

	/**
	 * Checking if the manually calculated merkle root is equals to the merkle root
	 * calculated by the object in the initialisation process.
	 */
	@Test
	public void calculateMerkleTreeFromTransactionList_ForwardTheSameTransacitonToMehtod_RootTheSameInBoth() {

		MerkleTree testTree = new MerkleTree(testTransactions);

		assertTrue(testTree.getMerkleRoot().equals(testRoot));
	}
}
