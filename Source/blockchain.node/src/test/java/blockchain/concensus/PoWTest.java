package blockchain.concensus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing the functionalities of the PoW object by mocking corresponding
 * objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class PoWTest {

	private static PoW testConcensusAlg;
	private static String correctHash;
	private static String wrongHash;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testConcensusAlg = new PoW();
		correctHash = "000000000000000000000000000000000000000000000000000";
		wrongHash = "764cc0ebb447fdefcabcd14ba9eabdf9e1c6c89f7ea24e735e376";

	}

	/**
	 * Running exactly once during the test run - at the very end of the test
	 * execution to clear all states of the test execution.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		testConcensusAlg = null;
		correctHash = null;
		wrongHash = null;
	}

	/**
	 * Testing the PoW object hasHashTheCorrectDifficulty() method by passing a
	 * correct hash which returns true
	 */
	@Test
	public void mockBlockHash_CallHasHashTheCorrectDifficulty_DifficultyCorrect() {

		assertTrue(testConcensusAlg.hasHashTheCorrectDifficulty(correctHash));
	}

	/**
	 * Testing the PoW object hasHashTheCorrectDifficulty() method by passing a
	 * wrong hash which returns false
	 * 
	 */
	@Test
	public void mockBlockHash_CallHasHashTheCorrectDifficulty_DifficultyNotCorrect() {

		assertFalse(testConcensusAlg.hasHashTheCorrectDifficulty(wrongHash));
	}

}
