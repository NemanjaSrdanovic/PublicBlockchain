package blockchain.chain;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.block.BlockController;
import blockchain.block.BlockHeader;
import blockchain.block.Transaction;
import blockchain.concensus.PoW;
import blockchain.concensus.SHA256Hasher;
import blockchain.controller.NodeController;
import blockchain.database.DriverClass;
import blockchain.wallet.model.Wallet;
import connection.Connection;
import controllers.ConnectionHandler;
import udp_connection.UDP_Client;

/**
 * Testing the functionalities of the MiningWorker object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class MiningWorkerTest {

	private static BlockchainController testBlockchainController;
	private static BlockController testBlockController;
	private static NodeController testNodeController;
	private static DriverClass testDatabase;
	private static PoW testConsensusAlgorithm;
	private static MiningWorker testMiningWorker;
	private static ConnectionHandler testConnectionHandler;
	private static Connection testConnection;
	private static UDP_Client testUDP_Client;
	private static Wallet testWallet;
	private static Transaction t1;
	private static Block genesisBlock;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testBlockchainController = Mockito.mock(BlockchainController.class);
		testNodeController = Mockito.mock(NodeController.class);
		testDatabase = Mockito.mock(DriverClass.class);
		testConsensusAlgorithm = Mockito.mock(PoW.class);
		testBlockController = new BlockController();
		testConnectionHandler = Mockito.mock(ConnectionHandler.class);
		testConnection = Mockito.mock(Connection.class);
		testUDP_Client = Mockito.mock(UDP_Client.class);
		testWallet = Mockito.mock(Wallet.class);

		Mockito.when(testBlockchainController.getDatabase()).thenReturn(testDatabase);
		Mockito.when(testBlockchainController.getNodeController()).thenReturn(testNodeController);
		Mockito.when(testBlockchainController.getConsensusAlgorithm()).thenReturn(testConsensusAlgorithm);
		Mockito.when(testBlockchainController.getBlockController()).thenReturn(testBlockController);

		t1 = new Transaction("xy", "xz", 1000);
		ArrayList<Transaction> testTransactions = new ArrayList<>(Arrays.asList(t1));

		Mockito.when(testDatabase.getAllTransactionsFromTransactionPoolWhereTimestampBefore(null))
				.thenReturn(testTransactions);

		genesisBlock = new Block(0, new BlockHeader("null", "null", 4), null, 1);

		Mockito.when(testDatabase.getLastBlockFromBlockchain()).thenReturn(genesisBlock);
		Mockito.doNothing().when(testBlockchainController).setCurrentlyMinedBlockIndex(Mockito.anyInt());

		Set<String> testConnectedIPsPort = new HashSet<String>(Arrays.asList("192.2.2.1", "192.2.2.2"));

		Mockito.when(testNodeController.getConnectionHandler()).thenReturn(testConnectionHandler);
		Mockito.when(testConnectionHandler.getConnection()).thenReturn(testConnection);
		Mockito.when(testConnection.getClient()).thenReturn(testUDP_Client);
		Mockito.when(testUDP_Client.getConnectedIPsPort()).thenReturn(testConnectedIPsPort);

		Mockito.when(testBlockchainController.getWallet()).thenReturn(testWallet);
		Mockito.when(testWallet.getWalletAddress()).thenReturn("xy");

		Mockito.when(testConsensusAlgorithm.hasHashTheCorrectDifficulty(Mockito.anyString())).thenCallRealMethod();

		testMiningWorker = new MiningWorker(testBlockchainController);
	}

	/**
	 * Testing the MiningWorker run() and calculateBlockHash() methods by mocking
	 * the triggering of the mining process where enough nodes are connected to
	 * start the mining and the transaction pool has been synchronised before the
	 * mining process. This results in generating a new BlockHeader object which is
	 * been hashed until a hash with the correct difficulty has been hashed. After
	 * this BlockHeader is used to generate a new Block object which contains all
	 * mocked transactions and is captured and checked when the resolveBlockConc...
	 * method is been called.
	 */
	@Test
	public void startMiningWorker_EnoughNodesConnectedAndSynchronisationSuccessful_HashCalculatedBlockMined() {

		Mockito.when(testNodeController.synchronizeTransactionPoolBeforeMining()).thenReturn(true);

		testMiningWorker.run();

		ArgumentCaptor<Block> capturedBlock = ArgumentCaptor.forClass(Block.class);
		ArgumentCaptor<Boolean> capturedBoolean = ArgumentCaptor.forClass(Boolean.class);

		Mockito.verify(testBlockchainController).resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(
				capturedBlock.capture(), capturedBoolean.capture());

		assertTrue(capturedBlock.getValue().getTransactionList().size() == 2);
		assertTrue(capturedBlock.getValue().getTransactionList().contains(t1));
		assertTrue(testConsensusAlgorithm.hasHashTheCorrectDifficulty(SHA256Hasher.returnSHA256HashStringFromString(
				capturedBlock.getValue().getBlockHeader().getHeaderDataForHashCalculation())));
		assertTrue(capturedBlock.getValue().getIndex() > genesisBlock.getIndex());

	}

}
