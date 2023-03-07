package blockchain.wallet.view;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constraints.Constraints;

/**
 * This WalletApp object is used to display the operations which can be
 * performed using the wallet/controller object like sending transactions,
 * viewing the blockchain and calling that operations.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 14 Dec 2021
 **/
public class WalletApp {

	private static Logger logger = LoggerFactory.getLogger(WalletApp.class);

	public static void main(String[] args) throws InterruptedException {

		logger.info("Starting wallet Cli...");

		String option;
		WalletBackendThread walletBackend;
		Thread thread = new Thread(walletBackend = new WalletBackendThread());
		thread.start();
		Thread.sleep(10000);
		Scanner scanner = new Scanner(System.in);

		while (true)

		{

			if (walletBackend.getWalletController().getConnection().getClient().getConnectedIPsPort()
					.size() >= Constraints.MIN_NODES_CONNECTED_TO_SEND) {

				System.out.println("\nEnter an Option");
				System.out.println("1 - Create a transaction");
				System.out.println("2 - Display pending transactions");
				System.out.println("3 - Display blockchain");
				System.out.println("4 - Display balance");
				System.out.println("5 - Display wallet address");
				System.out.println("0 - Exit");
				System.out.print("\n ");
				System.out.print("\nType an option: \n");

				option = null;
				try {
					option = scanner.next();
				} catch (Exception e) {
					System.err.println("Enter a Number!!!");
				}

				switch (option) {
				case "1":

					String toAdress = null;
					Long amount = null;

					try {
						System.out.println("\nEnter the receiver adress:");
						toAdress = scanner.next();
					} catch (Exception e) {
						System.err.println("Enter a adress!!!");
					}

					try {
						System.out.println("\nEnter the amount to be sent:");
						amount = Long.valueOf(scanner.next());
					} catch (Exception e) {

						System.err.println(
								"\nAmount has wrong format, re-enter the amount (Use ´.´ for representing decimal places)");

						System.out.println("\nEnter the amount to be sent:");

						try {

							amount = Long.valueOf(scanner.next());

						} catch (Exception e1) {

							System.err.println("\nAmount fromat wrong, transaction couldn´t be send.");
						}
					}

					if (amount != null)
						walletBackend.getWalletController().sendTransaction(toAdress, amount);

					break;

				case "2":

					System.out.println(walletBackend.getWalletController().pendingTransactionsToString());

					break;

				case "3":

					System.out.println(walletBackend.getWalletController().getBlockchain().toString());

					break;

				case "4":

					System.out.println("Balance (till block "
							+ (walletBackend.getWalletController().getBlockchain().getChain().size() - 1) + "): "
							+ walletBackend.getWalletController().getWallet().getBalance());

					break;

				case "5":

					System.out.println(
							"Wallet address: " + walletBackend.getWalletController().getWallet().getWalletAddress());

					break;

				case "0":

					System.out.println("Exit...");
					scanner.close();
					System.exit(1);

					break;
				default:
					System.err.println("Option doesn't exist! Please choose again.");
				}

			} else {

				logger.info(
						"Searching for nodes to connect. Operations are possible after enough nodes are connected.");

				Thread.sleep(5000);
			}

		}

	}

}
