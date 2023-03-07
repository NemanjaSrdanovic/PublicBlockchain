package blockchain.concensus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing the functionalities of the SHA256Hasher object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class SHA256HasherTest {

	private static String correctHashTextHash;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		correctHashTextHash = "c46b585af147b2c433087b658443c9a2b0ce2414a26b3e3e74e259a0e5fecad6";
	}

	/**
	 * Testing the SHA256Hasher object method returnSHA256HashStringFromString() by
	 * passing a string which returns a string that is equal with the correct hash
	 * that would be returned.
	 */
	@Test
	public void inputcorrectHashForText_CallReturnSHA256HashStringFromString_SameOutputAsText() {

		assertTrue(correctHashTextHash.equals(SHA256Hasher.returnSHA256HashStringFromString("correctHashTextHash")));
	}

	/**
	 * Testing the SHA256Hasher object method returnSHA256HashStringFromString() by
	 * passing a string which returns a string that is not equal with the correct
	 * hash that would be returned.
	 */
	@Test
	public void inputCorrectHashForText_CallReturnSHA256HashStringFromString_DifferentOutputAsText() {

		assertFalse(correctHashTextHash.equals(SHA256Hasher.returnSHA256HashStringFromString("CorrectHashTextHash")));
	}

}
