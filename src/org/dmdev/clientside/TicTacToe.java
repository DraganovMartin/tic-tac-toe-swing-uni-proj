package org.dmdev.clientside;

import org.dmdev.serverside.Server;

public class TicTacToe extends Thread {
	private static boolean ServerReady = false;
	private String Statistics[];

	public TicTacToe(String AppType) throws Exception {
		super(AppType);
	}

	// if the command is "java TicTacToe client IP", run this
	public TicTacToe(String AppType, String TargetIp) throws Exception {

		int NoOfGames = 0;
		String Name = null;

		Statistics = new String[1000];
		for (int i = 0; i < 1000; i++)
			Statistics[i] = null;

		if (AppType.toUpperCase().equals("CLIENT")) {

			try {
				while (true) {
					Client TTTClient = new Client(TargetIp, Statistics, Name);
					TTTClient.Play();

					// if play again, playing history will be cleared,
					// dispose old interface and start again

					if (!TTTClient.lblStatus.getText().equals("Status: Server down! Game stopped.")) {

						// get the game result first no matter the player
						// continues to play or not
						Statistics[++NoOfGames] = TTTClient.GetGameResult();
						Name = TTTClient.GetName();

						if (TTTClient.PlayAgain())
							TTTClient.dispose();
						else
							// not play again, the player can still read the
							// play history
							break;
					} else
						break;

				}

			} catch (Exception e) {
				System.err.println("SYSTEM MSG: Exception when connecting to server: " + e.toString());
			}

		}

	}

	// if the command is "java server", start two threads
	// one to start the server, one to start the client
	public void run() {

		if (Thread.currentThread().getName().equals("ServerThread")) {

			if (!ServerReady) {

				try {

					ServerReady = true;
					Server TTTServer = new Server();

				} catch (Exception e) {
					System.err.println("SYSTEM MSG: Exception when starting server: " + e.toString());
				}
			}

		} else if (Thread.currentThread().getName().equals("ClientThread")) {
			int NoOfGames = 0;
			String Name = null;

			Statistics = new String[1000];
			for (int i = 0; i < 1000; i++)
				Statistics[i] = null;

			try {

				while (!ServerReady)
					sleep(100);

				while (true) {
					Client TTTClient = new Client("127.0.0.1", Statistics, Name);
					TTTClient.Play();

					// get the game result first no matter the player continues
					// to play or not
					Statistics[++NoOfGames] = TTTClient.GetGameResult();
					Name = TTTClient.GetName();

					// if play again, playing history will be cleared,
					// dispose old interface and start again

					if (!TTTClient.lblStatus.getText().equals("Status: Server down! Game stopped.")) {
						if (TTTClient.PlayAgain())
							TTTClient.dispose();
						else
							// not play again, the player can still read the
							// play history
							break;
					} else
						break;

				}
			} catch (Exception e) {
				System.err.println("SYSTEM MSG: Exception when connecting to server: " + e.toString());
			}

		}
	}

	public static void main(String args[]) throws Exception {

		if ((args.length == 1) && (args[0].toUpperCase().equals("SERVER"))) {

			TicTacToe ServerHandlerThread = new TicTacToe("ServerThread");
			TicTacToe ClientHandlerThread = new TicTacToe("ClientThread");

			ServerHandlerThread.start();
			ClientHandlerThread.start();

		} else if (args.length == 2) {
			TicTacToe app = new TicTacToe(args[0], args[1]);
		} else {
			System.out.println("Usage: java TicTacToe client|server [ipaddress].");

		}

	}

}
