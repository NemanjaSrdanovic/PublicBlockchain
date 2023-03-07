package blockchain.concensus;

public class PoW {

	private final static int difficulty = 4;
	private static final int miningRate = 60;
	private static final int minerStartUpTime = 120;
	private static final double miningReward = 50;

	/**
	 * Instantiates a new PoW object.
	 */
	public PoW() {
		super();

	}

	/**
	 * Returns if the input hash has the correct difficulty by comparing the first
	 * x(difficulty number) of substrings and comparing if they match the char zero.
	 * 
	 * @param blockHash
	 * @return
	 */
	public boolean hasHashTheCorrectDifficulty(String blockHash) {

		if (blockHash.length() < PoW.difficulty)
			return false;

		return blockHash.substring(0, PoW.difficulty).equalsIgnoreCase("0".repeat(PoW.difficulty));
	}

	/**
	 * Returns the difficulty which the hash calculated by the node has to have.
	 * 
	 * @return
	 */
	public static int getDifficulty() {
		return difficulty;
	}

	/**
	 * Returns the amount of seconds which passes between two mining processes.
	 * 
	 * @return
	 */
	public static int getMiningRate() {
		return miningRate;
	}

	/**
	 * Returns the reward which the node, that mines the block first gets.
	 * 
	 * @return
	 */
	public static double getMiningreward() {
		return miningReward;
	}

	/**
	 * Returns the amount of seconds which passes from moment the node was started
	 * till the first mining process has been started.
	 * 
	 * @return
	 */
	public static int getMinerStartupTime() {
		return minerStartUpTime;
	}

}
