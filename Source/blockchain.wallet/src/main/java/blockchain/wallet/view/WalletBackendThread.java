package blockchain.wallet.view;

import blockchain.wallet.controller.WalletController;

/**
 * Creates a thread in which all operations of this Wallet project are running
 * in the backend, while the Cli is receiving commands that are been forwarded
 * to the backend.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 15 Dec 2021
 */
public class WalletBackendThread implements Runnable {

	private WalletController walletController;

	/**
	 * Instantiates a new WalletBackendThread object
	 */
	public WalletBackendThread() {
		super();
		this.walletController = new WalletController();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Starts the WalletController object which creates a new connection, wallet and
	 * updates node data while broadcasting transactions to the network.
	 */
	@Override
	public void run() {

		walletController.start();

	}

	/**
	 * Returning the WalletController object.
	 * 
	 * @return
	 */
	public WalletController getWalletController() {
		return walletController;
	}

}
