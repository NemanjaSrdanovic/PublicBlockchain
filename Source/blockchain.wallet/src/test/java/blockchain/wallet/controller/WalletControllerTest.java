package blockchain.wallet.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import blockchain.block.Block;
import blockchain.block.Transaction;
import blockchain.chain.Blockchain;
import blockchain.wallet.model.Wallet;
import connection.Connection;
import enumerations.EMessageEndpoint;
import messages.Message;
import udp_connection.UDP_Client;

/**
 * Testing the functionalities of the WalletController object by mocking
 * corresponding objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class WalletControllerTest {

	private static Wallet testWallet;
	private static Blockchain testBlockchain;
	private static WalletController testWalletController;
	private static Transaction genesisTransaction;
	private static Transaction walletOutTransaction;
	private static Transaction walletInTransaction;
	private static HashMap<String, Transaction> testPendingTransactions;
	private static Connection testConnection;
	private static UDP_Client testUDPClient;
	private static Set<String> testReceivedMessages;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testWallet = Mockito.mock(Wallet.class);
		testBlockchain = Mockito.mock(Blockchain.class);
		testWalletController = Mockito.mock(WalletController.class);
		LinkedList<Block> testChain = new LinkedList<>();
		testPendingTransactions = new HashMap<String, Transaction>();
		testConnection = Mockito.mock(Connection.class);
		testUDPClient = Mockito.mock(UDP_Client.class);
		testReceivedMessages = new HashSet<String>();

		genesisTransaction = new Transaction("system", "balance", 1000);
		walletOutTransaction = new Transaction("xy", "xz", 200);
		testPendingTransactions.put(walletOutTransaction.getTransactionID(), walletOutTransaction);
		walletInTransaction = new Transaction("xz", "xy", 199);
		Block testGenesisBlock = new Block(0, null, new ArrayList<>(Arrays.asList(genesisTransaction)), 1);
		Block firstBlock = new Block(1, null, new ArrayList<>(Arrays.asList(walletOutTransaction, walletInTransaction)),
				2);
		testChain.add(testGenesisBlock);
		testChain.add(firstBlock);

		Mockito.when(testWalletController.getWallet()).thenReturn(testWallet);
		Mockito.when(testWalletController.getBlockchain()).thenReturn(testBlockchain);
		Mockito.when(testBlockchain.getChain()).thenReturn(testChain);
		Mockito.when(testWallet.getWalletPendingTransaction()).thenReturn(testPendingTransactions);
		Mockito.when(testWallet.getWalletAddress()).thenReturn("xy");
		Mockito.doNothing().when(testWallet).setBalance(Mockito.anyDouble());
		Mockito.doNothing().when(testWallet).setPendingTransactionsAmount(Mockito.anyDouble());
		Mockito.when(testWallet.getPendingTransactionsAmount()).thenReturn(walletOutTransaction.getAmount());

		Mockito.when(testWallet.getHexStringPublicKey()).thenReturn("a3vdfe3adfzj3gs46632ngsf");
		Mockito.when(testWalletController.getConnection()).thenReturn(testConnection);
		Mockito.when(testConnection.getClient()).thenReturn(testUDPClient);
		Mockito.doNothing().when(testUDPClient).addMessage(Mockito.any(Message.class));
		Mockito.when(testWalletController.getReceivedMessages()).thenReturn(testReceivedMessages);

		Mockito.when(testWallet.getBalance()).thenReturn(1000.00);
		Mockito.when(testWallet.signTransaction(Mockito.anyString())).thenReturn("0x00y00a0f000faaf003");
		Mockito.when(testUDPClient.isMessageReceived(Mockito.any(Message.class))).thenReturn(true);

		Mockito.doCallRealMethod().when(testWalletController).setCurrentWalletBalance();
		Mockito.doCallRealMethod().when(testWalletController).sendWalletData();
		Mockito.doCallRealMethod().when(testWalletController).sendTransaction(Mockito.anyString(), Mockito.anyDouble());
		Mockito.doCallRealMethod().when(testWalletController).requestNetworkData();

	}

	/**
	 * Testing WalletController object method setCurrentWalletBalance() by mocking
	 * Blockchain containing the wallet in and out transactions. The calculated
	 * balance should be equals with the amount from the mocked in and out
	 * transactions.
	 */
	@Test
	public void setCurrentWalletBalanceCalled_BlockchainMocked_updateWalletBalanceAndupdatePendingTransactionsCalculateCorrect() {

		testWalletController.setCurrentWalletBalance();

		ArgumentCaptor<Double> capturedBalance = ArgumentCaptor.forClass(Double.class);
		Mockito.verify(testWallet, atLeast(1)).setBalance(capturedBalance.capture());

		assertTrue(capturedBalance.getValue() == (genesisTransaction.getAmount() + walletInTransaction.getAmount()
				- walletOutTransaction.getAmount()));

		assertTrue(testPendingTransactions.isEmpty());
	}

	/**
	 * Testing the WalletController object method sendWalletData(..) by catching the
	 * generated Message object and verifying that the object has the correct
	 * endpoint. The process should be executed without throwing a execution and the
	 * object forwarded to the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void sendWalletDataCalled_ReceivedMessagesContainingIDAndMessageWithPublicKeyAdded()
			throws InterruptedException {

		testWalletController.sendWalletData();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testReceivedMessages.contains(capturedMessage.getValue().getMessageId()));
		assertTrue(capturedMessage.getValue().getEndpoint().equals(EMessageEndpoint.PublicKey));
	}

	/**
	 * Testing the WalletController object method sendTransaction(..) by catching
	 * the generated Message object and verifying that the object has the correct
	 * endpoint. The process should be executed without throwing a execution and the
	 * object forwarded to the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void sendTransactionCalled_balanceSufficient_MessageSendAndTransactionAddedToPending()
			throws InterruptedException {

		Mockito.doNothing().when(testWalletController).setCurrentWalletBalance();

		testWalletController.sendTransaction("xz", 200);

		ArgumentCaptor<Message> capturedTransactionMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedTransactionMessage.capture());
		Transaction capturedTransaction = (Transaction) capturedTransactionMessage.getValue().getData();

		assertTrue(capturedTransactionMessage.getValue().getEndpoint().equals(EMessageEndpoint.Transaction));
		assertTrue(testPendingTransactions.containsKey(capturedTransaction.getTransactionID()));

	}

	/**
	 * Testing the WalletController object method requestNetworkData(..) by catching
	 * the generated Message object and verifying that the object has the correct
	 * endpoint. The process should be executed without throwing a execution and the
	 * object forwarded to the UPD_Client.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void requestNetworkDataCalled_DataRequestMessageAdded() throws InterruptedException {

		testWalletController.requestNetworkData();

		ArgumentCaptor<Message> capturedMessage = ArgumentCaptor.forClass(Message.class);
		Mockito.verify(testUDPClient, atLeast(1)).addMessage(capturedMessage.capture());

		assertTrue(testReceivedMessages.contains(capturedMessage.getValue().getMessageId()));
		assertTrue(capturedMessage.getValue().getEndpoint().equals(EMessageEndpoint.DataRequest));
	}
}
