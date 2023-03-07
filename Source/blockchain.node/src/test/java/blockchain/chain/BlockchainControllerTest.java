package blockchain.chain;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.controller.NodeController;
import blockchain.database.DriverClass;

/**
 * Testing the functionalities of the BlockchainController object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class BlockchainControllerTest {

	private static NodeController testNodeController;
	private static DriverClass testDatabase;
	private static BlockchainController testBlockchainController;
	private static Block newBlockToBeAdded;
	private static Block oldBlockInDatabase;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Block genesisBlock = new Block(0, null, null, 1);

		newBlockToBeAdded = new Block(1, null, null, 5);
		Thread.sleep(1);
		oldBlockInDatabase = new Block(1, null, null, 5);

		testNodeController = Mockito.mock(NodeController.class);
		testDatabase = Mockito.mock(DriverClass.class);

		Mockito.when(testDatabase.getLastBlockFromBlockchain()).thenReturn(genesisBlock);
		Mockito.when(testNodeController.getDatabase()).thenReturn(testDatabase);
		Mockito.doNothing().when(testDatabase).insertBlockIntoBlockchain(Mockito.any(Block.class));
		Mockito.doNothing().when(testDatabase).moveMinedTransactionsFromPool(Mockito.any(Block.class));
		Mockito.doNothing().when(testNodeController).broadcastNewlyMinedBlockToTheNetwork(Mockito.any(Block.class));

		testBlockchainController = new BlockchainController(testNodeController);

	}

	/**
	 * Testing the BlockchainController
	 * resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(..) method by
	 * mocking a block object with the same index as the block which is already
	 * saved in the blockchain.The new block has a mining times stamp which is
	 * before the mining time stamp of the block already in the blockchain. This
	 * results in the old block being replaced by the new one.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void mockBlockWithSameIndex_CallResolveBlockConsensusConflict_BlockReplacedWithNew()
			throws InterruptedException {

		Mockito.when(testDatabase.getBlockWithInputIndex(Mockito.anyInt())).thenReturn(oldBlockInDatabase);

		testBlockchainController.resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(newBlockToBeAdded, false);

		ArgumentCaptor<Block> capturedBlock = ArgumentCaptor.forClass(Block.class);

		Mockito.verify(testDatabase).replaceBlockFromBlockchain(capturedBlock.capture());

		assertTrue(capturedBlock.getValue().equals(newBlockToBeAdded));
	}

	/**
	 * Testing the BlockchainController
	 * resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(..) method by
	 * mocking a new block object that hasn't been inserted in the blockchain jet.
	 * This results in the block being added to the blockchain and broadcasted to
	 * the network as it has been mined by this node.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void mockNewlyMinedBlock_CallResolveBlockConsensusConflict_NewBlockInsertedIntoDBAndBroadcasted()
			throws InterruptedException {

		Mockito.when(testDatabase.getBlockWithInputIndex(Mockito.anyInt())).thenReturn(null);

		testBlockchainController.resolveBlockConsensusConflictOrInsertNewBlockIntoBlockchain(newBlockToBeAdded, true);

		ArgumentCaptor<Block> broadcastCapturedBlock = ArgumentCaptor.forClass(Block.class);
		ArgumentCaptor<Block> databaseCapturedBlock = ArgumentCaptor.forClass(Block.class);

		Mockito.verify(testDatabase, atLeast(1)).insertBlockIntoBlockchain(databaseCapturedBlock.capture());
		Mockito.verify(testNodeController).broadcastNewlyMinedBlockToTheNetwork(broadcastCapturedBlock.capture());

		assertTrue(broadcastCapturedBlock.getValue().equals(databaseCapturedBlock.getValue()));
		assertTrue(broadcastCapturedBlock.getValue().equals(newBlockToBeAdded));
	}

}
