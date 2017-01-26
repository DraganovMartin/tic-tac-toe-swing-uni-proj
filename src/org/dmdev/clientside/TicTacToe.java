package org.dmdev.clientside;

import org.dmdev.serverside.Server;

import java.util.Scanner;

public class TicTacToe extends Thread {
	private static boolean serverReady = false;
	private String statistics[];

	public TicTacToe(String appType) throws Exception {
		super(appType);
	}

	// if the command is "java TicTacToe client IP", run this
	public TicTacToe(String appType, String targetIp) throws Exception {

		int noOfGames = 0;
		String name = null;

		statistics = new String[1000];
		for (int i = 0; i < 1000; i++)
			statistics[i] = null;

		if (appType.toUpperCase().equals("CLIENT")) {

			try {
				while (true) {
					GUI gui = new GUI(targetIp, statistics, name);
					gui.Play();

					// if play again, playing history will be cleared,
					// dispose old interface and start again

					if (!gui.lblStatus.getText().equals("Status: Server down! Game stopped.")) {

						// get the game result first no matter the player
						// continues to play or not
						statistics[++noOfGames] = gui.getGameResult();
						name = gui.getPlayerName();

						if (gui.playAgain())
							gui.dispose();
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

			if (!serverReady) {

				try {

					serverReady = true;
					new Server();

				} catch (Exception e) {
					System.err.println("SYSTEM MSG: Exception when starting server: " + e.toString());
				}
			}

		} else if (Thread.currentThread().getName().equals("ClientThread")) {
			int NoOfGames = 0;
			String name = null;

			statistics = new String[1000];
			for (int i = 0; i < 1000; i++)
				statistics[i] = null;

			try {

				while (!serverReady)
					sleep(100);

				while (true) {
					GUI gui = new GUI("127.0.0.1", statistics, name);
					gui.Play();

					// get the game result first no matter the player continues
					// to play or not
					statistics[++NoOfGames] = gui.getGameResult();
					name = gui.getPlayerName();

					// if play again, playing history will be cleared,
					// dispose old interface and start again

					if (!gui.lblStatus.getText().equals("Status: Server down! Game stopped.")) {
						if (gui.playAgain())
							gui.dispose();
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

		Scanner sc = new Scanner(System.in);
		String answer;
		System.out.print("Start server (Y/N) : ");
		answer = sc.nextLine();
		if(answer.equalsIgnoreCase("Y")){
			// Starting server
			TicTacToe serverThread = new TicTacToe("ServerThread");
			// Starting GUI of the server
			TicTacToe clientThread = new TicTacToe("ClientThread");
			serverThread.start();
			clientThread.start();
		}
		else {
			System.out.println("Aborting !!!");
			System.exit(2);
		}
		System.out.println("Start client (Y/N) : ");
		answer = sc.nextLine();

		if (answer.equalsIgnoreCase("Y")) {
			TicTacToe app = new TicTacToe("client", "localhost");
			app.start();
		} else {
			System.out.println("Aborting !!!");
			System.exit(2);
		}

	}

}
