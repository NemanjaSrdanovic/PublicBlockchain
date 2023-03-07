package blockchain.concensus;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

/**
 * This singleton pattern provides the classes with a method that takes a string
 * input and calculates the hash of that string by using the SHA256 hash
 * function.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Dec 2021
 */
public class SHA256Hasher {

	private SHA256Hasher() {

	}

	/**
	 * Calculated the hash for a given input string
	 * 
	 * @param inputString
	 * @return
	 */
	public static String returnSHA256HashStringFromString(String inputString) {

		String hash = null;

		hash = Hashing.sha256().hashString(inputString, StandardCharsets.UTF_8).toString();

		if (hash == null)
			throw new NullPointerException("Hashing algorithm returned null for given input string.");

		return hash;

	}

}
